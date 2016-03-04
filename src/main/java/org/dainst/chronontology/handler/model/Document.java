package org.dainst.chronontology.handler.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.dainst.chronontology.Constants;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author Daniel M. de Oliveira
 */
public class Document {

    public static final String VERSION = "version";
    public static final String RESOURCE = "resource";
    public static final String MODIFIED = "modified";
    public static final String CREATED = "created";
    public static final String ID = "@id";
    public static final String DATASET = "dataset";

    private static final String[] supportedProperties = new String[] {
        VERSION,MODIFIED,CREATED,DATASET,ID, RESOURCE
    };

    private final ObjectNode node;

    /**
     * @param userName
     * @param node
     * @param id
     */
    public Document(
            final String id,
            final JsonNode node,
            final String userName) {

        this.node = (ObjectNode) node;
        initNode(id,userName);
    }

    private Document(
            final JsonNode node) {

        this.node = (ObjectNode) node;
    }


    /**
     * @param oldDoc
     * @return null if oldDoc is null
     */
    public static Document from(final JsonNode oldDoc) {
        if (oldDoc==null) return null;
        if (oldDoc.get(ID)==null) {
            throw new IllegalArgumentException(ID + Constants.MSG_NOT_NULL);
        }

        return new Document(
            oldDoc
        );
    }

    private void filterUnwanted(){
        List<String> toRemove= new ArrayList<String>();
        Iterator<String> fn= node.fieldNames();
        while (fn.hasNext()){
            String fk= fn.next();
            if (!Arrays.asList(supportedProperties).contains(fk))
            toRemove.add(fk);
        }
        for (String fk:toRemove)
            node.remove(fk);
    }

    private void initNode(String id,String userName) {
        setNodeId(id);
        initVersion();
        initCreatedAndModifiedDates(userName);
        filterUnwanted();
    }

    /**
     * Mixes in json from a node considered to be an older version
     * of this document. The created value of the current document gets
     * overwritten by the created value from the old one.
     *
     * The values of the modified array get merged with the modified date
     * of the current document.
     *
     * @param oldDm
     * @return
     */
    public Document merge(final Document oldDm) {
        JsonNode oldNode= oldDm.j();

        mergeModifiedDates(oldNode);
        overwriteCreatedDate(oldNode);
        setVersion(oldNode);
        return this;
    }


    /**
     * @return ISO 8601 formatted date.
     */
    private String date() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        return currentDateTime.format(DateTimeFormatter.ISO_DATE_TIME)+"Z";
    }


    private void initVersion() {
        node.put(VERSION, 1);
    }

    private void initCreatedAndModifiedDates(String userName) {
        ObjectNode created= (ObjectNode) new ObjectMapper().createObjectNode();
        created.put("user",userName);
        created.put("date",date());
        node.put(CREATED, created);
        ArrayNode a = node.putArray(MODIFIED);
        a.add(created);
    }

    public JsonNode j() {
        return node;
    }

    @Override
    public String toString() {
        return node.toString();
    }

    private void setNodeId(String id) {
        node.put(ID, id);
    }

    private void setVersion(JsonNode oldNode) {
        Integer version= oldNode.get(VERSION).asInt();
        version++;
        node.put(VERSION, version);
    }

    private void overwriteCreatedDate(JsonNode oldNode) {
        JsonNode dateCreated = oldNode.get(CREATED);
        node.put(CREATED, dateCreated);
    }

    private void mergeModifiedDates(JsonNode oldNode) {
        ArrayNode modifiedDates= (ArrayNode) oldNode.get(MODIFIED);
        modifiedDates.add(node.get(CREATED));
        node.set(MODIFIED, modifiedDates);
    }

    private String toString(JsonNode n) {
        return n.toString().replaceAll("\"","");
    }

    public String getId() {
        return toString(node.get(ID));
    }

    /**
     * @return null if the document belongs to no dataset.
     */
    public String getDataset() {
        if (node.get(DATASET)==null) return null;
        return toString(node.get(DATASET));
    }
}

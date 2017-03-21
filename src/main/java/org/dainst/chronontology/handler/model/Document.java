package org.dainst.chronontology.handler.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.dainst.chronontology.Constants;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author Daniel de Oliveira
 */
public class Document {

    public static final String VERSION = "version";
    public static final String RESOURCE = "resource";
    public static final String MODIFIED = "modified";
    public static final String CREATED = "created";
    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String DATASET = "dataset";
    public static final String DERIVED = "derived";
    public static final String RELATED = "related";
    public static final String NONE = "none";
    public static final String DELETED = "deleted";
    public static final String REPLACED_BY = "replacedBy";

    private static final String[] supportedProperties = new String[] {
        VERSION,MODIFIED,CREATED,DATASET,RESOURCE,DERIVED,RELATED,DELETED,REPLACED_BY
    };

    private final ObjectNode node;

    /**
     * @param id
     * @param type
     * @param node
     * @param userName
     */
    public Document(
            final String id,
            final String type,
            final JsonNode node,
            final String userName) {

        this.node = (ObjectNode) node;
        initNode(id,type,userName);
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
        if (oldDoc.get(RESOURCE)==null)
            throw new IllegalArgumentException(RESOURCE + Constants.MSG_NOT_NULL);
        if (oldDoc.get(RESOURCE).get(ID)==null)
            throw new IllegalArgumentException(RESOURCE+"."+ID + Constants.MSG_NOT_NULL);
        if (oldDoc.get(RESOURCE).get(TYPE)==null)
            throw new IllegalArgumentException(RESOURCE+"."+TYPE + Constants.MSG_NOT_NULL);

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

    private void initNode(String id,String type,String userName) {
        setUpResource(id,type);
        initVersion();
        initCreatedAndModifiedDates(userName);
        filterUnwanted();
        if (node.get(DATASET)==null)
            ((ObjectNode)node).put(DATASET,NONE);
    }

    /**
     * Mixes in json from a node considered to be an older version
     * of this document. The created value of the current document gets
     * overwritten by the created value from the old one.
     *
     * The values of the modified array get merged with the created date
     * of the current document. Additional modified dates in the current
     * document are not expected and will be overwritten.
     *
     * @param oldDm
     * @return
     */
    public Document merge(final Document oldDm) {
        JsonNode oldNode= oldDm.j();

        mergeModifiedDates(oldNode);
        overwriteCreatedDate(oldNode);
        overwriteTypeAndId(oldNode);
        setVersion(oldNode);
        return this;
    }


    /**
     * @return ISO 8601 formatted date.
     */
    private String date() {
        ZonedDateTime currentDateTime = ZonedDateTime.now();
        return currentDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
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
    }

    public JsonNode j() {
        return node;
    }

    @Override
    public String toString() {
        return node.toString();
    }

    private void setUpResource(String id,String type) {
        JsonNode resource= node.get(RESOURCE);
        if (resource==null) {
            ObjectNode created= new ObjectMapper().createObjectNode();
            node.put(RESOURCE,created);
            resource= node.get(RESOURCE);
        }
        ((ObjectNode)resource).put(ID, id);
        ((ObjectNode)resource).put(TYPE, type);
    }

    private void setVersion(JsonNode oldNode) {
        Integer version= oldNode.get(VERSION).asInt();
        version++;
        node.put(VERSION, version);
    }

    private void overwriteCreatedDate(JsonNode oldNode) {
        node.put(CREATED, oldNode.get(CREATED));
    }

    private void overwriteTypeAndId(JsonNode oldNode) {
        ((ObjectNode)node.get(RESOURCE)).put(ID, oldNode.get(RESOURCE).get(ID));
        ((ObjectNode)node.get(RESOURCE)).put(TYPE, oldNode.get(RESOURCE).get(TYPE));
    }

    public void mergeWithDataset(JsonNode replacementNode) {
        node.put(DELETED, true);
        node.put(REPLACED_BY, replacementNode.get(RESOURCE).get(ID));
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
        return toString(node.get(RESOURCE).get(ID));
    }

    public String getType() {
        return toString(node.get(RESOURCE).get(TYPE));
    }

    public boolean getDeleted() {
        JsonNode temp = node.get(DELETED);
        if(temp == null){
            return false;
        }
        return temp.booleanValue();
    }

    public String getReplacementId() {
        return node.get(REPLACED_BY).asText();
    }

    /**
     * @return null if the document belongs to no dataset.
     */
    public String getDataset() {
        if (node.get(DATASET)==null||node.get(DATASET).toString().replace("\"","").equals(NONE)) return null;
        return toString(node.get(DATASET));
    }
}

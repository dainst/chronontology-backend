package org.dainst.chronontology.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.dainst.chronontology.Constants.*;

/**
 * @author Daniel M. de Oliveira
 */
public class DocumentModel {

    private final ObjectNode node;
    private final String id;
    private final String userName;

    /**
     * @param userName
     * @param node
     * @param id
     */
    public DocumentModel(
            final String id,
            final JsonNode node,
            final String userName) {

        this.node = (ObjectNode) node;
        this.id= id;
        this.userName= userName;

        initNode();
    }

    private void initNode() {
        setNodeId();
        initVersion();
        initCreatedAndModifiedDates();
    }

    /**
     * Mixes in json from a node considered to be an older version
     * of this document. The created value of the current document gets
     * overwritten by the created value from the old one.
     *
     * The values of the modified array get merged with the modified date
     * of the current document.
     *
     * @param oldNode
     * @return
     */
    public DocumentModel merge(final JsonNode oldNode) {

        setNodeId();
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

    private void initCreatedAndModifiedDates() {
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

    private void setNodeId() {
        node.put("@id", id);
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
}

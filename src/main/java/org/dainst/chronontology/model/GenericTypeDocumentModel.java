package org.dainst.chronontology.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.dainst.chronontology.Constants.*;

/**
 * Clients outside of this package should never create instances
 * of it directly.
 *
 * @author Daniel M. de Oliveira
 */
class // Leave package private!

        GenericTypeDocumentModel implements DocumentModel {

    private final ObjectNode node;
    private final String typeName;
    private final String id;

    /**
     * Should not be called directly by clients outside of the model package.
     * They should use
     * {@link DocumentModelFactory#create(String, String, JsonNode)}
     * instead.
     * @param typeName
     * @param node
     * @param id
     */
    GenericTypeDocumentModel(
            final String typeName,
            final String id,
            final JsonNode node) {

        this.node = (ObjectNode) node;
        this.typeName= typeName;
        this.id= id;

        initNode();
    }

    private void initNode() {
        setNodeId();
        initVersion();
        initCreatedAndModifiedDates();
    }


    @SuppressWarnings("unused")
    private GenericTypeDocumentModel() {
        typeName= null;
        node = null;
        id= null;
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
        String date = date();
        node.put(CREATED, date);
        ArrayNode a = node.putArray(MODIFIED);
        a.add(date);
    }

    public JsonNode j() {
        return node;
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
    public GenericTypeDocumentModel merge(final JsonNode oldNode) {

        setNodeId();
        mergeModifiedDates(oldNode);
        overwriteCreatedDate(oldNode);
        setVersion(oldNode);
        return this;
    }

    private void setNodeId() {
        node.put("@id", "/" + typeName + "/" + id);
    }

    private void setVersion(JsonNode oldNode) {
        Integer version= oldNode.get(VERSION).asInt();
        version++;
        node.put(VERSION, version);
    }

    private void overwriteCreatedDate(JsonNode oldNode) {
        String dateCreated = oldNode.get(CREATED).asText();
        node.put(CREATED, dateCreated);
    }

    private void mergeModifiedDates(JsonNode oldNode) {
        ArrayNode modifiedDates= (ArrayNode) oldNode.get(MODIFIED);
        modifiedDates.add(node.get(CREATED));
        node.set(MODIFIED, modifiedDates);
    }
}

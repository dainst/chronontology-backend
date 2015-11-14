package org.dainst.chronontology.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Clients outside of this package should never create instances
 * of it directly.
 *
 * @author Daniel M. de Oliveira
 */
class // Leave package private!

        GenericTypeDocumentModel implements DocumentModel {

    private final JsonNode node;
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

        this.node= node;
        this.typeName= typeName;
        this.id= id;
        create();
    }

    @SuppressWarnings("unused")
    private GenericTypeDocumentModel() {
        node= null;
        typeName= null;
        id= null;
    }

    /**
     * @return ISO 8601 formatted date.
     */
    private String date() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        return currentDateTime.format(DateTimeFormatter.ISO_DATE_TIME)+"Z";
    }

    private void create()  {

        ((ObjectNode) node).put("@id", "/"+typeName+"/"+id);
        ((ObjectNode) node).put("version", 1);

        String date = date();
        ((ObjectNode) node).put("created", date);
        ArrayNode a = ((ObjectNode) node).putArray("modified");
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
    public GenericTypeDocumentModel mix(final JsonNode oldNode) {

        ArrayNode modifiedDates= (ArrayNode) oldNode.get("modified");
        modifiedDates.add(node.get("created"));
        ((ObjectNode) node).set("modified", modifiedDates);

        ((ObjectNode) node).put("@id", "/" + typeName + "/" + id);
        String dateCreated = oldNode.get("created").asText();
        ((ObjectNode) node).put("created", dateCreated);

        Integer version= oldNode.get("version").asInt();
        version++;
        ((ObjectNode) node).put("version", version);

        return this;
    }
}

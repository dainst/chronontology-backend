package org.dainst.chron;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Daniel M. de Oliveira
 */
public class DocumentModel {

    private final JsonNode node;
    private final String typeName;
    private final String id;

    public DocumentModel (
            final String typeName,
            final JsonNode node,
            final String id) {

        this.node= node;
        this.typeName= typeName;
        this.id= id;
        create();
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

        String date = date();
        ((ObjectNode) node).put("created", date);
        ArrayNode a = ((ObjectNode) node).putArray("modified");
        a.add(date);
    }

    JsonNode j() {
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
    DocumentModel mix(final JsonNode oldNode) {

        ArrayNode modifiedDates= (ArrayNode) oldNode.get("modified");
        modifiedDates.add(node.get("created"));
        ((ObjectNode) node).set("modified", modifiedDates);

        ((ObjectNode) node).put("@id", "/" + typeName + "/" + id);
        String dateCreated = oldNode.get("created").asText();
        ((ObjectNode) node).put("created", dateCreated);

        return this;
    }
}

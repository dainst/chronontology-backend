package org.dainst;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.dainst.C.TYPE_NAME;

/**
 * @author Daniel M. de Oliveira
 */
public class DocumentModel {

    private final JsonNode node;

    public DocumentModel (final JsonNode node) {
        this.node= node;
    }

    /**
     * @return ISO 8601 formatted date.
     */
    private String date() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ").format(new Date());
    }

    JsonNode addStorageInfo(final String id) throws IOException {
        JsonNode node = this.node.deepCopy();

        ((ObjectNode) node).put("@id", "/"+TYPE_NAME+"/"+id);

        String date = date();
        ((ObjectNode) node).put("created", date);
        ArrayNode a = ((ObjectNode) node).putArray("modified");
        a.add(date);

        return node;
    }

    JsonNode addStorageInfo(final JsonNode oldNode, final String id) {

        ((ObjectNode) node).put("@id", "/" + TYPE_NAME + "/" + id);
        String dateCreated = oldNode.get("created").asText();
        ((ObjectNode) node).put("created", dateCreated);

        ArrayNode modifiedDates = (ArrayNode) oldNode.get("modified");
        modifiedDates.add(date());
        ((ObjectNode) node).set("modified", modifiedDates);

        return node;
    }
}

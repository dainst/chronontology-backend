package org.dainst.chronontology.handler.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dainst.chronontology.Controller;

import javax.management.ObjectName;
import java.io.IOException;

/**
 * @author Daniel M. de Oliveira
 */
public class Results {

    private final static Logger logger = LogManager.getLogger(Results.class);
    private final String arrayName;
    private JsonNode json;

    public Results(String arrayName) {
        this.arrayName = arrayName;
        try {
            json = new ObjectMapper().readTree("{\""+this.arrayName+"\":[], \"facets\":{} }");
        } catch (IOException e) {} // WILL NOT HAPPEN
    }

    public Results(String arrayName, int total) {
        this.arrayName = arrayName;
        try {
            json = new ObjectMapper().readTree("{\""+this.arrayName+"\":[], \"total\":" + String.valueOf(total) + ", \"facets\":{} }");
        } catch (IOException e) {} // WILL NOT HAPPEN
    }

    public ArrayNode getAll() {
        return (ArrayNode) json.get(this.arrayName);
    }

    public Results add(final JsonNode jsonToAdd) {
        logger.debug(json);
        ArrayNode data=(ArrayNode) json.get(this.arrayName);
        data.add(jsonToAdd);
        return this;
    }

    public Results addFacet(final JsonNode jsonToAdd) {
        logger.debug(json);
        ((ObjectNode)json.get("facets")).setAll((ObjectNode) jsonToAdd);
        return this;
    }

    public boolean remove(int index) {
        ArrayNode data=(ArrayNode) json.get(this.arrayName);
        return (data.remove(index)!=null);
    }

    @Override
    public String toString() {
        return json.toString();
    }

    public JsonNode j() {
        return json;
    }
}

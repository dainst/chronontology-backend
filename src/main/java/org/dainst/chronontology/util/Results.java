package org.dainst.chronontology.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;

/**
 * @author Daniel M. de Oliveira
 */
public class Results {

    private final String arrayName;
    private JsonNode json;

    public Results(String arrayName) {
        this.arrayName= arrayName;
        try {
            json = new ObjectMapper().readTree("{\""+this.arrayName+"\":[]}");
        } catch (IOException e) {} // WILL NOT HAPPEN
    }

    public Results add(final JsonNode jsonToAdd)
            throws JsonProcessingException {
        ArrayNode data=(ArrayNode) json.get(this.arrayName);
        data.add(jsonToAdd);
        return this;
    }

    public JsonNode j() {
        return json;
    }
}
package org.dainst.chronontology.handler.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.log4j.Logger;
import org.dainst.chronontology.Controller;

import java.io.IOException;

/**
 * @author Daniel M. de Oliveira
 */
public class Results {

    private final static Logger logger = Logger.getLogger(Results.class);
    private final String arrayName;
    private JsonNode json;

    public Results(String arrayName) {
        this.arrayName = arrayName;
        try {
            json = new ObjectMapper().readTree("{\""+this.arrayName+"\":[]}");
        } catch (IOException e) {} // WILL NOT HAPPEN
    }

    public Results(String arrayName, int total) {
        this.arrayName = arrayName;
        try {
            json = new ObjectMapper().readTree("{\""+this.arrayName+"\":[], \"total\":" + String.valueOf(total) + "}");
        } catch (IOException e) {} // WILL NOT HAPPEN
    }

    public ArrayNode getAll() {
        return (ArrayNode) json.get(this.arrayName);
    }

    public Results add(final JsonNode jsonToAdd) {
        logger.info(json);
        ArrayNode data=(ArrayNode) json.get(this.arrayName);
        data.add(jsonToAdd);
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
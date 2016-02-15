package org.dainst.chronontology.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * @author Daniel M. de Oliveira
 */
public class JsonUtils {

    private final static Logger logger = Logger.getLogger(JsonUtils.class);

    /**
     * @param s
     * @return null if string not parsable
     */
    public static JsonNode json(String s)  {
        try {
            return new ObjectMapper().readTree(s);
        } catch (IOException e) {
            logger.warn("Cannot make JSON node from : '"+s+"'. Error is: "+e.getMessage());
            return null;
        }
    }

    public static JsonNode json() {
        return new ObjectMapper().createObjectNode();
    }
}

package org.dainst.chronontology;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * @author Daniel M. de Oliveira
 */
public class JsonIntegrationTestBase extends IntegrationTestBase {

    protected JsonNode sampleJson(final String sampleFieldValue) {
        JsonNode json= null;
        try {
            json = new ObjectMapper().readTree
                    ("{\"a\":\"" + sampleFieldValue + "\"}");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }
}

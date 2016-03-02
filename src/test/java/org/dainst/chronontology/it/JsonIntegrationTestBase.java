package org.dainst.chronontology.it;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.dainst.chronontology.handler.DocumentModel;
import org.dainst.chronontology.it.IntegrationTestBase;
import org.dainst.chronontology.util.JsonUtils;

import java.io.IOException;

/**
 * @author Daniel M. de Oliveira
 */
public class JsonIntegrationTestBase extends IntegrationTestBase {

    protected JsonNode sampleJson(final String sampleFieldValue) {
        return sampleJson(sampleFieldValue,null);
    }

    protected JsonNode sampleJson(final String sampleFieldValue,String id) {
        JsonNode json= null;
        json = JsonUtils.json();
        ((ObjectNode)json).put("a",sampleFieldValue);
        if (id!=null)
            ((ObjectNode)json).put("@id",id);
        return json;
    }
}

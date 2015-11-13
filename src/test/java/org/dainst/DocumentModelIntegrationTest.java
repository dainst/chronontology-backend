package org.dainst;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * @author Daniel M. de Oliveira
 */
public class DocumentModelIntegrationTest extends IntegrationTestBase {

    private JsonNode addId(JsonNode node, String id) throws JsonProcessingException {
        ((ObjectNode) node).put("@id", "/"+ TYPE_NAME+"/"+id);
        return node;
    }


    @Test
    public void respondWithEnrichedJSONonPost() throws IOException, JSONException {
        jsonAssertEquals(
                client.post(route("1"), sampleJson("b")),
                addId(sampleJson("b"), "1"));
    }

     /*
    @Test
    public void testIdTimeAndDateCreated() throws IOException, JSONException {

        jsonAssertEquals(
                post(route("1"), sampleJson("b")),
                addId(sampleJson("b"), "1"));  // TEST here

        jsonAssertEquals(
                get(route("1")),
                addId(sampleJson("b"), "1"));  // TEST here again

        jsonAssertEquals(
                post(route("1")+"?direct=true"),
                addId(sampleJson("b"), "1"));  // TEST here again

    @Test
    public void testIdTimeAndDateModified() throws IOException, JSONException {

        jsonAssertEquals(
                post(route("1"), sampleJson("b")),
                addId(sampleJson("b"), "1"));

        jsonAssertEquals(
                put(route("1")),
                addId(sampleJson("b"), "1"));

        jsonAssertEquals(
                post(route("1")+"?direct=true"),
                addId(sampleJson("b"), "1"));  // TEST here

    }*/
}

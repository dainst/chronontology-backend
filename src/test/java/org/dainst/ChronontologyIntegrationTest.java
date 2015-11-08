package org.dainst;

import static org.dainst.C.*;

import static org.testng.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * @author Daniel M. de Oliveira
 */
public class ChronontologyIntegrationTest extends IntegrationTestBase {

    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        startServer();
    }

    @AfterClass
    public static void afterClass() throws InterruptedException {
        stopServer();
        cleanDatastores();
    }

    private JsonNode sampleJson(String sampleFieldValue) throws IOException {
        return new ObjectMapper().readTree
                ("{\"a\":\"" + sampleFieldValue + "\"}");
    }

    private JsonNode addId(JsonNode node, String id) throws JsonProcessingException {
        ((ObjectNode) node).put("@id", "/"+ TYPE_NAME+"/"+id);
        return node;
    }

    private String route(String id) {
        return "/"+ C.TYPE_NAME+"/"+id;
    }

    @Test
    public void respondWithEnrichedJSONonPost() throws IOException {

        assertEquals(
                post(route("1"), sampleJson("b")),
                addId(sampleJson("b"), "1"));

    }

    @Test
    public void storeAndRetrieveOneDocument() throws IOException {

        post(route("1"), sampleJson("b"));

        assertEquals(
                get(route("1")),
                addId(sampleJson("b"), "1"));
    }

    @Test
    public void storeAndRetrieveMoreThanOneDocument() throws IOException {

        post(route("1"), sampleJson("b"));
        post(route("2"), sampleJson("a"));

        assertEquals(
                get(route("1")),
                addId(sampleJson("b"), "1"));
    }

    @Test
    public void retrieveDocumentsFromDifferentSources() throws IOException {

        connectDatastore.put("1",sampleJson("a"));
        mainDatastore.put("1",sampleJson("b"));

        assertEquals(
                get(route("1")),
                sampleJson("a"), "1");
        assertEquals(
                get(route("1") + "?direct=true"),
                sampleJson("b"), "1");
    }




}

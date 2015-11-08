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

import java.io.File;
import java.io.IOException;

/**
 * @author Daniel M. de Oliveira
 */
public class ChronontologyIntegrationTest extends IntegrationTestBase {

    private static final String TEST_FOLDER = "src/test/resources/";

    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        startServer();
    }

    @AfterClass
    public static void afterClass() throws InterruptedException {
        stopServer();

        new File(TEST_FOLDER + "1.txt").delete();
        new File(TEST_FOLDER + "2.txt").delete();
    }

    private JsonNode sampleJson(String sampleFieldValue) throws IOException {
        return new ObjectMapper().readTree
                ("{\"a\":\"" + sampleFieldValue + "\"}");
    }

    private JsonNode enrichWithId(JsonNode node,String id) throws JsonProcessingException {
        ((ObjectNode) node).put("@id", "/"+ TYPE_NAME+"/"+id);
        return node;
    }

    private String route(String id) {
        return "/"+ C.TYPE_NAME+"/"+id;
    }

    @Test
    public void respondWithEnrichedJSONonPost() throws IOException {

        assertEquals(
                postJSON(route("1"), sampleJson("b")),
                enrichWithId(sampleJson("b"), "1"));

    }

    @Test
    public void storeAndRetrieveOneDocument() throws IOException {

        postJSON(route("1"),sampleJson("b"));

        assertEquals(
                getJSON(route("1")),
                enrichWithId(sampleJson("b"), "1"));
    }

    @Test
    public void storeAndRetrieveMoreThanOneDocument() throws IOException {

        postJSON(route("1"),sampleJson("b"));
        postJSON(route("2"),sampleJson("a"));

        assertEquals(
                getJSON(route("1")),
                enrichWithId(sampleJson("b"), "1"));
    }
}

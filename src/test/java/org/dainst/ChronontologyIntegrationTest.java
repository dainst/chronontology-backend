package org.dainst;

import static org.dainst.C.*;

import static org.testng.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * @author Daniel M. de Oliveira
 */
public class ChronontologyIntegrationTest extends IntegrationTestBase {

    @Test
    public void storeAndRetrieveOneDocument() throws IOException {

        post(route("1"), sampleJson("b"));
        jsonAssertEquals(
                get(route("1")),
                sampleJson("b"));
    }

    @Test
    public void storeAndRetrieveMoreThanOneDocument() throws IOException {

        post(route("1"), sampleJson("b"));
        post(route("2"), sampleJson("a"));
        jsonAssertEquals(
                get(route("1")),
                sampleJson("b"));
    }


    @Test
    public void changeADocument() throws IOException, InterruptedException {

        post(route("1"), sampleJson("a"));
        put(route("1"), sampleJson("b"));
        jsonAssertEquals(
                get(route("1")),
                sampleJson("b")); // check also with direct = true
    }


    @Test
    public void documentDoesNotExistBeforePut() throws IOException {

        put(route("1"), sampleJson("a"));
        jsonAssertEquals(
                get(route("1")),
                sampleJson("a"));
    }


    @Test
    public void documentExistsBeforePost() throws IOException {

        post(route("1"), sampleJson("a"));
        post(route("1"), sampleJson("b"));

        jsonAssertEquals(
                get(route("1")),
                sampleJson("a"));
    }


    @Test
    public void retrieveDocumentsFromDifferentSources() throws IOException {

        connectDatastore.put(C.TYPE_NAME,"1",sampleJson("a"));
        mainDatastore.put(C.TYPE_NAME,"1",sampleJson("b"));

        jsonAssertEquals(
                get(route("1")),
                sampleJson("a"));
        assertEquals(
                get(route("1") + "?direct=true"),
                sampleJson("b"));
    }
}

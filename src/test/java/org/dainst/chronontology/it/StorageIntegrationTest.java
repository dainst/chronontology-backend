package org.dainst.chronontology.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.dainst.chronontology.TestConstants;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.dainst.chronontology.JsonTestUtils.jsonAssertEquals;
import static org.testng.Assert.assertEquals;



/**
 * @author Daniel M. de Oliveira
 */
public class StorageIntegrationTest extends JsonIntegrationTestBase {

    @Test
    public void getNonExistingDocument() {
        assertEquals(
                client.get(TYPE_ROUTE+"1"),
                null);
    }

    @Test
     public void postUnauthorized() {
        client.authenticate(TestConstants.USER_NAME_ADMIN,"wrong");
        client.post(TYPE_ROUTE, sampleJson("b"));
        assertEquals(
                client.get(TYPE_ROUTE+"1"),
                null);
    }

    @Test
    public void putUnauthorized() {
        client.authenticate(TestConstants.USER_NAME_ADMIN,"wrong");
        client.put(TYPE_ROUTE+"1", sampleJson("b"));
        assertEquals(
                client.get(TYPE_ROUTE+"1"),
                null);
    }

    @Test
    public void storeAndRetrieveOneDocument() {

        String id= idOf(client.post(TYPE_ROUTE, sampleJson("b")));
        jsonAssertEquals(
                client.get(id),
                sampleJson("b"));
    }

    @Test
    public void storeAndRetrieveMoreThanOneDocument() {

        client.post(TYPE_ROUTE, sampleJson("b"));
        String id= idOf(client.post(TYPE_ROUTE, sampleJson("a")));

        jsonAssertEquals(
                client.get(id),
                sampleJson("a"));
    }


    @Test
    public void changeADocument() {

        String id= idOf(client.post(TYPE_ROUTE, sampleJson("a")));
        client.put(id, sampleJson("b"));
        jsonAssertEquals(
                client.get(id),
                sampleJson("b")); // check also with direct = true
    }


    @Test
    public void documentDoesNotExistBeforePut() throws IOException {

        client.put(TYPE_ROUTE+"1", sampleJson("a"));
        jsonAssertEquals(
                client.get(TYPE_ROUTE+"1"),
                sampleJson("a"));
    }


    @Test
    public void retrieveDocumentsFromDifferentSources() throws IOException {

        connectDatastore.put(TestConstants.TEST_TYPE,"1",sampleJson("a","1"));
        mainDatastore.put(TestConstants.TEST_TYPE,"1",sampleJson("b","1"));

        jsonAssertEquals(
                client.get(TYPE_ROUTE+"1"),
                sampleJson("a"));
        jsonAssertEquals(
                client.get(TYPE_ROUTE+"1" + "?direct=true"),
                sampleJson("b"));
    }

    @Test
    public void respondWithEnrichedJSONonPost() throws IOException, JSONException {
        JsonNode n= client.post(TYPE_ROUTE, sampleJson("b"));
        String id= idOf(n);

        jsonAssertEquals(
                n,
                sampleJson("b",id));
    }
}

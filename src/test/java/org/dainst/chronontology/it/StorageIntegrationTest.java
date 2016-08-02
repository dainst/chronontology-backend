package org.dainst.chronontology.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.dainst.chronontology.JsonTestUtils;
import org.dainst.chronontology.TestConstants;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.dainst.chronontology.JsonTestUtils.jsonAssertEquals;
import static org.dainst.chronontology.JsonTestUtils.sampleDocument;
import static org.testng.Assert.assertEquals;


/**
 * @author Daniel de Oliveira
 */
public class StorageIntegrationTest extends IntegrationTest {

    @Test
    public void getNonExistingDocument() {
        assertEquals(
                client.get(TYPE_ROUTE+"1"),
                null);
    }

    @Test
     public void postUnauthorized() {
        client.authenticate(TestConstants.USER_NAME_ADMIN,"wrong");
        client.post(TYPE_ROUTE, JsonTestUtils.sampleDocument("b"));
        assertEquals(
                client.get(TYPE_ROUTE+"1"),
                null);
    }

    @Test
    public void putUnauthorized() {
        client.authenticate(TestConstants.USER_NAME_ADMIN,"wrong");
        client.put(TYPE_ROUTE+"1", JsonTestUtils.sampleDocument("b"));
        assertEquals(
                client.get(TYPE_ROUTE+"1"),
                null);
    }

    @Test
    public void storeAndRetrieveOneDocument() {
        System.out.println(client.post(TYPE_ROUTE, JsonTestUtils.sampleDocument("b")));

        String id= idOf(client.post(TYPE_ROUTE, JsonTestUtils.sampleDocument("b")));
        jsonAssertEquals(
                client.get(TYPE_ROUTE+id),
                JsonTestUtils.sampleDocument("b"));
    }

    @Test
    public void storeAndRetrieveMoreThanOneDocument() {

        client.post(TYPE_ROUTE, JsonTestUtils.sampleDocument("b"));
        String id= idOf(client.post(TYPE_ROUTE, JsonTestUtils.sampleDocument("a")));

        jsonAssertEquals(
                client.get(TYPE_ROUTE+id),
                JsonTestUtils.sampleDocument("a"));
    }


    @Test
    public void changeADocument() {

        String id= idOf(client.post(TYPE_ROUTE, JsonTestUtils.sampleDocument("a")));
        client.put(TYPE_ROUTE+id, JsonTestUtils.sampleDocument("b"));
        jsonAssertEquals(
                client.get(TYPE_ROUTE+id),
                JsonTestUtils.sampleDocument("b")); // check also with direct = true
    }


    @Test
    public void documentDoesNotExistBeforePut() throws IOException {

        client.put(TYPE_ROUTE+"1", JsonTestUtils.sampleDocument("a"));
        jsonAssertEquals(
                client.get(TYPE_ROUTE+"1"),
                JsonTestUtils.sampleDocument("a"));
    }


    @Test
    public void retrieveDocumentsFromDifferentSources() throws IOException {

        connectDatastore.put(TestConstants.TEST_TYPE,"1", sampleDocument("a","1"));
        mainDatastore.put(TestConstants.TEST_TYPE,"1", sampleDocument("b","1"));

        jsonAssertEquals(
                client.get(TYPE_ROUTE+"1"),
                JsonTestUtils.sampleDocument("a"));
        jsonAssertEquals(
                client.get(TYPE_ROUTE+"1" + "?direct=true"),
                JsonTestUtils.sampleDocument("b"));
    }

    @Test
    public void fetchSpecificVersions() throws IOException {

        mainDatastore.put(TestConstants.TEST_TYPE,"1", sampleDocument("a","1"));
        mainDatastore.put(TestConstants.TEST_TYPE,"1", sampleDocument("b","1"));

        jsonAssertEquals(
                client.get(TYPE_ROUTE+"1" + "?version=1"),
                JsonTestUtils.sampleDocument("a"));
        jsonAssertEquals(
                client.get(TYPE_ROUTE+"1" + "?version=2"),
                JsonTestUtils.sampleDocument("b"));
    }


    @Test
    public void respondWithEnrichedJSONonPost() throws IOException, JSONException {
        JsonNode n= client.post(TYPE_ROUTE, JsonTestUtils.sampleDocument("b"));
        String id= idOf(n);

        jsonAssertEquals(
                n,
                sampleDocument("b",id));
    }
}

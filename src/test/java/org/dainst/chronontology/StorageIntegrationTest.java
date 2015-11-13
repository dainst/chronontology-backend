package org.dainst.chronontology;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * @author Daniel M. de Oliveira
 */
public class StorageIntegrationTest extends IntegrationTestBase {

    @Test
    public void getNonExistingDocument() {
        assertEquals(
                client.get(route("1")),
                null);
    }

    @Test
     public void postUnauthorized() {
        client.authenticate(USER_NAME,"wrong");
        client.post(route("1"), sampleJson("b"));
        assertEquals(
                client.get(route("1")),
                null);
    }

    @Test
    public void putUnauthorized() {
        client.authenticate(USER_NAME,"wrong");
        client.put(route("1"), sampleJson("b"));
        assertEquals(
                client.get(route("1")),
                null);
    }

    @Test
    public void storeAndRetrieveOneDocument() {

        client.post(route("1"), sampleJson("b"));
        jsonAssertEquals(
                client.get(route("1")),
                sampleJson("b"));
    }

    @Test
    public void storeAndRetrieveMoreThanOneDocument() {

        client.post(route("1"), sampleJson("b"));
        client.post(route("2"), sampleJson("a"));
        jsonAssertEquals(
                client.get(route("1")),
                sampleJson("b"));
    }


    @Test
    public void changeADocument() {

        client.post(route("1"), sampleJson("a"));
        client.put(route("1"), sampleJson("b"));
        jsonAssertEquals(
                client.get(route("1")),
                sampleJson("b")); // check also with direct = true
    }


    @Test
    public void documentDoesNotExistBeforePut() throws IOException {

        client.put(route("1"), sampleJson("a"));
        jsonAssertEquals(
                client.get(route("1")),
                sampleJson("a"));
    }


    @Test
    public void documentExistsBeforePost() throws IOException {

        client.post(route("1"), sampleJson("a"));
        client.post(route("1"), sampleJson("b"));

        jsonAssertEquals(
                client.get(route("1")),
                sampleJson("a"));
    }


    @Test
    public void retrieveDocumentsFromDifferentSources() throws IOException {

        connectDatastore.put(TYPE_NAME,"1",sampleJson("a"));
        mainDatastore.put(TYPE_NAME,"1",sampleJson("b"));

        jsonAssertEquals(
                client.get(route("1")),
                sampleJson("a"));
        assertEquals(
                client.get(route("1") + "?direct=true"),
                sampleJson("b"));
    }
}

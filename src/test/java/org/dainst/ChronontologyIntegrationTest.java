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

    private JsonNode searchResultJson(String id, String sampleFieldValue) throws IOException {
        return new ObjectMapper().readTree
                ("{\"results\":[{\"a\":\""+sampleFieldValue+"\",\"@id\":\"/period/"+id+"\"}]}");
    }

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
    public void itemDoesNotExistBeforePut() throws IOException {

        put(route("1"), sampleJson("a"));
        jsonAssertEquals(
                get(route("1")),
                sampleJson("a"));
    }

    /*
    @Test
    public void itemExistsBeforePost() throws IOException {

        post(route("1"), sampleJson("a"));

        post(route("1"), sampleJson("b"));

        jsonAssertEquals(
                get(route("1")),
                addId(sampleJson("a"), "1"));
    }*/


    @Test
    public void retrieveDocumentsFromDifferentSources() throws IOException {

        connectDatastore.put("1",sampleJson("a"));
        mainDatastore.put("1",sampleJson("b"));

        jsonAssertEquals(
                get(route("1")),
                sampleJson("a"));
        assertEquals(
                get(route("1") + "?direct=true"),
                sampleJson("b"));
    }



    @Test
    public void matchQueryTermWithSlashes() throws IOException, InterruptedException {

        post(route("1"), sampleJson("/period/2"));
        jsonAssertEquals(
                get(route("") + "?q=a:/period/2"),
                searchResultJson("1", "/period/2")
        );
    }

    @Test
    public void matchExactlyTheOneTermWithSlashes() throws IOException, InterruptedException {

        post(route("1"), sampleJson("/period/2"));
        post(route("2"), sampleJson("/period/3"));
        jsonAssertEquals(
                get(route("") + "?q=a:/period/2"),
                searchResultJson("1", "/period/2")
        );
    }

    @Test
    public void matchQueryTermWithUrlEncodedSlashes() throws IOException, InterruptedException {

        post(route("1"), sampleJson("/period/2"));
        jsonAssertEquals(
                get(route("") + "?q=a:%22%2Fperiod%2F2%22"),
                searchResultJson("1", "/period/2")
        );
        post(route("2"), sampleJson("/period/1"));
        jsonAssertEquals(
                get(route("") + "?q=a:%2Fperiod%2F1"),
                searchResultJson("2", "/period/1")
        );
    }

    @Test
    public void searchInAllFields() throws IOException, InterruptedException {

        post(route("1"), sampleJson("abc"));
        post(route("2"), sampleJson("def"));
        jsonAssertEquals(
                get(route("") + "?q=def"),
                searchResultJson("2", "def")
        );
    }
}

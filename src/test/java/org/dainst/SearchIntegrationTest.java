package org.dainst;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * @author Daniel M. de Oliveira
 */
public class SearchIntegrationTest extends IntegrationTestBase {

    private JsonNode searchResultJson(String id, String sampleFieldValue) throws IOException {
        return new ObjectMapper().readTree
                ("{\"results\":[{\"a\":\""+sampleFieldValue+"\",\"@id\":\"/period/"+id+"\"}]}");
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

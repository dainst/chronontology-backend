package org.dainst.chron;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.fail;

/**
 * @author Daniel M. de Oliveira
 */
public class SearchIntegrationTest extends IntegrationTestBase {



    private JsonNode searchResultJson(String id, String sampleFieldValue) throws IOException {
        return new ObjectMapper().readTree
                ("{\"results\":[{\"a\":\""+sampleFieldValue+"\",\"@id\":\"/period/"+id+"\"}]}");
    }

    private void refreshES() {
        RequestBody body = RequestBody.create(JSON, "{}");
        Request.Builder b = new Request.Builder()
                .url(ES_URL+ "/" + INDEX_NAME + "/_refresh").post(body);
        try {
            ok.newCall(b.build()).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void matchQueryTermWithUrlEncodedSlashes() throws IOException, InterruptedException {

        client.post(route("1"), sampleJson("/period/2"));

        refreshES();
        jsonAssertEquals(
                client.get(route("") + "?q=a:%22%2Fperiod%2F2%22"),
                searchResultJson("1", "/period/2")
        );


        client.post(route("2"), sampleJson("/period/1"));
        refreshES();
        jsonAssertEquals(
                client.get(route("") + "?q=a:%22%2Fperiod%2F1%22"),
                searchResultJson("2", "/period/1")
        );
    }

    @Test
    public void searchInAllFields() throws IOException, InterruptedException {

        client.post(route("1"), sampleJson("abc"));
        client.post(route("2"), sampleJson("def"));

        refreshES();
        jsonAssertEquals(
                client.get(route("") + "?q=def"),
                searchResultJson("2", "def")
        );
    }

    private void assertTwoResultsAreFound(JsonNode searchResult)  {

        try {
            JSONCompareResult r  = JSONCompare.compareJSON(
                    "{\"results\":[{\"@id\":\"/period/3\"},{\"@id\":\"/period/2\"}]}",
                    searchResult.toString(), JSONCompareMode.LENIENT);

            if (r.failed()) fail(r.getMessage());
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void searchWithoutSizeRestriction() throws IOException, InterruptedException {

        client.post(route("1"), sampleJson("a"));
        client.post(route("2"), sampleJson("b"));
        client.post(route("3"), sampleJson("b"));

        refreshES();
        assertTwoResultsAreFound(client.get(route("") + "?q=a:b"));
    }

    @Test
    public void restrictedSizeSearch() throws IOException, InterruptedException {

        client.post(route("1"), sampleJson("a"));
        client.post(route("2"), sampleJson("b"));
        client.post(route("3"), sampleJson("b"));

        refreshES();
        jsonAssertEquals(
                client.get(route("") + "?q=a:b&size=1"),
                json("{\"results\":[{\"a\":\"b\"}]}"));


    }

    /* TODO replace test by test for error code in statuscodesintegrationtest
    @Test
    public void restrictedSizeSearchWrongNrFormat() throws IOException, InterruptedException {

        post(route("1"), sampleJson("a"));
        post(route("2"), sampleJson("b"));
        post(route("3"), sampleJson("b"));

        Thread.sleep(1000);

        assertTwoResultsAreFound(get(route("") + "?q=a:b&size=A"));
    }*/

    @Test
    public void restrictedSizeSearchSizeIsZero() throws IOException, InterruptedException {

        client.post(route("1"), sampleJson("a"));
        client.post(route("2"), sampleJson("b"));
        client.post(route("3"), sampleJson("b"));

        refreshES();
        jsonAssertEquals(
                client.get(route("") + "?q=a:b&size=0"),
                json("{\"results\":[]}"));
    }

    @Test
    public void restrictedSizeSearchSizeLowerThanZero() throws IOException, InterruptedException {

        client.post(route("1"), sampleJson("a"));
        client.post(route("2"), sampleJson("b"));
        client.post(route("3"), sampleJson("b"));

        refreshES();
        assertTwoResultsAreFound(client.get(route("") + "?q=a:b&size=-1"));
    }
}

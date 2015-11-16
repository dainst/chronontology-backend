package org.dainst.chronontology;

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
import static org.dainst.chronontology.TestUtils.*;

/**
 * @author Daniel M. de Oliveira
 */
public class SearchIntegrationTest extends IntegrationTestBase {



    private JsonNode searchResultJson(String id, String sampleFieldValue) throws IOException {
        return new ObjectMapper().readTree
                ("{\"results\":[{\"a\":\""+sampleFieldValue+"\",\"@id\":\""+id+"\"}]}");
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

        String id= idOf(client.post(TYPE_ROUTE, sampleJson("/period/2")));

        refreshES();
        jsonAssertEquals(
                client.get(TYPE_ROUTE + "?q=a:%22%2Fperiod%2F2%22"),
                searchResultJson(id, "/period/2")
        );


        String id2= idOf(client.post(TYPE_ROUTE, sampleJson("/period/1")));
        refreshES();
        jsonAssertEquals(
                client.get(TYPE_ROUTE + "?q=a:%22%2Fperiod%2F1%22"),
                searchResultJson(id2, "/period/1")
        );
    }

    @Test
    public void searchInAllFields() throws IOException, InterruptedException {

        client.post(TYPE_ROUTE, sampleJson("abc"));
        String id= idOf(client.post(TYPE_ROUTE, sampleJson("def")));

        refreshES();
        jsonAssertEquals(
                client.get(TYPE_ROUTE + "?q=def"),
                searchResultJson(id, "def")
        );
    }

    private void assertTwoResultsAreFound(JsonNode searchResult,String id1,String id2)  {

        try {
            JSONCompareResult r  = JSONCompare.compareJSON(
                    "{\"results\":[{\"@id\":\""+id1+"\"},{\"@id\":\""+id2+"\"}]}",
                    searchResult.toString(), JSONCompareMode.LENIENT);

            if (r.failed()) fail(r.getMessage());
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void searchWithoutSizeRestriction() throws IOException, InterruptedException {

        client.post(TYPE_ROUTE, sampleJson("a"));
        String id1= idOf(client.post(TYPE_ROUTE, sampleJson("b")));
        String id2= idOf(client.post(TYPE_ROUTE, sampleJson("b")));

        refreshES();
        assertTwoResultsAreFound(client.get(TYPE_ROUTE + "?q=a:b"),id1,id2);
    }

    @Test
    public void restrictedSizeSearch() throws IOException, InterruptedException {

        client.post(TYPE_ROUTE, sampleJson("a"));
        client.post(TYPE_ROUTE, sampleJson("b"));
        client.post(TYPE_ROUTE, sampleJson("b"));

        refreshES();
        jsonAssertEquals(
                client.get(TYPE_ROUTE + "?q=a:b&size=1"),
                json("{\"results\":[{\"a\":\"b\"}]}"));


    }

    /* TODO
    @Test
    public void badSearchRequest() throws IOException, InterruptedException {

        post(route("1"), sampleJson("a"));
        post(route("2"), sampleJson("b"));
        post(route("3"), sampleJson("b"));

        Thread.sleep(1000);

        assertTwoResultsAreFound(get(route("") + "?q=a:b&size=A")); // This A causes trouble.
    }*/

    @Test
    public void restrictedSizeSearchSizeIsZero() throws IOException, InterruptedException {

        client.post(TYPE_ROUTE, sampleJson("a"));
        client.post(TYPE_ROUTE, sampleJson("b"));
        client.post(TYPE_ROUTE, sampleJson("b"));

        refreshES();
        jsonAssertEquals(
                client.get(TYPE_ROUTE + "?q=a:b&size=0"),
                json("{\"results\":[]}"));
    }

    @Test
    public void restrictedSizeSearchSizeLowerThanZero() throws IOException, InterruptedException {

        client.post(TYPE_ROUTE, sampleJson("a"));
        String id1= idOf(client.post(TYPE_ROUTE, sampleJson("b")));
        String id2= idOf(client.post(TYPE_ROUTE, sampleJson("b")));

        refreshES();
        assertTwoResultsAreFound(client.get(TYPE_ROUTE + "?q=a:b&size=-1"),id1,id2);
    }
}

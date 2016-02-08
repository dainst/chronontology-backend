package org.dainst.chronontology;

import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import org.dainst.chronontology.store.ESServerTestUtil;
import org.dainst.chronontology.util.Results;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.dainst.chronontology.JsonTestUtils.json;
import static org.dainst.chronontology.JsonTestUtils.jsonAssertEquals;
import static org.testng.Assert.fail;

/**
 * @author Daniel M. de Oliveira
 */
public class SearchIntegrationTest extends JsonIntegrationTestBase {



    private JsonNode searchResultJson(String id, String sampleFieldValue) throws IOException {
        return results().add(
                json("{\"a\":\"" + sampleFieldValue+"\",\"@id\":\""+id+"\"}")).j();
    }

    private void refreshES() {
        RequestBody body = RequestBody.create(JSON, "{}");
        Request.Builder b = new Request.Builder()
                .url(ESServerTestUtil.getUrl()+ "/" + ESClientTestUtil.getIndexName() + "/_refresh").post(body);
        try {
            ok.newCall(b.build()).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String identifier(String suffix) {
        return TYPE_ROUTE+suffix;
    }

    private Results results() {
        return new Results("results");
    }

    @Test
    public void matchQueryTermWithUrlEncodedSlashes() throws IOException, InterruptedException {

        String id= idOf(client.post(TYPE_ROUTE, sampleJson(identifier("2"))));

        refreshES();
        jsonAssertEquals(
                client.get(TYPE_ROUTE + "?q=a:%22%2Fperiod%2F2%22"),
                searchResultJson(id, identifier("2"))
        );


        String id2= idOf(client.post(TYPE_ROUTE, sampleJson(identifier("1"))));
        refreshES();
        jsonAssertEquals(
                client.get(TYPE_ROUTE + "?q=a:%22%2Fperiod%2F1%22"),
                searchResultJson(id2, identifier("1"))
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
            JSONCompareResult r  = null;
            try {
                r = JSONCompare.compareJSON(
                        results().add(json("{\"@id\" : \""+id1+"\"}")).add(json("{\"@id\":\""+id2+"\"}")).j().toString(),
                        searchResult.toString(), JSONCompareMode.LENIENT);
            } catch (IOException e) {}

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
                results().add(sampleJson("b")).j());
    }

    @Test
    public void restrictedSizeSearchSizeIsZero() throws IOException, InterruptedException {

        client.post(TYPE_ROUTE, sampleJson("a"));
        client.post(TYPE_ROUTE, sampleJson("b"));
        client.post(TYPE_ROUTE, sampleJson("b"));

        refreshES();
        jsonAssertEquals(
                client.get(TYPE_ROUTE + "?q=a:b&size=0"),
                results().j());
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

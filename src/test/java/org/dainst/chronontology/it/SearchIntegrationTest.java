package org.dainst.chronontology.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.dainst.chronontology.JsonTestUtils;
import org.dainst.chronontology.handler.model.Results;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.dainst.chronontology.JsonTestUtils.jsonAssertEquals;
import static org.dainst.chronontology.JsonTestUtils.*;
import static org.dainst.chronontology.it.ESClientTestUtil.refreshES;
import static org.dainst.chronontology.util.JsonUtils.json;
import static org.testng.Assert.fail;

/**
 * @author Daniel M. de Oliveira
 */
public class SearchIntegrationTest extends IntegrationTest {



    private JsonNode searchResultJson(String id, String sampleFieldValue) throws IOException {
        return results().add(
                json("{\"a\":\"" + sampleFieldValue+"\",\"@id\":\""+id+"\"}")).j();
    }

    private String identifier(String suffix) {
        return TYPE_ROUTE+suffix;
    }

    private Results results() {
        return new Results("results");
    }

    @Test
    public void matchQueryTermWithUrlEncodedSlashes() throws IOException, InterruptedException {

        String id= idOf(client.post(TYPE_ROUTE, JsonTestUtils.sampleDocument(identifier("2"))));

        refreshES();
        jsonAssertEquals(
                client.get(TYPE_ROUTE + "?q=a:%22%2Fperiod%2F2%22"),
                searchResultJson(id, identifier("2"))
        );


        String id2= idOf(client.post(TYPE_ROUTE, JsonTestUtils.sampleDocument(identifier("1"))));
        refreshES();
        jsonAssertEquals(
                client.get(TYPE_ROUTE + "?q=a:%22%2Fperiod%2F1%22"),
                searchResultJson(id2, identifier("1"))
        );
    }

    @Test
    public void searchInAllFields() throws IOException, InterruptedException {

        client.post(TYPE_ROUTE, JsonTestUtils.sampleDocument("abc"));
        String id= idOf(client.post(TYPE_ROUTE, JsonTestUtils.sampleDocument("def")));

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

        client.post(TYPE_ROUTE, JsonTestUtils.sampleDocument("a"));
        String id1= idOf(client.post(TYPE_ROUTE, JsonTestUtils.sampleDocument("b")));
        String id2= idOf(client.post(TYPE_ROUTE, JsonTestUtils.sampleDocument("b")));

        refreshES();
        assertTwoResultsAreFound(client.get(TYPE_ROUTE + "?q=a:b"),id1,id2);
    }

    @Test
    public void restrictedSizeSearch() throws IOException, InterruptedException {

        client.post(TYPE_ROUTE, JsonTestUtils.sampleDocument("b"));
        client.post(TYPE_ROUTE, JsonTestUtils.sampleDocument("b"));
        String id=idOf(client.post(TYPE_ROUTE, JsonTestUtils.sampleDocument("a")));


        refreshES();
        jsonAssertEquals(
                client.get(TYPE_ROUTE + "?q=a:a&size=1"),
                results().add(sampleDocument("a",id)).j());
    }

    @Test
    public void restrictedSizeSearchSizeIsZero() throws IOException, InterruptedException {

        client.post(TYPE_ROUTE, JsonTestUtils.sampleDocument("a"));
        client.post(TYPE_ROUTE, JsonTestUtils.sampleDocument("b"));
        client.post(TYPE_ROUTE, JsonTestUtils.sampleDocument("b"));

        refreshES();
        jsonAssertEquals(
                client.get(TYPE_ROUTE + "?q=a:b&size=0"),
                results().j());
    }

    @Test
    public void restrictedSizeSearchSizeLowerThanZero() throws IOException, InterruptedException {

        client.post(TYPE_ROUTE, JsonTestUtils.sampleDocument("a"));
        String id1= idOf(client.post(TYPE_ROUTE, JsonTestUtils.sampleDocument("b")));
        String id2= idOf(client.post(TYPE_ROUTE, JsonTestUtils.sampleDocument("b")));

        refreshES();
        assertTwoResultsAreFound(client.get(TYPE_ROUTE + "?q=a:b&size=-1"),id1,id2);
    }
}

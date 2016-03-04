package org.dainst.chronontology.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.dainst.chronontology.JsonTestUtils;
import org.dainst.chronontology.handler.model.Document;
import org.dainst.chronontology.handler.model.Results;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.dainst.chronontology.JsonTestUtils.*;
import static org.dainst.chronontology.it.ESClientTestUtil.refreshES;
import static org.dainst.chronontology.util.JsonUtils.json;
import static org.testng.Assert.fail;

/**
 * @author Daniel M. de Oliveira
 */
public class SearchIntegrationTest extends IntegrationTest {

    private JsonNode searchResultJson(String id, String sampleFieldValue) throws IOException {
        return results().add(sampleDocument(sampleFieldValue,id)).j();
    }

    private Results results() {
        return new Results("results");
    }

    private void assertResultsAreFound(JsonNode searchResult, List<String> ids)  {

        Results expected= results();
        for (String id:ids) {
            try {
                expected.add(json("{\"@id\" : \""+id+"\"}"));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        try {
            JSONCompareResult r  = null;
            r = JSONCompare.compareJSON(
                    expected.toString(),
                    searchResult.toString(), JSONCompareMode.LENIENT);

            if (r.failed()) fail(r.getMessage());
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }

    private List<String> postSampleData(String... sampleData) {
        List<String> ids= new ArrayList<String>();
        for (String sample:sampleData) {
            ids.add(idOf(client.post(TYPE_ROUTE, sampleDocument(sample))));
        }
        refreshES();
        return ids;
    }

    @Test
    public void matchQueryTermWithUrlEncodedSlashes() throws IOException, InterruptedException {

        List<String> ids= postSampleData("/type/1","/type/2");

        jsonAssertEquals(
                client.get(TYPE_ROUTE + "?q="+Document.RESOURCE+":%22%2Ftype%2F1%22"),
                searchResultJson(ids.get(0), "/type/1")
        );
    }

    @Test
    public void searchInAllFields() throws IOException, InterruptedException {

        List<String> ids= postSampleData("abc","def");

        jsonAssertEquals(
                client.get(TYPE_ROUTE + "?q=def"),
                searchResultJson(ids.get(1), "def")
        );
    }

    @Test
    public void searchWithoutSizeRestriction() throws IOException, InterruptedException {

        List<String> ids= postSampleData("b","b");

        assertResultsAreFound(client.get(TYPE_ROUTE + "?q="+ Document.RESOURCE+":b"),ids);
    }

    @Test
    public void restrictedSizeSearchSizeLowerThanZero() throws IOException, InterruptedException {

        List<String> ids= postSampleData("b","b");

        assertResultsAreFound(client.get(TYPE_ROUTE + "?q="+Document.RESOURCE+":b&size=-1"),ids);
    }

    @Test
    public void restrictedSizeSearch() throws IOException, InterruptedException {

        List<String> ids= postSampleData("b","b","a");

        jsonAssertEquals(
                client.get(TYPE_ROUTE + "?q="+Document.RESOURCE+":a&size=1"),
                results().add(sampleDocument("a",ids.get(2))).j());
    }

    @Test
    public void restrictedSizeSearchSizeIsZero() throws IOException, InterruptedException {

        postSampleData("b","b","a");

        jsonAssertEquals(
                client.get(TYPE_ROUTE + "?q=a:b&size=0"),
                results().j());
    }


}

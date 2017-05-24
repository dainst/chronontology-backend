package org.dainst.chronontology;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.dainst.chronontology.handler.model.Document;
import org.dainst.chronontology.handler.model.Results;
import org.dainst.chronontology.util.JsonUtils;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import java.util.List;

import static org.dainst.chronontology.util.JsonUtils.json;
import static org.testng.Assert.fail;

/**
 * @author Daniel de Oliveira
 */
public class JsonTestUtils {

    public static void jsonAssertEquals(final JsonNode actual,final JsonNode expected) {
        try {
            JSONAssert.assertEquals(
                    expected.toString(),
                    actual.toString(), false);
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }

    public static JsonNode sampleDocument(final String sampleFieldValue) {
        return sampleDocument(sampleFieldValue,null,null);
    }

    public static JsonNode sampleDocument(final String sampleFieldValue, String id) {
        return sampleDocument(sampleFieldValue,id,null);
    }

    public static JsonNode sampleDocument(final String sampleFieldValue, String id, String dataset) {
        JsonNode json= null;
        json = JsonUtils.json();
        ((ObjectNode)json).put(Document.RESOURCE,new ObjectMapper().createObjectNode());
        ((ObjectNode)json.get(Document.RESOURCE)).put("sampleField",sampleFieldValue);
        if (id!=null) {
            ((ObjectNode)json.get(Document.RESOURCE)).put(Document.ID,id);
            ((ObjectNode)json.get(Document.RESOURCE)).put(Document.TYPE,TestConstants.TEST_TYPE);
        }
        if (dataset!=null)
            ((ObjectNode)json).put(Document.DATASET,dataset);
        ((ObjectNode)json).put(Document.BOOST, 1.0);
        return json;
    }

    /**
     * Takes a <code>searchResult</code> as obtained from the search endpoint and
     * tests if it contains results for any of the specified ids.
     *
     * For example, if <code>ids</code> is {1,2}, <code>searchResult</code> set must contain
     * two nodes with at least the @id fields. All other fields are optional, but
     * it must contain the two nodes.
     *
     * <pre>
     * {
     *     "results" : [
     *          {
     *              "@id" : "1"
     *          },
     *          {
     *              "@id" : "2"
     *          }
     *     ]
     * }
     * </pre>
     *
     * If there is at least one node missing, the method fails with
     * testng's fail().
     *
     * @param searchResult
     * @param ids
     */
    public static void assertResultsAreFound(JsonNode searchResult, List<String> ids)  {

        Results expected= new Results("results");
        for (String id:ids) {
            expected.add(json("{\"resource\":{\""+Document.ID+"\" : \""+id+"\"}}"));
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
}

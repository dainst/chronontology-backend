package org.dainst.chronontology;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.dainst.chronontology.handler.model.Document;
import org.dainst.chronontology.handler.model.Results;
import org.dainst.chronontology.util.JsonUtils;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import java.io.IOException;
import java.util.List;

import static org.dainst.chronontology.util.JsonUtils.json;
import static org.testng.Assert.fail;

/**
 * @author Daniel M. de Oliveira
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
        ((ObjectNode)json).put(Document.RESOURCE,sampleFieldValue);
        if (id!=null)
            ((ObjectNode)json).put(Document.ID,id);
        if (dataset!=null)
            ((ObjectNode)json).put(Document.DATASET,dataset);
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
            expected.add(json("{\""+Document.ID+"\" : \""+id+"\"}"));
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

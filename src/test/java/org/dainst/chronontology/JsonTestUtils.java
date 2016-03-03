package org.dainst.chronontology;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.dainst.chronontology.util.JsonUtils;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

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
        return sampleDocument(sampleFieldValue,null);
    }

    public static JsonNode sampleDocument(final String sampleFieldValue, String id) {
        JsonNode json= null;
        json = JsonUtils.json();
        ((ObjectNode)json).put("a",sampleFieldValue);
        if (id!=null)
            ((ObjectNode)json).put("@id",id);
        return json;
    }
}

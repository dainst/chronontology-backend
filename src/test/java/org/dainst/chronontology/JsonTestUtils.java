package org.dainst.chronontology;

import com.fasterxml.jackson.databind.JsonNode;
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
}

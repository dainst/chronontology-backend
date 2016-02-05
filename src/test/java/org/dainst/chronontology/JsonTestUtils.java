package org.dainst.chronontology;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;

import static org.testng.Assert.fail;

/**
 * @author Daniel M. de Oliveira
 */
public class JsonTestUtils {

    public static JsonNode json() {
        return new ObjectMapper().createObjectNode();
    }

    public static void jsonAssertEquals(final JsonNode actual,final JsonNode expected) {
        try {
            JSONAssert.assertEquals(
                    expected.toString(),
                    actual.toString(), false);
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }

    public static JsonNode json(String s) throws IOException {
        return new ObjectMapper().readTree(s);
    }
}

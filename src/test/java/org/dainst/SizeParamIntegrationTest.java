package org.dainst;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class SizeParamIntegrationTest extends IntegrationTestBase{

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
    public void searchWithoutSizeRestriction() throws IOException {

        post(route("1"), sampleJson("a"));
        post(route("2"), sampleJson("b"));
        post(route("3"), sampleJson("b"));

        assertTwoResultsAreFound(get(route("") + "?q=a:b"));
    }

    @Test
    public void restrictedSizeSearch() throws IOException {

        post(route("1"), sampleJson("a"));
        post(route("2"), sampleJson("b"));
        post(route("3"), sampleJson("b"));

        jsonAssertEquals(
                get(route("") + "?q=a:b&size=1"),
                jsonNode("{\"results\":[{\"a\":\"b\"}]}"));


    }

    @Test
    public void restrictedSizeSearchWrongNrFormat() throws IOException{

        post(route("1"), sampleJson("a"));
        post(route("2"), sampleJson("b"));
        post(route("3"), sampleJson("b"));

        assertTwoResultsAreFound(get(route("") + "?q=a:b&size=A"));
    }

    @Test
    public void restrictedSizeSearchSizeIsZero() throws IOException {

        post(route("1"), sampleJson("a"));
        post(route("2"), sampleJson("b"));
        post(route("3"), sampleJson("b"));

        jsonAssertEquals(
                get(route("") + "?q=a:b&size=0"),
                jsonNode("{\"results\":[]}"));
    }

    @Test
    public void restrictedSizeSearchSizeLowerThanZero() throws IOException {

        post(route("1"), sampleJson("a"));
        post(route("2"), sampleJson("b"));
        post(route("3"), sampleJson("b"));

        assertTwoResultsAreFound(get(route("") + "?q=a:b&size=-1"));
    }
}

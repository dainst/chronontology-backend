package org.dainst;

import com.fasterxml.jackson.databind.JsonNode;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.fail;

/**
 * @author Daniel M. de Oliveira
 */
public class SizeParamIntegrationTest extends IntegrationTestBase{

    private void assertTwoResultsAreFound(JsonNode searchResult) {
        if (!searchResult.toString().equals(
                "{\"results\":[{\"a\":\"b\",\"@id\":\"/period/3\"},{\"a\":\"b\",\"@id\":\"/period/2\"}]}")
                &&
                !searchResult.toString().equals(
                        "{\"results\":[{\"a\":\"b\",\"@id\":\"/period/2\"},{\"a\":\"b\",\"@id\":\"/period/3\"}]}"))
            fail();
    }

    @Test
    public void searchWithoutSizeRestriction() throws IOException, InterruptedException {

        post(route("1"), sampleJson("a"));
        post(route("2"), sampleJson("b"));
        post(route("3"), sampleJson("b"));

        assertTwoResultsAreFound(get(route("") + "?q=a:b"));
    }

    @Test
    public void restrictedSizeSearch() throws IOException, InterruptedException {

        post(route("1"), sampleJson("a"));
        post(route("2"), sampleJson("b"));
        post(route("3"), sampleJson("b"));

        JsonNode searchResult = get(route("") + "?q=a:b&size=1");
        if (!searchResult.equals(
                searchResultJson("3", "b"))
                &&
                !searchResult.equals(
                        searchResultJson("2", "b")))
            fail();
    }

    @Test
    public void restrictedSizeSearchWrongNrFormat() throws IOException, InterruptedException {

        post(route("1"), sampleJson("a"));
        post(route("2"), sampleJson("b"));
        post(route("3"), sampleJson("b"));

        assertTwoResultsAreFound(get(route("") + "?q=a:b&size=A"));
    }

    @Test
    public void restrictedSizeSearchSizeIsZero() throws IOException, InterruptedException {

        post(route("1"), sampleJson("a"));
        post(route("2"), sampleJson("b"));
        post(route("3"), sampleJson("b"));

        assertTwoResultsAreFound(get(route("") + "?q=a:b&size=0"));
    }

    @Test
    public void restrictedSizeSearchSizeLowerThanZero() throws IOException, InterruptedException {

        post(route("1"), sampleJson("a"));
        post(route("2"), sampleJson("b"));
        post(route("3"), sampleJson("b"));

        assertTwoResultsAreFound(get(route("") + "?q=a:b&size=-1"));
    }
}

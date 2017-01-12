package org.dainst.chronontology.it;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.dainst.chronontology.TestConstants;
import org.dainst.chronontology.handler.model.Document;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.dainst.chronontology.JsonTestUtils.assertResultsAreFound;
import static org.dainst.chronontology.JsonTestUtils.sampleDocument;
import static org.dainst.chronontology.it.ESClientTestUtil.refreshES;

import static org.testng.Assert.assertEquals;

/**
 * @author Daniel M. de Oliveira
 */
public class SearchIntegrationTest extends IntegrationTest {

    private List<String> postSampleData(String dataset,String... sampleData) {
        List<String> ids= new ArrayList<String>();
        for (String sample:sampleData) {
            ids.add(idOf(client.post(TYPE_ROUTE, sampleDocument(sample,null,dataset))));
        }
        refreshES();
        return ids;
    }

    @Test
    public void matchQueryTermWithUrlEncodedSlashes() throws IOException, InterruptedException {

        ESClientTestUtil.createEsTypeAndMapping();
        refreshES();

        List<String> ids = postSampleData(null,"/type/1","/type/2");
        ids.remove(1);

        assertResultsAreFound(
                client.get(TYPE_ROUTE + "?q=sampleField:\"%2Ftype%2F1\"")
                ,ids);
    }

    @Test
    public void searchInAllFields() throws IOException, InterruptedException {

        List<String> ids= postSampleData(null,"abc","def");
        ids.remove(0);
        JsonNode result = client.get(TYPE_ROUTE + "?q=def");
        assertResultsAreFound( result, ids);
    }

    @Test
    public void searchWithoutSizeRestriction() throws IOException, InterruptedException {

        List<String> ids= postSampleData(null,"b","b");

        assertResultsAreFound(
                client.get(TYPE_ROUTE + "?q=sampleField:b")
                ,ids);
    }

    @Test
    public void restrictedSizeSearchSizeLowerThanZero() throws IOException, InterruptedException {

        List<String> ids= postSampleData(null,"b","b");

        assertResultsAreFound(
                client.get(TYPE_ROUTE + "?"+Document.RESOURCE+":b&size=-1")
                ,ids);
    }

    @Test
    public void restrictedSizeSearch() throws IOException, InterruptedException {

        List<String> ids= postSampleData(null,"b","b","a");
        ids.remove(0);
        ids.remove(0);

        assertResultsAreFound(
                client.get(TYPE_ROUTE + "?q=sampleField:a&size=1")
                ,ids);
    }

    @Test
    public void restrictedSizeSearchSizeIsZero() throws IOException, InterruptedException {

        postSampleData(null,"b","b","a");

        assertResultsAreFound(
                client.get(TYPE_ROUTE + "?sampleField:b&size=0")
                ,new ArrayList<String>());
    }



    @Test
    public void adminSeesAllDatasets() throws IOException, InterruptedException {

        List<String> ids=postSampleData("dataset1","a","a","a");
        ids.addAll(postSampleData(null,"a","a","a"));

        client.authenticate(TestConstants.USER_NAME_ADMIN,TestConstants.PASS_WORD);

        assertResultsAreFound(client.get(TYPE_ROUTE + "?sampleField:a"),ids);
    }

    @Test
    public void readerWithPermissionSeesHisDatasets() throws IOException, InterruptedException {

        List<String> ids=postSampleData("dataset1","a","a","a");
        ids.addAll(postSampleData(null,"a","a","a"));

        client.authenticate(TestConstants.USER_NAME_3,TestConstants.PASS_WORD);

        assertResultsAreFound(client.get(TYPE_ROUTE + "?sampleField:a"),ids);
    }

    @Test
    public void restrictedSizeSearchWithDatasets() throws IOException, InterruptedException {

        postSampleData("dataset1","a","a","a");
        List<String> ids= postSampleData(null,"a","a","a");

        client.authenticate(null,null);

        assertResultsAreFound(client.get(TYPE_ROUTE + "?q=sampleField:a&size=3"),ids);
    }

    @Test
    public void sizeAndOffsetSearchWithDatasets() throws IOException, InterruptedException {

        postSampleData("ds1","a","a","a");
        postSampleData(null,"a","a","a");

        client.authenticate(null,null);

        assertEquals(
            ((ArrayNode) client.get(TYPE_ROUTE + "?q=sampleField:a&size=3&from=1")
                    .get("results")).size()
            ,2);
        assertEquals(
            ((ArrayNode) client.get(TYPE_ROUTE + "?q=sampleField:a&size=1&from=1")
                    .get("results")).size()
            ,1);
        assertEquals(
            ((ArrayNode) client.get(TYPE_ROUTE + "?q=sampleField:a&from=2")
                    .get("results")).size()
            ,1);
    }

    @Test
    public void quotesOnComplexSearches() throws IOException, InterruptedException {
        postSampleData("ds1","a","b","a");
        postSampleData("ds2","a","b","a");

        client.authenticate(TestConstants.USER_NAME_ADMIN, TestConstants.PASS_WORD);

        assertEquals(
                ((ArrayNode) client.get(TYPE_ROUTE + "?q=\"sampleField:a AND dataset:ds1\"")
                        .get("results")).size()
                ,2);
    }

    @Test
    public void quotesOnComplexSearchesWorkWithSizeAndOFrom() throws IOException, InterruptedException {
        postSampleData("ds1","a","a","a");
        postSampleData("ds2","a","b","a");

        client.authenticate(TestConstants.USER_NAME_ADMIN, TestConstants.PASS_WORD);

        assertEquals(
                ((ArrayNode) client.get(TYPE_ROUTE + "?q=\"sampleField:a AND dataset:ds1\"&size=4&from=1")
                        .get("results")).size()
                ,2);
    }

    @Test
    public void searchWithFacet() throws IOException, InterruptedException {
        postSampleData("ds1","a","a","a");
        postSampleData("ds2","a","b","a");

        client.authenticate(TestConstants.USER_NAME_ADMIN, TestConstants.PASS_WORD);

        JsonNode facets = client.get(TYPE_ROUTE + "?q=*&facet=sampleField").get("facets");
        assertEquals(facets.get("sampleField").get("buckets").get(0).get("doc_count").asInt(), 5);
        assertEquals(facets.get("sampleField").get("buckets").get(1).get("doc_count").asInt(), 1);
    }

    @Test
    public void searchFacetQuery() throws IOException, InterruptedException {
        postSampleData("ds1","a","a","a");
        postSampleData("ds2","a","b","c");

        client.authenticate(TestConstants.USER_NAME_ADMIN, TestConstants.PASS_WORD);

        JsonNode results = client.get(TYPE_ROUTE + "?q=*&fq=sampleField:a");
        assertEquals(results.get("total").asInt(), 4);

        results = client.get(TYPE_ROUTE + "?q=*&fq=sampleField:b");
        assertEquals(results.get("total").asInt(), 1);
    }

    @Test
    public void searchFacetQueryWithEscapedColon() throws IOException, InterruptedException {
        postSampleData("ds1","a:b","a:b","a");
        postSampleData("ds2","b:a","b","c");

        client.authenticate(TestConstants.USER_NAME_ADMIN, TestConstants.PASS_WORD);

        JsonNode results = client.get(TYPE_ROUTE + "?q=*&fq=sampleField:a\\:b");
        assertEquals(results.get("total").asInt(), 2);

        results = client.get(TYPE_ROUTE + "?q=*&fq=sampleField:b\\:a");
        assertEquals(results.get("total").asInt(), 1);
    }
}

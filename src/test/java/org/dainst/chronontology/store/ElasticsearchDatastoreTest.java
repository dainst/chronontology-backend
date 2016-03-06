package org.dainst.chronontology.store;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dainst.chronontology.JsonTestUtils;
import org.dainst.chronontology.TestConstants;
import org.dainst.chronontology.it.ESClientTestUtil;
import org.dainst.chronontology.store.rest.JsonRestClient;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.testng.Assert.assertEquals;
import static org.dainst.chronontology.TestConstants.*;

/**
 * @author Daniel M. de Oliveira
 */
public class ElasticsearchDatastoreTest {


    private final JsonRestClient client= new JsonRestClient(ESServerTestUtil.getUrl());
    private final ElasticsearchDatastore store=
            new ElasticsearchDatastore(client,TEST_INDEX);

    private JsonNode sampleJson(final String sampleFieldValue) throws IOException {
        return new ObjectMapper().readTree
                ("{\"a\":\"" + sampleFieldValue + "\"}");
    }

    @BeforeClass
    public void setUp() {
        ESServerTestUtil.startElasticSearchServer();
    }

    @AfterClass
    public void tearDown() {
        ESServerTestUtil.stopElasticSearchServer();
    }

    @AfterMethod
    public void afterMethod() {
        store.remove(TEST_TYPE, "a");
    }

    @Test
    public void returnCorrectResultsWithOffsetAndSizeParams() throws IOException {

        client.put("/"+TestConstants.TEST_INDEX+"/"+TestConstants.TEST_TYPE + "/a", JsonTestUtils.sampleDocument("a","1","none"));
        client.put("/"+TestConstants.TEST_INDEX+"/"+TestConstants.TEST_TYPE + "/b", JsonTestUtils.sampleDocument("a","2","dataset1"));
        client.put("/"+TestConstants.TEST_INDEX+"/"+TestConstants.TEST_TYPE + "/c", JsonTestUtils.sampleDocument("a","3","none"));
        client.put("/"+TestConstants.TEST_INDEX+"/"+TestConstants.TEST_TYPE + "/d", JsonTestUtils.sampleDocument("a","4","dataset1"));
        client.put("/"+TestConstants.TEST_INDEX+"/"+TestConstants.TEST_TYPE + "/e", JsonTestUtils.sampleDocument("a","5","none"));

        ESClientTestUtil.refreshES();

        JsonTestUtils.assertResultsAreFound(
                store.search(TEST_TYPE,"", Arrays.asList(new String[]{"dataset:none"})).j(),
                Arrays.asList("1","3","5"));
        JsonTestUtils.assertResultsAreFound(
                store.search(TEST_TYPE,"a?from=0&size=1", Arrays.asList(new String[]{"dataset:none"})).j(),
                Arrays.asList("1"));
        JsonTestUtils.assertResultsAreFound(
                store.search(TEST_TYPE,"a?from=1&size=1", Arrays.asList(new String[]{"dataset:none"})).j(),
                Arrays.asList("3"));
        JsonTestUtils.assertResultsAreFound(
                store.search(TEST_TYPE,"a?from=1&size=2", Arrays.asList(new String[]{"dataset:none"})).j(),
                Arrays.asList("3","5"));
        JsonTestUtils.assertResultsAreFound(
                store.search(TEST_TYPE,"", Arrays.asList(new String[]{"dataset:none","dataset:dataset1"})).j(),
                Arrays.asList("1","2","3","4","5"));
        JsonTestUtils.assertResultsAreFound(
                store.search(TEST_TYPE,"a?from=1&size=2", Arrays.asList(new String[]{"dataset:none","dataset:dataset1"})).j(),
                Arrays.asList("2","3"));
        JsonTestUtils.assertResultsAreFound(
                store.search(TEST_TYPE,"a", null).j(),
                Arrays.asList("1","2","3","4","5"));
    }

    @Test
    public void putAndGetItemForId() throws IOException {

        store.put(TEST_TYPE,"a",sampleJson("a"));
        assertEquals(store.get(TEST_TYPE,"a"),sampleJson("a"));
    }

    @Test
    public void deleteAnItem() throws IOException, InterruptedException {

        store.put(TEST_TYPE,"a",sampleJson("a"));
        store.remove(TEST_TYPE, "a");

        Thread.sleep(100);
        assertEquals(store.get(TEST_TYPE,"a"), null);
    }
}

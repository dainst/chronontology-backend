package org.dainst.chronontology.store;

import com.squareup.okhttp.OkHttpClient;
import org.dainst.chronontology.JsonTestUtils;
import org.dainst.chronontology.it.ESClientTestUtil;
import org.dainst.chronontology.store.rest.JsonRestClient;
import org.testng.annotations.*;

import java.io.IOException;
import java.util.Arrays;

import static org.dainst.chronontology.TestConstants.TEST_INDEX;
import static org.dainst.chronontology.TestConstants.TEST_TYPE;
import static org.testng.Assert.assertEquals;

/**
 * @author Daniel M. de Oliveira
 */
public class ElasticsearchDatastoreTest {

    private final JsonRestClient client= new JsonRestClient(ESServerTestUtil.getUrl(),new OkHttpClient(),false);
    private final ElasticsearchDatastore store=
            new ElasticsearchDatastore(client,TEST_INDEX);

    @BeforeClass
    public void setUp() {
        ESServerTestUtil.startElasticSearchServer();
    }

    @AfterClass
    public void tearDown() {
        ESServerTestUtil.stopElasticSearchServer();
    }

    @BeforeMethod
    public void before() {
        store.put(TEST_TYPE,"a", JsonTestUtils.sampleDocument("a","1","none"));
        store.put(TEST_TYPE,"b", JsonTestUtils.sampleDocument("a","2","dataset1"));
        store.put(TEST_TYPE,"c", JsonTestUtils.sampleDocument("a","3","none"));
        store.put(TEST_TYPE,"d", JsonTestUtils.sampleDocument("a","4","dataset1"));
        store.put(TEST_TYPE,"e", JsonTestUtils.sampleDocument("a","5","none"));
        ESClientTestUtil.refreshES();
    }

    @AfterMethod
    public void afterMethod() {
        store.remove(TEST_TYPE, "a");
        store.remove(TEST_TYPE, "b");
        store.remove(TEST_TYPE, "c");
        store.remove(TEST_TYPE, "d");
        store.remove(TEST_TYPE, "e");
        ESClientTestUtil.refreshES();
    }

    @Test
    public void findDocsWhereDatasetIsNone() {
        JsonTestUtils.assertResultsAreFound(
                store.search(TEST_TYPE,"", Arrays.asList(new String[]{"dataset:none"})).j(),
                Arrays.asList("1","3","5"));
    }

    @Test
    public void findDocsMatchingTermWithFromAndSizeAndDatasetNone() {
        JsonTestUtils.assertResultsAreFound(
                store.search(TEST_TYPE, "a?from=0&size=1", Arrays.asList(new String[]{"dataset:none"})).j(),
                Arrays.asList("1"));
        JsonTestUtils.assertResultsAreFound(
                store.search(TEST_TYPE, "a?from=1&size=1", Arrays.asList(new String[]{"dataset:none"})).j(),
                Arrays.asList("3"));
        JsonTestUtils.assertResultsAreFound(
                store.search(TEST_TYPE,"a?from=1&size=2", Arrays.asList(new String[]{"dataset:none"})).j(),
                Arrays.asList("3","5"));
    }

    @Test
    public void findAllDocsWithDifferentDatasets() {
        JsonTestUtils.assertResultsAreFound(
                store.search(TEST_TYPE,"", Arrays.asList(new String[]{"dataset:none","dataset:dataset1"})).j(),
                Arrays.asList("1","2","3","4","5"));
    }

    @Test
    public void findAllDocsWithDifferentDatasetsAndSizeAndFrom() {
        JsonTestUtils.assertResultsAreFound(
                store.search(TEST_TYPE,"a?from=1&size=2", Arrays.asList(new String[]{"dataset:none","dataset:dataset1"})).j(),
                Arrays.asList("2","3"));
    }

    @Test
    public void findInAllPossibleDatasets() {
        JsonTestUtils.assertResultsAreFound(
                store.search(TEST_TYPE,"a", null).j(),
                Arrays.asList("1","2","3","4","5"));
    }

    @Test
    public void findWithSizeAndFromWithoutOtherSearchTerms() {
        JsonTestUtils.assertResultsAreFound(
                store.search(TEST_TYPE,"size=1", Arrays.asList(new String[]{"dataset:none"})).j(),
                Arrays.asList("1"));
        JsonTestUtils.assertResultsAreFound(
                store.search(TEST_TYPE,"size=1&from=2", Arrays.asList(new String[]{"dataset:none"})).j(),
                Arrays.asList("5"));
    }

    @Test
    public void getItemForId() throws IOException {
        assertEquals(store.get(TEST_TYPE,"a"),JsonTestUtils.sampleDocument("a","1","none"));
    }

    @Test
    public void deleteAnItem() throws IOException, InterruptedException {
        store.remove(TEST_TYPE, "a");

        Thread.sleep(100);
        assertEquals(store.get(TEST_TYPE,"a"), null);
    }
}

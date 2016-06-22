package org.dainst.chronontology.store;

import com.squareup.okhttp.OkHttpClient;
import org.dainst.chronontology.JsonTestUtils;
import org.dainst.chronontology.handler.model.Query;
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
 * @author Sebastian Cuy
 */
public class ElasticsearchDatastoreTest {

    private final JsonRestClient client = new JsonRestClient(ESServerTestUtil.getUrl(), new OkHttpClient(), false);
    private final ElasticsearchDatastore store =
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
        Query query = new Query("", 0, 10);
        query.addDataset("none");
        JsonTestUtils.assertResultsAreFound(
                store.search(TEST_TYPE, query).j(),
                Arrays.asList("1","3","5"));
    }

    @Test
    public void findDocsMatchingTermWithFromAndSizeAndDatasetNone() {

        Query query = new Query("a", 0, 1);
        query.addDataset("none");
        JsonTestUtils.assertResultsAreFound(
                store.search(TEST_TYPE, query).j(),
                Arrays.asList("1"));

        query = new Query("a", 1, 1);
        query.addDataset("none");
        JsonTestUtils.assertResultsAreFound(
                store.search(TEST_TYPE, query).j(),
                Arrays.asList("3"));

        query = new Query("a", 1, 2);
        query.addDataset("none");
        JsonTestUtils.assertResultsAreFound(
                store.search(TEST_TYPE, query).j(),
                Arrays.asList("3","5"));

    }

    @Test
    public void findAllDocsWithDifferentDatasets() {
        Query query = new Query("", 0, 10);
        query.addDataset("none");
        query.addDataset("dataset1");
        JsonTestUtils.assertResultsAreFound(
                store.search(TEST_TYPE, query).j(),
                Arrays.asList("1","2","3","4","5"));
    }

    @Test
    public void findAllDocsWithDifferentDatasetsAndSizeAndFrom() {

        Query query = new Query("", 1, 2);
        query.addDataset("none");
        query.addDataset("dataset1");
        JsonTestUtils.assertResultsAreFound(
                store.search(TEST_TYPE, query).j(),
                Arrays.asList("2","3"));

    }

    @Test
    public void findInAllPossibleDatasets() {
        Query query = new Query("a", 0, 10);
        JsonTestUtils.assertResultsAreFound(
                store.search(TEST_TYPE, query).j(),
                Arrays.asList("1","2","3","4","5"));
    }

    @Test
    public void findWithSizeAndFromWithoutOtherSearchTerms() {

        Query query = new Query("", 0, 1);
        JsonTestUtils.assertResultsAreFound(
                store.search(TEST_TYPE, query).j(),
                Arrays.asList("1"));

        query = new Query("", 2, 1);
        JsonTestUtils.assertResultsAreFound(
                store.search(TEST_TYPE, query).j(),
                Arrays.asList("3"));

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

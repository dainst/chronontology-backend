package org.dainst.chronontology.store;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dainst.chronontology.connect.JsonRestClient;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.dainst.chronontology.TestConstants.*;

/**
 * @author Daniel M. de Oliveira
 */
public class ESRestSearchableKeyValueStoreTest {


    private final JsonRestClient jrc= new JsonRestClient(ESServerTestUtil.getUrl());
    private final ESRestSearchableDatastore store=
            new ESRestSearchableDatastore(jrc,TEST_INDEX);

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

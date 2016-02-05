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

/**
 * @author Daniel M. de Oliveira
 */
public class ESRestSearchableKeyValueStoreTest {


    private static final String TYPE_NAME= "period";
    private final JsonRestClient jrc= new JsonRestClient("http://localhost:9200");
    private final ESRestSearchableKeyValueStore store=
            new ESRestSearchableKeyValueStore(jrc,"jeremy_test");

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
        store.remove(TYPE_NAME, "a");
    }

    @Test
    public void putAndGetItemForId() throws IOException {

        store.put(TYPE_NAME,"a",sampleJson("a"));
        assertEquals(store.get(TYPE_NAME,"a"),sampleJson("a"));
    }

    @Test
    public void deleteAnItem() throws IOException, InterruptedException {

        store.put(TYPE_NAME,"a",sampleJson("a"));
        store.remove(TYPE_NAME, "a");

        Thread.sleep(100);
        assertEquals(store.get(TYPE_NAME,"a"), null);
    }
}

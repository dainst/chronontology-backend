package org.dainst;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;

/**
 * @author Daniel M. de Oliveira
 */
public class ElasticSearchDatastoreConnectorComponentTest {

    private final ESConnection esC= new ESConnection("elasticsearch","localhost");

    private final ElasticSearchDatastoreConnector store=
            new ElasticSearchDatastoreConnector(esC,"jeremy_test");

    private JsonNode sampleJson(final String sampleFieldValue) throws IOException {
        return new ObjectMapper().readTree
                ("{\"a\":\"" + sampleFieldValue + "\"}");
    }

    @AfterMethod
    public void afterMethod() {
        store.delete(C.TYPE_NAME,"a");
    }

    @Test
    public void putAndGetItemForId() throws IOException {

        store.put(C.TYPE_NAME,"a",sampleJson("a"));
        assertEquals(store.get(C.TYPE_NAME,"a"),sampleJson("a"));
    }

    @Test
    public void deleteAnItem() throws IOException, InterruptedException {

        store.put(C.TYPE_NAME,"a",sampleJson("a"));
        store.delete(C.TYPE_NAME,"a");

        Thread.sleep(100);
        assertEquals(store.get(C.TYPE_NAME,"a"), null);
    }


    /*


    @Test
    public void searchInFields() throws IOException {
        store.search("a:b", null);
    }

    @Test
    public void searchOverAllFields() throws IOException {
        store.search("b", null);
    }

    */
}

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

    ElasticSearchDatastoreConnector store = new ElasticSearchDatastoreConnector("jeremy_test");

    private JsonNode sampleJson(String sampleFieldValue) throws IOException {
        return new ObjectMapper().readTree
                ("{\"a\":\"" + sampleFieldValue + "\"}");
    }

    @AfterMethod
    public void afterMethod() {
        store.delete("a");
    }

    @Test
    public void putAndGetItemForId() throws IOException {

        store.put("a",sampleJson("a"));
        assertEquals(store.get("a"),sampleJson("a"));
    }

    @Test
    public void deleteAnItem() throws IOException {

        store.put("a",sampleJson("a"));
        store.delete("a");
        assertEquals(store.get("a"),null);
    }
}

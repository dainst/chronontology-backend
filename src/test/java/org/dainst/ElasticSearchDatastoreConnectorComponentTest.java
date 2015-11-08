package org.dainst;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

/**
 * @author Daniel M. de Oliveira
 */
public class ElasticSearchDatastoreConnectorComponentTest {

    ElasticSearchDatastoreConnector store = new ElasticSearchDatastoreConnector("jeremy_test");

    @AfterMethod
    public void afterMethod() {
        store.delete("a");
    }

    @Test
    public void putAndGetItemForId() {

        store.put("a","{\"a\":\"a\"}");
        assertEquals(store.get("a"),"{\"a\":\"a\"}");
    }

    @Test
    public void deleteAnItem() {

        store.put("a","{\"a\":\"a\"}");
        store.delete("a");
        assertEquals(store.get("a"),null);
    }
}

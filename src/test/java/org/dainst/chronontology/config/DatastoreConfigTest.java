package org.dainst.chronontology.config;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Daniel M. de Oliveira
 */
public class DatastoreConfigTest extends ConfigTestBase {

    @Test
    public void test() {
        DatastoreConfig config= new DatastoreConfig(
                props("50"),
                "datastores.es.");

        assertTrue(config.validate());

        ElasticSearchConfig esc= (ElasticSearchConfig) config;
        assertEquals(esc.getIndexName(),"connect");
        assertEquals(esc.getUrl(),"http://localhost:9200");
    }
}

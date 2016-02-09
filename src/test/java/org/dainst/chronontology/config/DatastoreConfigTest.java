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
                "datastores.0.");

        assertTrue(config.validate());

        ElasticSearchConfig esc= (ElasticSearchConfig) config;
        assertEquals(esc.getIndexName(),"connect");
        assertEquals(esc.getUrl(),"http://localhost:9200");
    }

    @Test
    public void testFilesystem() {
        DatastoreConfig config= new DatastoreConfig(
                props("51"),
                "datastores.1.");

        assertTrue(config.validate());
        assertEquals(config.getPath(),"ds/");
    }

    @Test
    public void omitPathWithFilesystem() {
        DatastoreConfig config= new DatastoreConfig(
                props("52"),
                "datastores.1.");

        assertTrue(config.validate());
        assertEquals(config.getPath(),"datastore/");
    }
}

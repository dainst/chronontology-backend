package org.dainst.chronontology.config;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Daniel M. de Oliveira
 */
public class DatastoreConfigTest extends ConfigTestBase {

    @Test
    public void test() {
        DatastoreConfig config= new DatastoreConfig("0");
        assertTrue(config.validate(props("50")));
        assertEquals(config.getIndexName(),"connect");
        assertEquals(config.getUrl(),"http://localhost:9200");
    }

    @Test
    public void testFilesystem() {
        DatastoreConfig config= new DatastoreConfig("1");
        assertTrue(config.validate(props("51")));
        assertEquals(config.getPath(),"ds/");
    }

    @Test
    public void omitPathWithFilesystem() {
        DatastoreConfig config= new DatastoreConfig("1");
        assertTrue(config.validate(props("52")));
        assertEquals(config.getPath(),"datastore/");
    }

    @Test
    public void constraintViolationWrongDatastoreType() {
        DatastoreConfig config= new DatastoreConfig("0");
        assertFalse(config.validate(props("53")));
    }
}

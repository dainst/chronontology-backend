package org.dainst.chronontology.config;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Daniel M. de Oliveira
 */
public class DatastoreConfigTest extends ConfigTestBase {

    Properties props = null;

    @BeforeMethod
    public void before() {
        props= new Properties();
    }

    @Test
    public void test() {
        props.put("datastores.0.indexName",ConfigConstants.ES_INDEX_NAME);
        props.put("datastores.0.url","http://localhost:9200");
        
        DatastoreConfig config= new DatastoreConfig("0");
        assertTrue(config.validate(props));
        assertEquals(config.getIndexName(),ConfigConstants.ES_INDEX_NAME);
        assertEquals(config.getUrl(),"http://localhost:9200");
    }

    @Test
    public void testFilesystem() {
        String path= "ds/";
        props.put("datastores.1.path",path);
        props.put("datastores.1.type",ConfigConstants.DATASTORE_TYPE_FS);

        DatastoreConfig config= new DatastoreConfig("1");
        assertTrue(config.validate(props));
        assertEquals(config.getPath(),path);
    }

    @Test
    public void omitPathWithFilesystem() {
        props.put("datastores.1.type",ConfigConstants.DATASTORE_TYPE_FS);

        DatastoreConfig config= new DatastoreConfig("1");
        assertTrue(config.validate(props));
        assertEquals(config.getPath(),ConfigConstants.DATASTORE_PATH);
    }

    @Test
    public void constraintViolationWrongDatastoreType() {
        props.put("datastores.0.type","unknown");

        DatastoreConfig config= new DatastoreConfig("0");
        assertFalse(config.validate(props));
    }
}

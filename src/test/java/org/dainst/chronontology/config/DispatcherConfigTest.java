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
public class DispatcherConfigTest {

    private Properties props = new Properties();
    private DispatcherConfig dispatcherConfig = new DispatcherConfig();

    @BeforeMethod
    public void before() {
        props= new Properties();
        dispatcherConfig = new DispatcherConfig();
    }

    @Test
    public void dontUseConnect() {
        props.put("useConnect","false");

        assertTrue(dispatcherConfig.validate(props));
        assertEquals(dispatcherConfig.isUseConnect(),false);
    }

    @Test
    public void basic() {

        dispatcherConfig.validate(props);
        assertEquals(dispatcherConfig.isUseConnect(),true);
    }


    @Test
    public void esConfig() {
        props.put("useConnect","false");
        props.put("datastores.0.indexName","index");
        props.put("datastores.0.url","http://localhost:9200");

        assertTrue(dispatcherConfig.validate(props));
        assertEquals(dispatcherConfig.getDatastoreConfigs()[0].getIndexName(),"index");
        assertEquals(dispatcherConfig.getDatastoreConfigs()[0].getUrl(),"http://localhost:9200");
    }

    @Test
    public void omitDedicatedEsConfig() {
        props.put("typeNames","period");
        props.put("credentials","abc:def");
        props.put("useEmbeddedES","true");
        props.put("useConnect","false");

        assertTrue(dispatcherConfig.validate(props));
        assertEquals(dispatcherConfig.getDatastoreConfigs()[0].getIndexName(), ConfigConstants.ES_INDEX_NAME);
        assertEquals(dispatcherConfig.getDatastoreConfigs()[0].getUrl(), ConfigConstants.EMBEDDED_ES_URL);
    }

    @Test
    public void twoEShaveSameUrlIndexName() {
        props.put("useConnect","true");
        props.put("datastores.0.indexName","index");
        props.put("datastores.0.url","http://localhost:9200");
        props.put("datastores.1.indexName","index");
        props.put("datastores.1.url","http://localhost:9200");

        assertFalse(dispatcherConfig.validate(props));
        assertTrue(dispatcherConfig.getConstraintViolations().contains(
                ConfigConstants.MSG_CONSTRAINT_VIOLATION+ DispatcherConfig.MSG_ES_CLASH));
    }

    @Test
    public void twoEShaveSameUrlButDifferentIndexName() {
        props.put("useConnect","true");
        props.put("datastores.0.indexName","index1");
        props.put("datastores.0.url","http://localhost:9200");
        props.put("datastores.1.indexName","index2");
        props.put("datastores.1.url","http://localhost:9200");

        assertTrue(dispatcherConfig.validate(props));
        assertTrue(dispatcherConfig.getConstraintViolations().isEmpty());
    }


    @Test
    public void datastore0isNotES_single() {
        props.put("useConnect","false");
        props.put("datastores.0.type",ConfigConstants.DATASTORE_TYPE_FS);

        assertFalse(dispatcherConfig.validate(props));
        assertTrue(dispatcherConfig.getConstraintViolations().contains(
                ConfigConstants.MSG_CONSTRAINT_VIOLATION+ DispatcherConfig.MSG_MUST_TYPE_ES));
    }

    @Test
    public void datastore0isNotES_connect() {
        props.put("datastores.0.type","filesystem");

        assertFalse(dispatcherConfig.validate(props));
        assertTrue(dispatcherConfig.getConstraintViolations().contains(
                ConfigConstants.MSG_CONSTRAINT_VIOLATION+ DispatcherConfig.MSG_MUST_TYPE_ES));
    }

}

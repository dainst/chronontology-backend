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
public class ControllerConfigTest {

    private Properties props = new Properties();
    private ControllerConfig controllerConfig = new ControllerConfig();

    @BeforeMethod
    public void before() {
        props= new Properties();
        controllerConfig= new ControllerConfig();
    }

    @Test
    public void dontUseConnect() {
        props.put("useConnect","false");

        assertTrue(controllerConfig.validate(props));
        assertEquals(controllerConfig.isUseConnect(),false);
    }

    @Test
    public void basic() {

        controllerConfig.validate(props);
        assertEquals(controllerConfig.isUseConnect(),true);
    }


    @Test
    public void esConfig() {
        props.put("useConnect","false");
        props.put("datastores.0.indexName","index");
        props.put("datastores.0.url","http://localhost:9200");

        assertTrue(controllerConfig.validate(props));
        assertEquals(controllerConfig.getDatastoreConfigs()[0].getIndexName(),"index");
        assertEquals(controllerConfig.getDatastoreConfigs()[0].getUrl(),"http://localhost:9200");
    }

    @Test
    public void omitDedicatedEsConfig() {
        props.put("typeNames","period");
        props.put("credentials","abc:def");
        props.put("useEmbeddedES","true");
        props.put("useConnect","false");

        assertTrue(controllerConfig.validate(props));
        assertEquals(controllerConfig.getDatastoreConfigs()[0].getIndexName(), ConfigConstants.ES_INDEX_NAME);
        assertEquals(controllerConfig.getDatastoreConfigs()[0].getUrl(), ConfigConstants.EMBEDDED_ES_URL);
    }

    @Test
    public void twoEShaveSameUrlIndexName() {
        props.put("useConnect","true");
        props.put("datastores.0.indexName","index");
        props.put("datastores.0.url","http://localhost:9200");
        props.put("datastores.1.indexName","index");
        props.put("datastores.1.url","http://localhost:9200");

        assertFalse(controllerConfig.validate(props));
        assertTrue(controllerConfig.getConstraintViolations().contains(
                ConfigConstants.MSG_CONSTRAINT_VIOLATION+ControllerConfig.MSG_ES_CLASH));
    }

    @Test
    public void twoEShaveSameUrlButDifferentIndexName() {
        props.put("useConnect","true");
        props.put("datastores.0.indexName","index1");
        props.put("datastores.0.url","http://localhost:9200");
        props.put("datastores.1.indexName","index2");
        props.put("datastores.1.url","http://localhost:9200");

        assertTrue(controllerConfig.validate(props));
        assertTrue(controllerConfig.getConstraintViolations().isEmpty());
    }


    @Test
    public void datastore0isNotES_single() {
        props.put("useConnect","false");
        props.put("datastores.0.type",ConfigConstants.DATASTORE_TYPE_FS);

        assertFalse(controllerConfig.validate(props));
        assertTrue(controllerConfig.getConstraintViolations().contains(
                ConfigConstants.MSG_CONSTRAINT_VIOLATION+ControllerConfig.MSG_MUST_TYPE_ES));
    }

    @Test
    public void datastore0isNotES_connect() {
        props.put("datastores.0.type","filesystem");

        assertFalse(controllerConfig.validate(props));
        assertTrue(controllerConfig.getConstraintViolations().contains(
                ConfigConstants.MSG_CONSTRAINT_VIOLATION+ControllerConfig.MSG_MUST_TYPE_ES));
    }

}

package org.dainst.chronontology.config;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Properties;

import static org.testng.Assert.assertEquals;
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
}

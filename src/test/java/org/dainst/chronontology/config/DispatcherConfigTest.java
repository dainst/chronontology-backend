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
        props.put("datastore.elasticsearch.indexName","index");
        props.put("datastore.elasticsearch.url","http://localhost:9200");

        assertTrue(dispatcherConfig.validate(props));
        assertEquals(dispatcherConfig.getElasticsearchDatastoreConfig().getIndexName(),"index");
        assertEquals(dispatcherConfig.getElasticsearchDatastoreConfig().getUrl(),"http://localhost:9200");
    }

    @Test
    public void omitDedicatedEsConfig() {
        props.put("typeNames","period");
        props.put("credentials","abc:def");
        props.put("useEmbeddedES","true");
        props.put("useConnect","false");

        assertTrue(dispatcherConfig.validate(props));
        assertEquals(dispatcherConfig.getElasticsearchDatastoreConfig().getIndexName(), ConfigConstants.ES_INDEX_NAME);
        assertEquals(dispatcherConfig.getElasticsearchDatastoreConfig().getUrl(), ConfigConstants.EMBEDDED_ES_URL);
    }
}

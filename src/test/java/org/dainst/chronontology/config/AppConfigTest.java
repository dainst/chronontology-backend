package org.dainst.chronontology.config;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Properties;

import static org.testng.Assert.*;

/**
 * @author Daniel M. de Oliveira
 */
public class AppConfigTest extends ConfigTestBase {

    private AppConfig appConfig;
    private Properties props= null;

    @BeforeMethod
    public void before() {
        props= new Properties();
        appConfig= new AppConfig();
    }

    @Test
    public void basic() {
        props.put("serverPort","4567");
        props.put("typeNames","period");
        props.put("credentials","abc:def");

        appConfig.validate(props);
        assertEquals(appConfig.getServerPort(),"4567");
        assertEquals(appConfig.getCredentials()[0],"abc:def");
        assertEquals(appConfig.isUseConnect(),true);
    }

    /**
     * required: typeNames
     */
    @Test
    public void missingRequiredProperty() {
        props.put("credentials","abc:def");

        assertFalse(appConfig.validate(props));
    }

    @Test
    public void useEmbeddedES() {
        props.put("typeNames","period");
        props.put("credentials","abc:def");
        props.put("useEmbeddedES","true");

        appConfig.validate(props);
        assertNotNull(appConfig.getElasticsearchServerConfig());
    }

    @Test
    public void dontUseEmbeddedESBySettingParamFalse() {
        props.put("useEmbeddedES","false");
        props.put("typeNames","period");
        props.put("credentials","abc:def");

        appConfig.validate(props);
        assertNull(appConfig.getElasticsearchServerConfig());
    }

    /**
     * Omitting param is supported for backward compatibility.
     */
    @Test
    public void dontUseEmbeddedESByOmittingParam() {
        props.put("typeNames","period");
        props.put("credentials","abc:def");

        assertTrue(appConfig.validate(props));
        assertNull(appConfig.getElasticsearchServerConfig());
    }

    @Test
    public void allowOmitServerPort() {
        props.put("typeNames","period");
        props.put("credentials","abc:def");
        props.put("useEmbeddedES","true");

        assertTrue(appConfig.validate(props));
        assertEquals(appConfig.getServerPort(), ConfigConstants.SERVER_PORT);
    }

    @Test
    public void dontUseConnect() {
        props.put("typeNames","period");
        props.put("credentials","abc:def");
        props.put("useEmbeddedES","true");
        props.put("useConnect","false");

        assertTrue(appConfig.validate(props));
        assertEquals(appConfig.isUseConnect(),false);
    }

    @Test
    public void esConfig() {
        props.put("typeNames","period");
        props.put("credentials","abc:def");
        props.put("useEmbeddedES","true");
        props.put("useConnect","false");
        props.put("datastores.0.indexName","index");
        props.put("datastores.0.url","http://localhost:9200");

        assertTrue(appConfig.validate(props));
        assertEquals(appConfig.getDatastoreConfigs()[0].getIndexName(),"index");
        assertEquals(appConfig.getDatastoreConfigs()[0].getUrl(),"http://localhost:9200");
    }

    @Test
    public void omitDedicatedEsConfig() {
        props.put("typeNames","period");
        props.put("credentials","abc:def");
        props.put("useEmbeddedES","true");
        props.put("useConnect","false");

        assertTrue(appConfig.validate(props));
        assertEquals(appConfig.getDatastoreConfigs()[0].getIndexName(), ConfigConstants.ES_INDEX_NAME);
        assertEquals(appConfig.getDatastoreConfigs()[0].getUrl(), ConfigConstants.EMBEDDED_ES_URL);
    }

    @Test
    public void elasticSearchServerConfig() {
        props.put("typeNames","period");
        props.put("credentials","abc:def");
        props.put("useEmbeddedES","true");
        props.put("useConnect","false");

        assertTrue(appConfig.validate(props));
        assertEquals(appConfig.getElasticsearchServerConfig().getPort(), ConfigConstants.EMBEDDED_ES_PORT);
    }

    @Test
    public void firstDatastoreNotOfTypeES() {
        props.put("typeNames","period");
        props.put("credentials","abc:def");
        props.put("useEmbeddedES","true");
        props.put("useConnect","false");
        props.put("datastores.0.type",ConfigConstants.DATASTORE_TYPE_FS);

        assertFalse(appConfig.validate(props));
    }
}

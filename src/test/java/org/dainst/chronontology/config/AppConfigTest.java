package org.dainst.chronontology.config;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Daniel M. de Oliveira
 */
public class AppConfigTest extends ConfigTestBase {

    private AppConfig appConfig;

    @BeforeMethod
    public void before() {
        appConfig= new AppConfig();
    }

    @Test
    public void basic() {
        appConfig.validate(props("1"));
        assertEquals(appConfig.getServerPort(),"4567");
        assertEquals(appConfig.getCredentials()[0],"admin:s3cr3t");
        assertEquals(appConfig.isUseConnect(),true);
    }

    @Test
    public void missingRequiredProperty() {
        assertFalse(appConfig.validate(props("2")));
    }

    @Test
    public void useEmbeddedES() {
        appConfig.validate(props("3"));
        assertNotNull(appConfig.getElasticsearchServerConfig());
    }

    @Test
    public void dontUseEmbeddedESBySettingParamFalse() {
        appConfig.validate(props("4"));
        assertNull(appConfig.getElasticsearchServerConfig());
    }

    /**
     * Omitting param is supported for backward compatibility.
     */
    @Test
    public void dontUseEmbeddedESByOmittingParam() {
        assertTrue(appConfig.validate(props("5")));
        assertNull(appConfig.getElasticsearchServerConfig());
    }

    @Test
    public void allowOmitServerPort() {
        assertTrue(appConfig.validate(props("10")));
        assertEquals(appConfig.getServerPort(), ConfigConstants.SERVER_PORT);
    }

    @Test
    public void allowOmitESIndexName() {
        assertTrue(appConfig.validate(props("11")));
        assertEquals(appConfig.getServerPort(), ConfigConstants.SERVER_PORT);
    }

    @Test
    public void dontUseConnect() {
        assertTrue(appConfig.validate(props("12")));
        assertEquals(appConfig.isUseConnect(),false);
    }

    @Test
    public void esConfig() {
        assertTrue(appConfig.validate(props("13")));
        assertEquals(appConfig.getDatastoreConfigs()[0].getIndexName(),"index");
        assertEquals(appConfig.getDatastoreConfigs()[0].getUrl(),"http://localhost:9200");
    }

    @Test
    public void omitDedicatedEsConfig() {
        assertTrue(appConfig.validate(props("14")));
        assertEquals(appConfig.getDatastoreConfigs()[0].getIndexName(), ConfigConstants.ES_INDEX_NAME);
        assertEquals(appConfig.getDatastoreConfigs()[0].getUrl(), ConfigConstants.EMBEDDED_ES_URL);
    }

    @Test
    public void elasticSearchServerConfig() {
        assertTrue(appConfig.validate(props("15")));
        assertEquals(appConfig.getElasticsearchServerConfig().getPort(), ConfigConstants.EMBEDDED_ES_PORT);
    }

    @Test
    public void firstDatastoreNotOfTypeES() {
        assertFalse(appConfig.validate(props("16")));
    }
}

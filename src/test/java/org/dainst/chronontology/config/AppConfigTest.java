package org.dainst.chronontology.config;

import org.dainst.chronontology.Constants;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

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
        assertEquals(appConfig.isUseEmbeddedES(),true);
    }

    @Test
    public void dontUseEmbeddedESBySettingParamFalse() {
        appConfig.validate(props("4"));
        assertEquals(appConfig.isUseEmbeddedES(),false);
    }

    /**
     * Omitting param is supported for backward compatibility.
     */
    @Test
    public void dontUseEmbeddedESByOmittingParam() {
        assertTrue(appConfig.validate(props("5")));
        assertEquals(appConfig.isUseEmbeddedES(),false);
    }

    @Test
    public void useSpecifiedESUrlWithUsingEmbeddedES() {
        assertTrue(appConfig.validate(props("6")));
        assertEquals(appConfig.isUseEmbeddedES(),true);
        assertEquals(appConfig.getEsPort(),"9200");
    }

    @Test
    public void allowOmitEsUrlIfUsingEmbeddedES() {
        assertTrue(appConfig.validate(props("7")));
        assertEquals(appConfig.isUseEmbeddedES(),true);
        Assert.assertEquals(appConfig.getEsPort(), Constants.EMBEDDED_ES_PORT);
    }

    @Test
    public void dontAllowOmitEsUrlWhenNotUsingEmbeddedES() {
        assertFalse(appConfig.validate(props("8")));
    }

    @Test
    public void allowOmitServerPort() {
        assertTrue(appConfig.validate(props("10")));
        assertEquals(appConfig.getServerPort(),Constants.SERVER_PORT);
    }

    @Test
    public void allowOmitESIndexName() {
        assertTrue(appConfig.validate(props("11")));
        assertEquals(appConfig.getServerPort(),Constants.SERVER_PORT);
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
        assertEquals(appConfig.getDatastoreConfigs()[0].getIndexName(),Constants.ES_INDEX_NAME);
        assertEquals(appConfig.getDatastoreConfigs()[0].getUrl(),Constants.EMBEDDED_ES_URL);
    }
}

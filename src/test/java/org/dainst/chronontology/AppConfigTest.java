package org.dainst.chronontology;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Daniel M. de Oliveira
 */
public class AppConfigTest {

    private AppConfig appConfig;

    @BeforeMethod
    public void before() {
        appConfig= new AppConfig();
    }

    @Test
    public void basic() {
        appConfig.loadConfiguration(TestConstants.TEST_FOLDER+"config.1.properties");
        assertEquals(appConfig.getServerPort(),"4567");
        assertEquals(appConfig.getEsIndexName(),"jeremy");
        assertEquals(appConfig.getCredentials()[0],"admin:s3cr3t");
    }

    @Test
    public void missingRequiredProperty() {
        assertFalse(appConfig.loadConfiguration(TestConstants.TEST_FOLDER+"config.2.properties"));
    }

    @Test
    public void useEmbeddedES() {
        appConfig.loadConfiguration(TestConstants.TEST_FOLDER+"config.3.properties");
        assertEquals(appConfig.isUseEmbeddedES(),true);
    }

    @Test
    public void dontUseEmbeddedESBySettingParamFalse() {
        appConfig.loadConfiguration(TestConstants.TEST_FOLDER+"config.4.properties");
        assertEquals(appConfig.isUseEmbeddedES(),false);
    }

    /**
     * Omitting param is supported for backward compatibility.
     */
    @Test
    public void dontUseEmbeddedESByOmittingParam() {
        assertTrue(appConfig.loadConfiguration(TestConstants.TEST_FOLDER+"config.5.properties"));
        assertEquals(appConfig.isUseEmbeddedES(),false);
    }

    @Test
    public void overrideESUrlWhenUsingEmbeddedES() {
        assertTrue(appConfig.loadConfiguration(TestConstants.TEST_FOLDER+"config.6.properties"));
        assertEquals(appConfig.isUseEmbeddedES(),true);
        assertEquals(appConfig.getEsUrl(),Constants.EMBEDDED_ES_URL);
    }

    @Test
    public void allowOmitEsUrlWhenUsingEmbeddedES() {
        assertTrue(appConfig.loadConfiguration(TestConstants.TEST_FOLDER+"config.7.properties"));
        assertEquals(appConfig.isUseEmbeddedES(),true);
        assertEquals(appConfig.getEsUrl(),Constants.EMBEDDED_ES_URL);
    }

    @Test
    public void dontAllowOmitEsUrlWhenNotUsingEmbeddedES() {
        assertFalse(appConfig.loadConfiguration(TestConstants.TEST_FOLDER+"config.8.properties"));
    }

    @Test
    public void omitDatastorePath() {
        assertTrue(appConfig.loadConfiguration(TestConstants.TEST_FOLDER+"config.9.properties"));
        assertEquals(appConfig.getDataStorePath(),Constants.DATASTORE_PATH);
    }
}

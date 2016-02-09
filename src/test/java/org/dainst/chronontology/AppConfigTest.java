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

    private String propsFile(String number) {
        return TestConstants.TEST_FOLDER+"config."+number+".properties";
    }

    @Test
    public void basic() {
        appConfig.loadConfiguration(propsFile("1"));
        assertEquals(appConfig.getServerPort(),"4567");
        assertEquals(appConfig.getEsIndexName(),"jeremy");
        assertEquals(appConfig.getCredentials()[0],"admin:s3cr3t");
        assertEquals(appConfig.isUseConnect(),true);
    }

    @Test
    public void missingRequiredProperty() {
        assertFalse(appConfig.loadConfiguration(propsFile("2")));
    }

    @Test
    public void useEmbeddedES() {
        appConfig.loadConfiguration(propsFile("3"));
        assertEquals(appConfig.isUseEmbeddedES(),true);
    }

    @Test
    public void dontUseEmbeddedESBySettingParamFalse() {
        appConfig.loadConfiguration(propsFile("4"));
        assertEquals(appConfig.isUseEmbeddedES(),false);
    }

    /**
     * Omitting param is supported for backward compatibility.
     */
    @Test
    public void dontUseEmbeddedESByOmittingParam() {
        assertTrue(appConfig.loadConfiguration(propsFile("5")));
        assertEquals(appConfig.isUseEmbeddedES(),false);
    }

    @Test
    public void useSpecifiedESUrlWithUsingEmbeddedES() {
        assertTrue(appConfig.loadConfiguration(propsFile("6")));
        assertEquals(appConfig.isUseEmbeddedES(),true);
        assertEquals(appConfig.getEsUrl(),"http://localhost:9200");
    }

    @Test
    public void allowOmitEsUrlIfUsingEmbeddedES() {
        assertTrue(appConfig.loadConfiguration(propsFile("7")));
        assertEquals(appConfig.isUseEmbeddedES(),true);
        assertEquals(appConfig.getEsUrl(),Constants.EMBEDDED_ES_URL);
    }

    @Test
    public void dontAllowOmitEsUrlWhenNotUsingEmbeddedES() {
        assertFalse(appConfig.loadConfiguration(propsFile("8")));
    }

    @Test
    public void allowOmitDatastorePath() {
        assertTrue(appConfig.loadConfiguration(propsFile("9")));
        assertEquals(appConfig.getDataStorePath(),Constants.DATASTORE_PATH);
    }

    @Test
    public void allowOmitServerPort() {
        assertTrue(appConfig.loadConfiguration(propsFile("10")));
        assertEquals(appConfig.getServerPort(),Constants.SERVER_PORT);
    }

    @Test
    public void allowOmitESIndexName() {
        assertTrue(appConfig.loadConfiguration(propsFile("11")));
        assertEquals(appConfig.getServerPort(),Constants.SERVER_PORT);
    }

    @Test
    public void dontUseConnect() {
        assertTrue(appConfig.loadConfiguration(propsFile("12")));
        assertEquals(appConfig.isUseConnect(),false);
    }
}

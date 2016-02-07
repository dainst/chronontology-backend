package org.dainst.chronontology;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

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
    public void test() {
        appConfig.loadConfiguration(TestConstants.TEST_FOLDER+"config.1.properties");
        assertEquals(appConfig.getServerPort(),"4567");
        assertEquals(appConfig.getEsIndexName(),"jeremy");
    }

    @Test
    public void missingRequiredProperty() {
        assertFalse(appConfig.loadConfiguration(TestConstants.TEST_FOLDER+"config.2.properties"));
    }

}

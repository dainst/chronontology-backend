package org.dainst.chronontology.config;

import org.dainst.chronontology.TestConstants;

import java.util.Properties;

/**
 * @author Daniel M. de Oliveira
 */
public class ConfigTestBase {

    protected Properties props(String number) {
        return PropertiesLoader.loadConfiguration(TestConstants.TEST_FOLDER+"config."+number+".properties");
    }
}

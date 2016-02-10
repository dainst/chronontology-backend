package org.dainst.chronontology;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.dainst.chronontology.config.*;
import org.dainst.chronontology.controller.DocumentModel;

import java.util.Properties;

/**
 * Main class. Handles wiring of application components.
 *
 * @author Daniel M. de Oliveira
 */
public class App {

    final static Logger logger = Logger.getLogger(App.class);
    private static final String DEFAULT_PROPERTIES_FILE_PATH = "config.properties";

    public static void main(String [] args) {

        Properties props= PropertiesLoader.loadConfiguration(DEFAULT_PROPERTIES_FILE_PATH);
        if (props==null) System.exit(1);
        AppConfig appConfig= new AppConfig();
        if (appConfig.validate(props)==false) System.exit(1);

        AppConfigurator.configure(appConfig);
    }
}

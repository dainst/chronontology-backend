package org.dainst.chronontology;

import org.apache.log4j.Logger;
import org.dainst.chronontology.config.*;
import org.dainst.chronontology.controller.Controller;

import java.util.Properties;

/**
 * Main class. Handles wiring of application components.
 *
 * @author Daniel M. de Oliveira
 */
public class App {

    final static Logger logger = Logger.getLogger(App.class);
    private static final String DEFAULT_PROPERTIES_FILE_PATH = "config.properties";
    private Controller controller = null;

    public static void main(String [] args) {

        new AppConfigurator().configure(
                makeAppConfigFrom(properties(DEFAULT_PROPERTIES_FILE_PATH)));
    }

    private static Properties properties(String path) {
        Properties props= PropertiesLoader.loadConfiguration(path);
        if (props==null) {
            logger.error("Could not load properties from file: config.properties.");
            System.exit(1);
        }
        return props;
    }

    private static AppConfig makeAppConfigFrom(Properties props) {

        AppConfig appConfig= new AppConfig();
        if (appConfig.validate(props)==false) {
            for (String err: appConfig.getConstraintViolations()) {
                logger.error(err);
            }
            System.exit(1);
        }
        return appConfig;
    }

    public App(Controller controller) {
        this.controller = controller;
    }

    public Controller getController() {
        return controller;
    }
}

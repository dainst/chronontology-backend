package org.dainst.chronontology;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dainst.chronontology.config.AppConfigurator;
import org.dainst.chronontology.config.AppConfig;
import org.dainst.chronontology.config.PropertiesLoader;

import java.util.Properties;

/**
 * Main class. Handles wiring of application components.

 * @author Daniel de Oliveira
 */
public class App {

    private final static Logger logger = LogManager.getLogger(App.class);
    private final static String DEFAULT_PROPERTIES_FILE_PATH = "config.properties";

    private final Controller controller;

    public App(Controller controller) {
        this.controller = controller;
    }

    public static void main(String[] args) {
        new AppConfigurator().configure(makeAppConfigFrom(properties(DEFAULT_PROPERTIES_FILE_PATH)));
    }

    public Controller getController() {
        return controller;
    }

    private static Properties properties(String path) {

        Properties props = PropertiesLoader.loadConfiguration(path);
        if (props == null) {
            logger.error("Could not load properties from file: config.properties.");
            System.exit(1);
        }
        return props;
    }

    private static AppConfig makeAppConfigFrom(Properties props) {

        AppConfig appConfig = new AppConfig();
        if (appConfig.validate(props) == false) {
            for (String err : appConfig.getConstraintViolations()) {
                logger.error(err);
            }
            System.exit(1);
        }
        return appConfig;
    }
}

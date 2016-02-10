package org.dainst.chronontology;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.dainst.chronontology.config.*;
import org.dainst.chronontology.controller.Controller;
import org.dainst.chronontology.extra.EmbeddedES;
import org.dainst.chronontology.controller.DocumentModel;

import java.util.Properties;

import static spark.Spark.port;

/**
 * Main class. Handles wiring of application components.
 *
 * @author Daniel M. de Oliveira
 */
public class App {

    final static Logger logger = Logger.getLogger(App.class);
    private static final String DEFAULT_PROPERTIES_FILE_PATH = "config.properties";



    private static String[] getTypes(final String typesString) {
        String[] types= typesString.split(",");
        for (String typeName:types) {
            DocumentModel dm= new DocumentModel(typeName, "1", new ObjectMapper().createObjectNode(),"admin");
            if (dm==null) {
                logger.error("No document model found for "+typeName);
                System.exit(1);
            }
        }
        return types;
    }

    public static void main(String [] args) {

        Properties props= PropertiesLoader.loadConfiguration(DEFAULT_PROPERTIES_FILE_PATH);
        if (props==null) System.exit(1);
        AppConfig appConfig= new AppConfig();
        if (appConfig.validate(props)==false) System.exit(1);

        if (appConfig.getElasticsearchServerConfig()!=null) new EmbeddedES(
                appConfig.getElasticsearchServerConfig());

        final int serverPort= Integer.parseInt(appConfig.getServerPort());
        port(serverPort);

        new Router(
                ControllerConfigurator.configure(appConfig.getControllerConfig()),
                getTypes(appConfig.getTypeNames()),
                appConfig.getCredentials());
    }


}

package org.dainst.chronontology;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.dainst.chronontology.config.AppConfig;
import org.dainst.chronontology.config.DatastoreConfig;
import org.dainst.chronontology.config.PropertiesLoader;
import org.dainst.chronontology.controller.ConnectController;
import org.dainst.chronontology.controller.Controller;
import org.dainst.chronontology.controller.SimpleController;
import org.dainst.chronontology.extra.EmbeddedES;
import org.dainst.chronontology.controller.DocumentModel;
import org.dainst.chronontology.store.ESRestSearchableDatastore;
import org.dainst.chronontology.store.FileSystemDatastore;

import java.io.File;
import java.util.Properties;

import static spark.Spark.port;

import org.dainst.chronontology.util.JsonRestClient;

/**
 * Main class. Handles wiring of application components.
 *
 * @author Daniel M. de Oliveira
 */
public class App {

    final static Logger logger = Logger.getLogger(App.class);
    private static final String DEFAULT_PROPERTIES_FILE_PATH = "config.properties";

    private static FileSystemDatastore initDS(DatastoreConfig datastoreConfig) {

        String datastorePath= datastoreConfig.getPath();

        if (!(new File(datastorePath).exists())) {
            logger.error("Creating directory \"" + datastorePath + "\" for usage by main datastore.");
            new File(datastorePath).mkdirs();
        }
        return new FileSystemDatastore(datastorePath);
    }

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


        ESRestSearchableDatastore searchable=
                new ESRestSearchableDatastore(
                        new JsonRestClient(appConfig.getDatastoreConfigs()[0].getUrl()),
                        appConfig.getDatastoreConfigs()[0].getIndexName());


        Controller controller= null;
        if (appConfig.isUseConnect())
            controller= new ConnectController(
                    initDS(appConfig.getDatastoreConfigs()[1]),searchable);
        else
            controller= new SimpleController(searchable);


        new Router(
                controller,
                getTypes(appConfig.getTypeNames()),
                appConfig.getCredentials());
    }
}

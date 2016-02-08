package org.dainst.chronontology;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.dainst.chronontology.controller.ConnectController;
import org.dainst.chronontology.controller.Controller;
import org.dainst.chronontology.controller.SimpleController;
import org.dainst.chronontology.extra.EmbeddedES;
import org.dainst.chronontology.controller.DocumentModel;
import org.dainst.chronontology.store.ESRestSearchableDatastore;
import org.dainst.chronontology.store.FileSystemDatastore;

import java.io.File;

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

    private static FileSystemDatastore initDS(String datastorePath) {

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

        AppConfig appConfig= new AppConfig();
        if (!appConfig.loadConfiguration(DEFAULT_PROPERTIES_FILE_PATH)) System.exit(1);

        if (appConfig.isUseEmbeddedES()) new EmbeddedES();

        final int serverPort= Integer.parseInt(appConfig.getServerPort());
        port(serverPort);


        ESRestSearchableDatastore searchable=
                new ESRestSearchableDatastore(
                        new JsonRestClient(appConfig.getEsUrl()),
                        appConfig.getEsIndexName());


        Controller controller= null;
        if (appConfig.isUseConnect())
            controller= new ConnectController(
                    initDS(appConfig.getDataStorePath()),searchable);
        else
            controller= new SimpleController(searchable);


        new Router(
                controller,
                getTypes(appConfig.getTypeNames()),
                appConfig.getCredentials());
    }
}

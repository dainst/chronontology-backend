package org.dainst.chronontology;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.dainst.chronontology.model.DocumentModel;
import org.dainst.chronontology.model.DocumentModelFactory;
import org.dainst.chronontology.store.ESRestSearchableKeyValueStore;
import org.dainst.chronontology.store.FileSystemKeyValueStore;

import java.io.File;

import static spark.Spark.port;

import org.dainst.chronontology.connect.JsonRestClient;

import static org.dainst.chronontology.PropertiesHolder.*;

/**
 * Main class. Handles application setup.
 *
 * @author Daniel M. de Oliveira
 */
public class App {

    final static Logger logger = Logger.getLogger(App.class);
    private static final String DEFAULT_PROPERTIES_FILE_PATH = "config.properties";

    private static FileSystemKeyValueStore initDS(String datastorePath) {

        if (!(new File(datastorePath).exists())) {
            logger.error("The specified path " + datastorePath + " does not exist.");
            System.exit(1);
        }
        return new FileSystemKeyValueStore(datastorePath);
    }

    private static String[] getTypes(final String typesString) {
        String[] types= typesString.split(",");
        for (String typeName:types) {
            DocumentModel dm= DocumentModelFactory.create(typeName, "1", new ObjectMapper().createObjectNode());
            if (dm==null) {
                logger.error("No document model found for "+typeName);
                System.exit(1);
            }
        }
        return types;
    }

    public static void main(String [] args) {

        if (!loadConfiguration(DEFAULT_PROPERTIES_FILE_PATH)) System.exit(1);

        final int serverPort= Integer.parseInt(getServerPort());
        port(serverPort);

        final Controller controller= new Controller(
                initDS(getDataStorePath()),
                new ESRestSearchableKeyValueStore(
                        new JsonRestClient(getEsUrl()),
                        getEsIndexName()));

        new Router(
                controller,
                getTypes(getTypeNames()),
                getCredentials());
    }
}

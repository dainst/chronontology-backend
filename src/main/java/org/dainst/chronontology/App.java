package org.dainst.chronontology;

import org.apache.log4j.Logger;
import org.dainst.chronontology.store.ESRestSearchableKeyValueStore;
import org.dainst.chronontology.store.FileSystemKeyValueStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static spark.Spark.port;

import org.dainst.chronontology.connect.JsonRestClient;

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
            return null;
        }
        return new FileSystemKeyValueStore(datastorePath);
    }

    private static Properties loadProps(String propertiesFilePath) {
        Properties props = new Properties();
        try (
                FileInputStream is =new FileInputStream(new File(propertiesFilePath)))
        {
            props.load(is);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }
        return props;
    }

    public static void main(String [] args) {

        Properties props= loadProps(DEFAULT_PROPERTIES_FILE_PATH);
        if (props==null) System.exit(1);

        final FileSystemKeyValueStore store=
                initDS((String)props.get("datastorePath"));
        if (store==null) {
            System.exit(1);
        }

        int serverPort= Integer.parseInt((String)props.get("serverPort"));
        port(serverPort);
        String[] typeNames= ((String)props.get("typeNames")).split(",");
        String[] credentials= ((String)props.get("credentials")).split(",");

        JsonRestClient jrc= new JsonRestClient((String)props.get("esUrl"));

        Controller controller= new Controller(
                store,new ESRestSearchableKeyValueStore(jrc,(String)props.get("esIndexName")));

        new Router(
                controller,
                typeNames,
                credentials);
    }
}

package org.dainst.chronontology;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.dainst.chronontology.model.DocumentModel;
import org.dainst.chronontology.model.DocumentModelFactory;
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
            System.exit(1);
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
            System.exit(1);
        }
        return props;
    }



    private static Properties _validate(final Properties props,final String name) {
        if (props.get(name)==null) {
            logger.error("Property "+name+" does not exist");
            System.exit(1);
        }

        return props;
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

    private static Properties validate(Properties props) {
        _validate(props,"serverPort");
        _validate(props,"esIndexName");
        _validate(props,"datastorePath");
        _validate(props,"esUrl");
        _validate(props,"credentials");
        _validate(props,"typeNames");
        return props;
    }

    public static void main(String [] args) {

        final Properties props= validate(loadProps(DEFAULT_PROPERTIES_FILE_PATH));

        final int serverPort= Integer.parseInt((String)props.get("serverPort"));
        port(serverPort);

        final Controller controller= new Controller(
                initDS((String)props.get("datastorePath")),
                new ESRestSearchableKeyValueStore(
                        new JsonRestClient((String)props.get("esUrl")),
                        (String)props.get("esIndexName")));

        new Router(
                controller,
                getTypes((String)props.get("typeNames")),
                ((String)props.get("credentials")).split(","));
    }
}

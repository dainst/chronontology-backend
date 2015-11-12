package org.dainst;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static spark.Spark.port;


/**
 * Main class. Handles application setup.
 *
 * @author Daniel M. de Oliveira
 */
public class Chronontology {

    final static Logger logger = Logger.getLogger(Chronontology.class);
    private static final String DEFAULT_PROPERTIES_FILE_PATH = "config.properties";

    private static FileSystemDatastoreConnector initDS(String datastorePath) {

        if (!(new File(datastorePath).exists())) {
            logger.error("The specified path "+datastorePath+" does not exist.");
            return null;
        }
        return new FileSystemDatastoreConnector(datastorePath);
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

        final FileSystemDatastoreConnector store=
                initDS((String)props.get("datastorePath"));
        if (store==null) {
            System.exit(1);
        }

        int serverPort= Integer.parseInt((String)props.get("serverPort"));
        port(serverPort);
        String typeName= (String)props.get("typeName");

        ESConnection esC= new ESConnection("elasticsearch",(String)props.get("esHost"));
        new Router(
                store,
                new ElasticSearchDatastoreConnector(esC,(String)props.get("esIndexName")),
                typeName);
    }
}

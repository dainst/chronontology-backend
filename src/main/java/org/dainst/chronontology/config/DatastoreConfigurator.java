package org.dainst.chronontology.config;

import org.apache.log4j.Logger;
import org.dainst.chronontology.store.Connector;
import org.dainst.chronontology.store.Datastore;
import org.dainst.chronontology.store.ESRestSearchableDatastore;
import org.dainst.chronontology.store.FileSystemDatastore;
import org.dainst.chronontology.util.JsonRestClient;

import java.io.File;

/**
 * @author Daniel M. de Oliveira
 */
public class DatastoreConfigurator implements Configurator<Datastore> {

    final static Logger logger = Logger.getLogger(DatastoreConfigurator.class);

    public Datastore configure(Config config) {
        DatastoreConfig datastoreConfig= (DatastoreConfig) config;

        if (datastoreConfig.getType().equals(ConfigConstants.DATASTORE_TYPE_ES)) {
            return new ESRestSearchableDatastore(
                    new JsonRestClient(datastoreConfig.getUrl()),datastoreConfig.getIndexName());
        } else {
            return initDS(datastoreConfig);
        }
    }

    private FileSystemDatastore initDS(DatastoreConfig datastoreConfig) {

        String datastorePath= datastoreConfig.getPath();

        if (!(new File(datastorePath).exists())) {
            logger.error("Creating directory \"" + datastorePath + "\" for usage by main datastore.");
            new File(datastorePath).mkdirs();
        }
        return new FileSystemDatastore(datastorePath);
    }
}

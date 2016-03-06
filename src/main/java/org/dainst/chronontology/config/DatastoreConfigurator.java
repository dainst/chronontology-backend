package org.dainst.chronontology.config;

import org.dainst.chronontology.store.Datastore;
import org.dainst.chronontology.store.ElasticsearchDatastore;
import org.dainst.chronontology.store.FilesystemDatastore;
import org.dainst.chronontology.store.rest.JsonRestClient;

/**
 * @author Daniel M. de Oliveira
 */
public class DatastoreConfigurator implements Configurator<Datastore,DatastoreConfig> {

    public Datastore configure(DatastoreConfig config) {

        if (config.getType().equals(ConfigConstants.DATASTORE_TYPE_ES)) {
            return new ElasticsearchDatastore(
                    new JsonRestClient(config.getUrl()),config.getIndexName());
        } else {
            return initDS(config);
        }
    }

    private FilesystemDatastore initDS(DatastoreConfig config) {

        String datastorePath= config.getPath();
        return new FilesystemDatastore(datastorePath);
    }
}

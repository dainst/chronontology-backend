package org.dainst.chronontology.config;

import org.dainst.chronontology.store.Datastore;
import org.dainst.chronontology.store.ElasticsearchDatastore;
import org.dainst.chronontology.store.FilesystemDatastore;
import org.dainst.chronontology.store.rest.JsonRestClient;

/**
 * @author Daniel M. de Oliveira
 */
public class FilesystemDatastoreConfigurator implements Configurator<FilesystemDatastore,FilesystemDatastoreConfig> {

    public FilesystemDatastore configure(FilesystemDatastoreConfig config) {

        String datastorePath= config.getPath();
        return new FilesystemDatastore(datastorePath);
    }
}

package org.dainst.chronontology.config;

import org.dainst.chronontology.handler.dispatch.ConnectDispatcher;
import org.dainst.chronontology.handler.dispatch.Dispatcher;
import org.dainst.chronontology.handler.dispatch.SimpleDispatcher;
import org.dainst.chronontology.store.ElasticsearchDatastore;
import org.dainst.chronontology.store.FilesystemDatastore;

/**
 * @author Daniel M. de Oliveira
 */
public class DispatcherConfigurator implements Configurator<Dispatcher,DispatcherConfig> {

    public Dispatcher configure(DispatcherConfig config) {

        ElasticsearchDatastore searchable=
                new ElasticsearchDatastoreConfigurator().configure(config.getElasticsearchDatastoreConfig());

        Dispatcher dispatcher = null;
        if (config.isUseConnect())
            dispatcher = new ConnectDispatcher(
                    new FilesystemDatastoreConfigurator().configure(config.getFilesystemDatastoreConfig()),
                    searchable);
        else
            dispatcher = new SimpleDispatcher(searchable);

        return dispatcher;
    }
}

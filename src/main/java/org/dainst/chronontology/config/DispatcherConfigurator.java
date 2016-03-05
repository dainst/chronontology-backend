package org.dainst.chronontology.config;

import org.dainst.chronontology.handler.dispatch.ConnectDispatcher;
import org.dainst.chronontology.handler.dispatch.Dispatcher;
import org.dainst.chronontology.handler.dispatch.SimpleDispatcher;
import org.dainst.chronontology.store.ESRestSearchableDatastore;
import org.dainst.chronontology.store.VersionedDatastore;

/**
 * @author Daniel M. de Oliveira
 */
public class DispatcherConfigurator implements Configurator<Dispatcher,DispatcherConfig> {

    public Dispatcher configure(DispatcherConfig config) {

        ESRestSearchableDatastore searchable= (ESRestSearchableDatastore)
                new DatastoreConfigurator().configure(config.getDatastoreConfigs()[0]);

        Dispatcher dispatcher = null;
        if (config.isUseConnect())
            dispatcher = new ConnectDispatcher(
                    (VersionedDatastore) new DatastoreConfigurator().configure(config.getDatastoreConfigs()[1]),
                    searchable);
        else
            dispatcher = new SimpleDispatcher(searchable);

        return dispatcher;
    }
}

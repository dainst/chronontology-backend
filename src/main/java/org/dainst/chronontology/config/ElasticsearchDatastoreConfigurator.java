package org.dainst.chronontology.config;

import org.dainst.chronontology.store.ElasticsearchDatastore;
import org.dainst.chronontology.store.rest.JsonRestClient;

/**
 * @author Daniel M. de Oliveira
 */
public class ElasticsearchDatastoreConfigurator
        implements Configurator<ElasticsearchDatastore,ElasticsearchDatastoreConfig> {

    @Override
    public ElasticsearchDatastore configure(ElasticsearchDatastoreConfig config) {
        return new ElasticsearchDatastore(
                new JsonRestClient(config.getUrl()),
                config.getIndexName());
    }
}

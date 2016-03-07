package org.dainst.chronontology.handler.dispatch;

/**
 */

import com.fasterxml.jackson.databind.JsonNode;
import org.dainst.chronontology.handler.ServerStatusHandler;
import org.dainst.chronontology.handler.model.Results;
import org.dainst.chronontology.store.ElasticsearchDatastore;

import java.io.IOException;
import java.util.List;

/**
 * The simple dispatcher holds a single searchable datastore.
 *
 * @author Daniel M. de Oliveira
 */
public class SimpleDispatcher extends Dispatcher {

    protected final ElasticsearchDatastore datastore;

    public SimpleDispatcher(ElasticsearchDatastore datastore) {
        this.datastore = datastore;
    }

    @Override
    public JsonNode dispatchGet(final String bucket, final String key) {
        return datastore.get(bucket,key);
    }

    @Override
    public void addDatatoreStatus(ServerStatusHandler handler, Results r) throws IOException {
        r.add(handler.makeDataStoreStatus("single","elasticsearch", datastore));
    }

    @Override
    public boolean dispatchPost(final String bucket, final String key, final JsonNode value) {
        return datastore.put(bucket,key, value);
    }

    @Override
    public boolean dispatchPut(final String bucket, final String key, final JsonNode value) {
        return datastore.put(bucket,key, value);
    }

    @Override
    public JsonNode dispatchGet(final String bucket, final String key,
                                final Boolean direct, // ignored
                                final Integer version
        ) {
        return dispatchGet(bucket,key);
    }

    @Override
    public Results dispatchSearch(final String bucket, final String query, final List<String> excludes) {
        return datastore.search( bucket, query, excludes );
    }
}


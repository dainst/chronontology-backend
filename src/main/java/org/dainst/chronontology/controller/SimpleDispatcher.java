package org.dainst.chronontology.controller;

/**
 */

import com.fasterxml.jackson.databind.JsonNode;
import org.dainst.chronontology.handler.ServerStatusHandler;
import org.dainst.chronontology.util.Results;
import org.dainst.chronontology.store.SearchableDatastore;
import spark.Request;

import java.io.IOException;

/**
 *
 *
 * @author Daniel M. de Oliveira
 */
public class SimpleDispatcher extends Dispatcher {

    protected final SearchableDatastore connectDatastore;

    public SimpleDispatcher(SearchableDatastore connectDatastore) {
        this.connectDatastore= connectDatastore;
    }

    @Override
    public JsonNode get(String bucket, String key) {
        return connectDatastore.get(bucket,key);
    }

    @Override
    public void addDatatoreStatus(ServerStatusHandler handler, Results r) throws IOException {
        r.add(handler.makeDataStoreStatus("main",connectDatastore));
    }

    @Override
    public boolean handlePost(String bucket, String key, JsonNode value) {
        return connectDatastore.put(bucket,key, value);
    }

    @Override
    public boolean handlePut(String bucket, String key, JsonNode value) {
        return connectDatastore.put(bucket,key, value);
    }

    @Override
    public JsonNode handleGet(String bucket, String key, Request req) {
        return connectDatastore.get(bucket,key);
    }

    @Override
    public JsonNode handleSearch(String bucket, String query) {
        return connectDatastore.search( bucket, query );
    }
}


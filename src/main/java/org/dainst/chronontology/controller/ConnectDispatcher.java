package org.dainst.chronontology.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.dainst.chronontology.handler.ServerStatusHandler;
import org.dainst.chronontology.util.Results;
import org.dainst.chronontology.store.Datastore;
import org.dainst.chronontology.store.SearchableDatastore;
import spark.Request;

import java.io.IOException;


/**
 *
 *
 * @author Daniel M. de Oliveira
 */
public class ConnectDispatcher extends Dispatcher {

    protected final Datastore mainDatastore;
    protected final SearchableDatastore connectDatastore;

    public ConnectDispatcher(Datastore mainDatastore, SearchableDatastore connectDatastore) {
        this.mainDatastore= mainDatastore;
        this.connectDatastore= connectDatastore;
    }

    @Override
    public JsonNode get(String bucket, String key) {
        return mainDatastore.get(bucket,key);
    }

    @Override
    public void addDatatoreStatus(ServerStatusHandler handler, Results r) throws IOException {
        r.add(handler.makeDataStoreStatus("main",mainDatastore));
        r.add(handler.makeDataStoreStatus("connect",connectDatastore));
    }

    @Override
    public boolean handlePost(String bucket, String key, JsonNode value) {
        return (mainDatastore.put(bucket,key, value) & connectDatastore.put(bucket,key, value));
    }

    @Override
    public boolean handlePut(String bucket, String key, JsonNode value) {
        return (mainDatastore.put(bucket,key, value) && connectDatastore.put(bucket,key, value));
    }

    @Override
    public JsonNode handleGet(String bucket, String key, Request req) {
        JsonNode result= shouldBeDirect(req.queryParams("direct"))
                ? mainDatastore.get(bucket,key)
                : connectDatastore.get(bucket,key);
        return result;
    }

    @Override
    public JsonNode handleSearch(String bucket, String query) {
        return connectDatastore.search( bucket, query );
    }

    private boolean shouldBeDirect(final String directParam) {
        return (directParam!=null&&
                directParam.equals("true"));
    }

    public Datastore[] getDatatores() {
        Datastore[] datastores= new Datastore[2];
        datastores[0]= connectDatastore;
        datastores[1]= mainDatastore;
        return datastores;
    }
}

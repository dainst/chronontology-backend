package org.dainst.chronontology.handler.dispatch;

import com.fasterxml.jackson.databind.JsonNode;
import org.dainst.chronontology.handler.ServerStatusHandler;
import org.dainst.chronontology.handler.model.Results;
import org.dainst.chronontology.store.Datastore;
import org.dainst.chronontology.store.SearchableDatastore;
import org.dainst.chronontology.store.VersionedDatastore;
import spark.Request;

import java.io.IOException;


/**
 * The ConnectDispatcher knows two datastores, one of which is
 * the main datastore which holds authoritative versions of documents,
 * and the other is the connect datastore, where documents get send to
 * to get enriched by other applications of the connect infrastructure.
 *
 * @author Daniel M. de Oliveira
 */
public class ConnectDispatcher extends Dispatcher {

    protected final VersionedDatastore mainDatastore;
    protected final SearchableDatastore connectDatastore;

    public ConnectDispatcher(VersionedDatastore mainDatastore, SearchableDatastore connectDatastore) {
        this.mainDatastore= mainDatastore;
        this.connectDatastore= connectDatastore;
    }

    @Override
    public JsonNode dispatchGet(final String bucket, final String key) {
        return dispatchGet(bucket,key,true,null);
    }

    @Override
    public void addDatatoreStatus(ServerStatusHandler handler, Results r) throws IOException {
        r.add(handler.makeDataStoreStatus("main",mainDatastore));
        r.add(handler.makeDataStoreStatus("connect",connectDatastore));
    }

    @Override
    public boolean dispatchPost(final String bucket, final String key, final JsonNode value) {
        return (mainDatastore.put(bucket,key, value) & connectDatastore.put(bucket,key, value));
    }

    @Override
    public boolean dispatchPut(final String bucket, final String key, final JsonNode value) {
        return (mainDatastore.put(bucket,key, value) && connectDatastore.put(bucket,key, value));
    }

    @Override
    public JsonNode dispatchGet(
            final String bucket,
            final String key,
            final Boolean direct,
            final Integer version) {

        if (version!=null)
            return mainDatastore.get(bucket,key,version);

        JsonNode result= direct
                ? mainDatastore.get(bucket,key)
                : connectDatastore.get(bucket,key);
        return result;
    }

    @Override
    public Results dispatchSearch(final String bucket, final String query) {
        return connectDatastore.search( bucket, query );
    }



    public Datastore[] getDatatores() {
        Datastore[] datastores= new Datastore[2];
        datastores[0]= connectDatastore;
        datastores[1]= mainDatastore;
        return datastores;
    }
}

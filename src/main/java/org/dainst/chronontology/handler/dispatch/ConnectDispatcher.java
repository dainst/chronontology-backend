package org.dainst.chronontology.handler.dispatch;

import com.fasterxml.jackson.databind.JsonNode;
import org.dainst.chronontology.handler.ServerStatusHandler;
import org.dainst.chronontology.handler.model.Query;
import org.dainst.chronontology.handler.model.Results;
import org.dainst.chronontology.store.Datastore;
import org.dainst.chronontology.store.ElasticsearchDatastore;
import org.dainst.chronontology.store.FilesystemDatastore;

import java.io.IOException;
import java.util.List;


/**
 * The ConnectDispatcher knows two datastores, one of which is
 * the main datastore which holds authoritative versions of documents,
 * and the other is the connect datastore, where documents get send to
 * to get enriched by other applications of the connect infrastructure.
 *
 * @author Daniel M. de Oliveira
 */
public class ConnectDispatcher extends Dispatcher {

    protected final FilesystemDatastore mainDatastore;
    protected final ElasticsearchDatastore connectDatastore;

    public ConnectDispatcher(FilesystemDatastore mainDatastore, ElasticsearchDatastore connectDatastore) {
        this.mainDatastore= mainDatastore;
        this.connectDatastore= connectDatastore;
    }

    @Override
    public JsonNode dispatchGet(final String bucket, final String key) {
        return dispatchGet(bucket,key,true,null);
    }

    @Override
    public void addDatatoreStatus(ServerStatusHandler handler, Results r) throws IOException {
        r.add(handler.makeDataStoreStatus("connect","elasticsearch",connectDatastore));
        r.add(handler.makeDataStoreStatus("main","filesystem",mainDatastore));
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

        if (version != null)
            return mainDatastore.get(bucket, key, version);

        JsonNode result = direct
                ? mainDatastore.get(bucket, key)
                : connectDatastore.get(bucket, key);
        return result;
    }

    @Override
    public Results dispatchSearch(final String bucket, final Query query) {
        return connectDatastore.search(bucket, query);
    }

    @Override
    public Datastore[] getDatastores() {
        Datastore[] datastores = new Datastore[2];
        datastores[0] = connectDatastore;
        datastores[1] = mainDatastore;
        return datastores;
    }

    @Override
    public Datastore getDatastoreByClass(Class c) {
        if(c.isInstance(mainDatastore)){
            return mainDatastore;
        }
        if(c.isInstance(connectDatastore)){
            return connectDatastore;
        }
        return null;
    }
}

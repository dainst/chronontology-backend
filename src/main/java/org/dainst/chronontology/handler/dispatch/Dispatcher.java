package org.dainst.chronontology.handler.dispatch;

import com.fasterxml.jackson.databind.JsonNode;
import org.dainst.chronontology.handler.ServerStatusHandler;
import org.dainst.chronontology.handler.model.Query;
import org.dainst.chronontology.handler.model.Results;
import org.dainst.chronontology.store.Datastore;
import spark.Request;

import java.io.IOException;
import java.util.List;

/**
 * Dispatcher are used to proxy datastore access calls issued
 * by handlers. Concrete implementations of Dispatchers can delegate
 * these calls to one or more datastores.
 *
 * @author Daniel M. de Oliveira
 */
public abstract class Dispatcher {

    abstract public JsonNode dispatchGet(String bucket, String key);

    abstract public boolean dispatchPost(final String bucket, final String key, final JsonNode value);

    abstract public boolean dispatchPut(final String bucket, final String key, final JsonNode value);

    abstract public JsonNode dispatchGet(final String bucket, final String key, final Boolean direct,
                                         final Integer version);

    abstract public Results dispatchSearch(String bucket, final Query query);

    abstract public void addDatatoreStatus(ServerStatusHandler handler, Results r) throws IOException;

    abstract public Datastore[] getDatastores();

    abstract public Datastore getDatastoreByClass(Class c);
}

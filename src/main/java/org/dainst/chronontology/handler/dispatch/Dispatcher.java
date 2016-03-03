package org.dainst.chronontology.handler.dispatch;

import com.fasterxml.jackson.databind.JsonNode;
import org.dainst.chronontology.handler.ServerStatusHandler;
import org.dainst.chronontology.handler.model.Results;
import spark.Request;

import java.io.IOException;

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
    abstract public JsonNode dispatchGet(final String bucket, final String key, Request req);
    abstract public Results dispatchSearch(String bucket, String query);
    abstract public void addDatatoreStatus(ServerStatusHandler handler, Results r) throws IOException;
}

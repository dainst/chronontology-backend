package org.dainst.chronontology.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.log4j.Logger;
import org.dainst.chronontology.handler.ServerStatusHandler;
import org.dainst.chronontology.util.Results;
import spark.Request;

import java.io.IOException;

/**
 * Template methods are used so subclasses can implement
 * concrete behaviour for datastore handling.
 *
 * @author Daniel M. de Oliveira
 */
public abstract class Dispatcher {

    final static Logger logger = Logger.getLogger(Dispatcher.class);

    // Template methods
    abstract public JsonNode dispatchGet(String bucket, String key);
    abstract public boolean dispatchPost(final String bucket, final String key, final JsonNode value);
    abstract public boolean dispacthPut(final String bucket, final String key, final JsonNode value);
    abstract public JsonNode dispatchGet(final String bucket, final String key, Request req);
    abstract public JsonNode dispatchSearch(String bucket, String query);
    abstract public void addDatatoreStatus(ServerStatusHandler handler, Results r) throws IOException;
    // Template methods
}

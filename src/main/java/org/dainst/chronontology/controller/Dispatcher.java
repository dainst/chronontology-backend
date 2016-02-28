package org.dainst.chronontology.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.log4j.Logger;
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
    abstract protected JsonNode get(String bucket, String key);
    abstract protected boolean handlePost(final String bucket, final String key, final JsonNode value);
    abstract protected boolean handlePut(final String bucket, final String key, final JsonNode value);
    abstract protected JsonNode handleGet(final String bucket, final String key, Request req);
    abstract protected JsonNode handleSearch(String bucket, String query);
    abstract protected void addDatatoreStatus(ServerStatusHandler handler, Results r) throws IOException;
    // Template methods
}

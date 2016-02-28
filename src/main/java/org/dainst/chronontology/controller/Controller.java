package org.dainst.chronontology.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.dainst.chronontology.store.Connector;
import org.dainst.chronontology.util.JsonUtils;
import org.dainst.chronontology.util.Results;
import org.eclipse.jetty.server.Server;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.dainst.chronontology.Constants.*;

/**
 * Template methods are used so subclasses can implement
 * concrete behaviour for datastore handling.
 *
 * @author Daniel M. de Oliveira
 */
public abstract class Controller {

    final static Logger logger = Logger.getLogger(Controller.class);

    public RightsValidator getRightsValidator() { // TODO should be a property of the router
        return rightsValidator;
    }

    private RightsValidator rightsValidator= null;

    public Controller(RightsValidator validator) {
        this.rightsValidator= validator;
    }

    // Template methods
    abstract protected JsonNode get(String bucket, String key);
    abstract protected boolean handlePost(final String bucket, final String key, final JsonNode value);
    abstract protected boolean handlePut(final String bucket, final String key, final JsonNode value);
    abstract protected JsonNode handleGet(final String bucket, final String key, Request req);
    abstract protected JsonNode handleSearch(String bucket, String query);
    abstract protected void addDatatoreStatus(ServerStatusHandler handler, Results r) throws IOException;
    // Template methods
}

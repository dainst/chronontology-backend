package org.dainst.chronontology.controller;

import com.fasterxml.jackson.databind.JsonNode;
import spark.Request;

/**
 * @author Daniel M. de Oliveira
 */
public class Handler {

    public Handler(Dispatcher dispatcher, RightsValidator rightsValidator) {
        this.dispatcher = dispatcher;
        this.rightsValidator= rightsValidator;
    }

    protected final Dispatcher dispatcher;
    protected final RightsValidator rightsValidator;


    public boolean userAccessLevelSufficient(Request req, JsonNode n, RightsValidator.Rights rights) {
        if (n.get("dataset")!=null &&
                !rightsValidator.hasPermission(req.attribute("user"),
                        n.get("dataset").toString().replaceAll("\"",""),rights)) {
            return false;
        }
        return true;
    }
}

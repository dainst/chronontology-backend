package org.dainst.chronontology.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.dainst.chronontology.controller.Dispatcher;
import spark.Request;

/**
 * @author Daniel M. de Oliveira
 */
public class Handler {

    protected static final String ID = ":id";

    public Handler(Dispatcher dispatcher, RightsValidator rightsValidator) {
        this.dispatcher = dispatcher;
        this.rightsValidator= rightsValidator;
    }

    protected final Dispatcher dispatcher;
    protected final RightsValidator rightsValidator;


    public boolean userAccessLevelSufficient(Request req, DocumentModel dm, RightsValidator.Operation operation) {
        if (dm.getDataset()!=null &&
                !rightsValidator.hasPermission(req.attribute("user"),
                        dm.getDataset(), operation)) {
            return false;
        }
        return true;
    }
}

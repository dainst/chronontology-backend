package org.dainst.chronontology.controller;

import com.fasterxml.jackson.databind.JsonNode;
import spark.Request;

/**
 * @author Daniel M. de Oliveira
 */
public class Handler {

    public Handler(Controller controller) {
        this.controller = controller;
    }

    protected final Controller controller;

    public boolean userAccessLevelSufficient(Request req, JsonNode n, RightsValidator.Rights rights) {
        if (n.get("dataset")!=null &&
                !controller.getRightsValidator().hasPermission(req.attribute("user"),
                        n.get("dataset").toString().replaceAll("\"",""),rights)) {
            return false;
        }
        return true;
    }
}

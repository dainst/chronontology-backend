package org.dainst.chronontology.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.dainst.chronontology.controller.Dispatcher;
import org.dainst.chronontology.util.JsonUtils;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static org.dainst.chronontology.Constants.HTTP_FORBIDDEN;
import static org.dainst.chronontology.Constants.HTTP_NOT_FOUND;

/**
 * @author Daniel M. de Oliveira
 */
public class GetHandler extends Handler {

    public GetHandler(Dispatcher dispatcher, RightsValidator rightsValidator) {
        super(dispatcher,rightsValidator);
    }

    public Object handle(
            final String typeName,
            final Request req,
            final Response res) throws IOException {

        JsonNode result= dispatcher.dispatchGet(typeName,req.params(ID),req);
        if (result==null){
            res.status(HTTP_NOT_FOUND);
            return "";
        }
        if (!super.userAccessLevelSufficient(req,result, RightsValidator.Operation.READ)) {
            res.status(HTTP_FORBIDDEN);
            return JsonUtils.json();
        }
        return result;
    }

}

package org.dainst.chronontology.handler;

import org.dainst.chronontology.handler.dispatch.Dispatcher;
import org.dainst.chronontology.handler.model.Document;
import org.dainst.chronontology.handler.model.RightsValidator;
import org.dainst.chronontology.util.JsonUtils;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static org.dainst.chronontology.Constants.HTTP_FORBIDDEN;
import static org.dainst.chronontology.Constants.HTTP_NOT_FOUND;

/**
 * @author Daniel M. de Oliveira
 */
public class GetDocumentHandler extends DocumentHandler {

    public GetDocumentHandler(Dispatcher dispatcher, RightsValidator rightsValidator) {
        super(dispatcher,rightsValidator);
    }

    @Override
    public Object handle(
            final Request req,
            final Response res) throws IOException {

        Document result= Document.from(dispatcher.dispatchGet(type(req), simpleId(req),req));
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

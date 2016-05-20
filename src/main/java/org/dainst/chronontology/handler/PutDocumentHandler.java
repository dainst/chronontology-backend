package org.dainst.chronontology.handler;

import org.dainst.chronontology.handler.dispatch.Dispatcher;
import org.dainst.chronontology.handler.model.Document;
import org.dainst.chronontology.handler.model.RightsValidator;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static org.dainst.chronontology.Constants.*;
import static org.dainst.chronontology.util.JsonUtils.json;

/**
 * @author Daniel M. de Oliveira
 */
public class PutDocumentHandler extends DocumentHandler {

    public PutDocumentHandler(Dispatcher dispatcher, RightsValidator rightsValidator) {
        super(dispatcher, rightsValidator);
    }

    public Object handle(
            final Request req,
            final Response res) throws IOException {


        Document dm= makeDocumentModel(req,res,false);
        if (dm==null) return json();


        int status;
        Document oldDm = Document.from(
                dispatcher.dispatchGet(type(req), simpleId(req)));

        if (oldDm!=null) {

            if (!userAccessLevelSufficient(req,oldDm, RightsValidator.Operation.EDIT)) {
                res.status(HTTP_FORBIDDEN);
                return json();
            } else {
                dm.merge(oldDm);
                status= HTTP_OK;
            }

        } else {
            status= HTTP_CREATED;
        }

        if (!dispatcher.dispatchPut(type(req), simpleId(req),dm.j())) {
            res.status(HTTP_INTERNAL_SERVER_ERROR);
            return json();
        }
        else {
            res.status(status);
            return dm;
        }
    }
}

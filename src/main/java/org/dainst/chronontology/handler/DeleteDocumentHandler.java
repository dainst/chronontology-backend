package org.dainst.chronontology.handler;

import org.dainst.chronontology.handler.dispatch.Dispatcher;
import org.dainst.chronontology.handler.model.Document;
import org.dainst.chronontology.handler.model.RightsValidator;
import org.dainst.chronontology.util.JsonUtils;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static org.dainst.chronontology.Constants.*;
import static org.dainst.chronontology.util.JsonUtils.json;

/**
 * @author Simon Hohl
 */

public class DeleteDocumentHandler extends DocumentHandler {
    public DeleteDocumentHandler(Dispatcher dispatcher, RightsValidator rightsValidator) {
        super(dispatcher,rightsValidator);
    }

    @Override
    public Object handle(
            final Request req,
            final Response res) throws IOException {

        Document deleteDocument = Document.from(dispatcher.dispatchGet(type(req), simpleId(req)));
        Document replacementDocument = Document.from(dispatcher.dispatchGet(type(req), replacedById(req)));

        if(deleteDocument == null){
            res.status(HTTP_NOT_FOUND);
            res.type("text/plain");
            return "Error: " + type(req) + " with ID " + simpleId(req) + " not found.";
        }

        if(replacementDocument == null){
            res.status(HTTP_NOT_FOUND);
            res.type("text/plain");
            return "Error: " + type(req) + " with ID " + replacedById(req) + " not found.";
        }

        if(deleteDocument.getId().equals(replacementDocument.getId())) {
            res.status(HTTP_BAD_REQUEST);
            res.type("text/plain");
            return "Error: data can not replaced by itself: " + simpleId(req);
        }

        if(replacementDocument.getDeleted()){
            res.status(HTTP_BAD_REQUEST);
            res.type("text/plain");
            return "Error: replacement is already marked as deleted: " + replacedById(req);
        }

        deleteDocument.mergeWithDataset(replacementDocument.j());

        if(!dispatcher.dispatchPut(type(req), simpleId(req), deleteDocument.j())){
            res.status(HTTP_INTERNAL_SERVER_ERROR);
            return "";
        }
        else {
            res.status(HTTP_OK);
            return deleteDocument;
        }
    }
}

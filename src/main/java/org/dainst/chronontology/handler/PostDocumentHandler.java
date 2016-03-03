package org.dainst.chronontology.handler;

import org.dainst.chronontology.handler.dispatch.Dispatcher;
import org.dainst.chronontology.handler.model.Document;
import org.dainst.chronontology.handler.model.RightsValidator;
import org.dainst.chronontology.util.JsonUtils;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static org.dainst.chronontology.Constants.*;

/**
 * @author Daniel M. de Oliveira
 */
public class PostDocumentHandler extends DocumentHandler {

    public PostDocumentHandler(Dispatcher dispatcher, RightsValidator rightsValidator) {
        super(dispatcher,rightsValidator);
    }

    @Override
    public Object handle(
            final Request req,
            final Response res) throws IOException {

        Document dm= makeDocumentModel(req,res,true);
        if (dm==null) return JsonUtils.json();

        if (!dispatcher.dispatchPost(req.pathInfo(),dm.getId().replace(req.pathInfo(),""),dm.j()))
            res.status(HTTP_INTERNAL_SERVER_ERROR);
        else {
            res.status(HTTP_CREATED);
        }

        res.header("location", dm.getId());
        return dm;
    }
}

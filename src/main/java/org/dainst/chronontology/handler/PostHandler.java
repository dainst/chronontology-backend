package org.dainst.chronontology.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.codec.binary.Base64;
import org.dainst.chronontology.controller.Dispatcher;
import org.dainst.chronontology.util.JsonUtils;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Random;

import static org.dainst.chronontology.Constants.*;
import static org.dainst.chronontology.util.JsonUtils.*;

/**
 * @author Daniel M. de Oliveira
 */
public class PostHandler extends BaseDocumentHandler {

    public PostHandler(Dispatcher dispatcher, RightsValidator rightsValidator) {
        super(dispatcher,rightsValidator);
    }

    @Override
    public Object handle(
            final Request req,
            final Response res) throws IOException {

        DocumentModel dm= makeDocumentModel(req,res,true);
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

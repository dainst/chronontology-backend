package org.dainst.chronontology.handler;

import org.dainst.chronontology.Constants;
import org.dainst.chronontology.handler.dispatch.Dispatcher;
import org.dainst.chronontology.handler.model.RightsValidator;
import org.dainst.chronontology.handler.model.Results;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel M. de Oliveira
 */
public class SearchDocumentHandler extends DocumentHandler {

    public SearchDocumentHandler(Dispatcher dispatcher, RightsValidator rightsValidator) {
        super(dispatcher,rightsValidator);
    }

    @Override
    public Object handle(
            final Request req,
            final Response res) throws IOException {

        List<String> includes= new ArrayList<String>();
        for (String include:rightsValidator.readableDatasets(req.attribute("user"))) {
            includes.add("dataset:"+include);
        }
        if (req.attribute("user").equals(Constants.USER_NAME_ADMIN))
            includes=null;

        Results results= dispatcher.dispatchSearch(req.pathInfo(),req.queryString(),includes);
        return results;
    }
}

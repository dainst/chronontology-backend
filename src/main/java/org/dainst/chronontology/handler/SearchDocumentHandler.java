package org.dainst.chronontology.handler;

import org.dainst.chronontology.Constants;
import org.dainst.chronontology.handler.dispatch.Dispatcher;
import org.dainst.chronontology.handler.model.Query;
import org.dainst.chronontology.handler.model.Results;
import org.dainst.chronontology.handler.model.RightsValidator;
import spark.Request;
import spark.Response;

import java.io.IOException;

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

        final Query query = Query.fromParams(req.queryMap().toMap());

        if (!req.attribute("user").equals(Constants.USER_NAME_ADMIN)) {
            for (String include:rightsValidator.readableDatasets(req.attribute("user"))) {
                query.addDataset(include);
            }
        }

        Results results = dispatcher.dispatchSearch(req.pathInfo(), query);
        return results;
    }
}

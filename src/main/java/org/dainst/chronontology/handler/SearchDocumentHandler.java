package org.dainst.chronontology.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.dainst.chronontology.controller.Dispatcher;
import org.dainst.chronontology.util.Results;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

        Results results= dispatcher.dispatchSearch(req.pathInfo(),req.queryString());
        removeNodes(results, indicesToRemove(req, results));
        return results;
    }

    private void removeNodes(Results r, List<Integer> indicesToRemove) {
        Collections.reverse(indicesToRemove); // TODO write test
        for (Integer index:indicesToRemove) {
            r.remove(index); // TODO check removal
        }
    }

    private List<Integer> indicesToRemove(Request req, Results r) {
        List<Integer> indicesToRemove = new ArrayList<Integer>();
        int i=0;
        for (final JsonNode n : r.getAll()) {

            if (!userAccessLevelSufficient(req, Document.from(n), RightsValidator.Operation.READ)) {
                indicesToRemove.add(i);
            }
            i++;
        }
        return indicesToRemove;
    }
}

package org.dainst.chronontology.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.dainst.chronontology.controller.Dispatcher;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Daniel M. de Oliveira
 */
public class SearchHandler extends BaseDocumentHandler {

    public SearchHandler(Dispatcher dispatcher, RightsValidator rightsValidator) {
        super(dispatcher,rightsValidator);
    }

    @Override
    public Object handle(
            final Request req,
            final Response res) throws IOException {

        JsonNode searchResults= dispatcher.dispatchSearch(req.pathInfo(),req.queryString());

        ArrayNode resultsNode= (ArrayNode) searchResults.get("results");
        removeNodes(resultsNode, indicesToRemove(req, resultsNode));

        return searchResults;
    }

    private void removeNodes(ArrayNode a, List<Integer> indicesToRemove) {
        Collections.reverse(indicesToRemove); // TODO write test
        for (Integer index:indicesToRemove) {
            a.remove(index); // TODO check removal
        }
    }

    private List<Integer> indicesToRemove(Request req, ArrayNode a) {
        List<Integer> indicesToRemove = new ArrayList<Integer>();
        int i=0;
        for (final JsonNode n : a) {

            if (!userAccessLevelSufficient(req,DocumentModel.from(n), RightsValidator.Operation.READ)) {
                indicesToRemove.add(i);
            }
            i++;
        }
        return indicesToRemove;
    }
}

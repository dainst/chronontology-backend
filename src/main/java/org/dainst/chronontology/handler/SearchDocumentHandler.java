package org.dainst.chronontology.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.dainst.chronontology.handler.dispatch.Dispatcher;
import org.dainst.chronontology.handler.model.Document;
import org.dainst.chronontology.handler.model.RightsValidator;
import org.dainst.chronontology.handler.model.Results;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        String queryString= req.queryString();
        Integer size= null;
        Integer offset= null;
        if (queryString!=null) {
            Matcher sizeMatcher = Pattern.compile("([&|?]size=\\d+)").matcher(queryString);
            while (sizeMatcher.find()) {
                String s = sizeMatcher.group(1);
                size = Integer.parseInt(s.split("=")[1]);
                queryString = queryString.replace(s, "");
            }
            Matcher offsetMatcher = Pattern.compile("([&|?]offset=\\d+)").matcher(queryString);
            while (offsetMatcher.find()) {
                String s = offsetMatcher.group(1);
                offset = Integer.parseInt(s.split("=")[1]);
                queryString = queryString.replace(s, "");
            }
        }
        Results results= dispatcher.dispatchSearch(req.pathInfo(),queryString);
        removeNodes(results, indicesToRemove(req, results));


        trimUntilOffset(results,offset);
        trimToSize(results,size);
        return results;
    }

    private void trimUntilOffset(Results results, Integer offset) {
        if (offset==null) return;
        for (int i=0;i<offset;i++) {
            results.remove(i);
        }
    }

    private void trimToSize(Results results, Integer size) {
        if (size==null) return;
        for (int i=results.getAll().size()-1;i>0;i--) {
            if (i>=size) results.remove(i);
        }
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

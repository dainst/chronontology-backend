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

        Query q = new Query(req.queryString());

        Results results= dispatcher.dispatchSearch(req.pathInfo(),q.queryString);
        removeNodes(results, indicesToRemove(req, results));


        trimUntilOffset(results,q.offset);
        trimToSize(results,q.size);
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

    private class Query {
        public String queryString=null;
        public Integer size= null;
        public Integer offset= null;

        public Query(String qs) {
            if (qs!=null) {
                queryString = qs;
                size= mod("([&|?]size=\\d+)");
                offset= mod("([&|?]offset=\\d+)");
            }
        }

        private Integer mod(String pattern) {
            Integer nr= null;
            Matcher sizeMatcher = Pattern.compile(pattern).matcher(queryString);
            while (sizeMatcher.find()) {
                String s = sizeMatcher.group(1);
                nr = Integer.parseInt(s.split("=")[1]);
                queryString = queryString.replace(s, "");
            }
            return nr;
        }
    }
}

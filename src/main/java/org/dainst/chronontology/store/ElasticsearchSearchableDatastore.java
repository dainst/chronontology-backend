package org.dainst.chronontology.store;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.dainst.chronontology.handler.model.Results;
import org.dainst.chronontology.store.rest.JsonRestClient;
import org.dainst.chronontology.util.JsonUtils;

import java.net.URLDecoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Accesses elastic search via its rest api.
 *
 * @author Daniel M. de Oliveira
 */
public class ElasticsearchSearchableDatastore implements SearchableDatastore {

    private JsonRestClient client;

    private final String indexName;

    @SuppressWarnings("unused")
    private ElasticsearchSearchableDatastore() {indexName=null;};

    public ElasticsearchSearchableDatastore(
            final JsonRestClient client,
            final String indexName) {

        this.indexName= indexName;
        this.client= client;
    }

    /**
     * @param key identifies the item uniquely.
     * @return null if item not found.
     */
    @Override
    public JsonNode get(final String typeName,final String key) {
        return client.get("/" + indexName+ "/" + typeName + "/" + key).get("_source");
    };



    /**
     * @param queryString an elastic search url query string.
     *                    Should contain the only part after
     *                    "_search?".
     * @return a JsonNode with a top level field named results which
     *   is an array containing objects representing the search hits.
     *   The results array can be empty if there where no results.
     *   When errors occur, null gets returned.
     */
    @Override
    public Results search(
            final String typeName,
            final String queryString,
            final List<String> includes) {

        JsonNode q= convert(queryString);
        if (includes!=null) q= include(q,includes);

        JsonNode response= client.post("/" + indexName + "/" + typeName + "/_search",
                q);

        if ((response==null)||
                (response.get("hits")==null)) return null;

        ArrayNode searchHits= (ArrayNode) response.get("hits").get("hits");
        if (searchHits==null)
            return null;

        return makeResults(searchHits);
    }

    private Results makeResults(ArrayNode searchHits) {
        Results results = SearchableDatastore.results();
        for (JsonNode o:searchHits) {
            results.add(o.get("_source"));
        }
        return results;
    }

    @Override
    public boolean isConnected() {
        return (client.get("/")!=null);
    }

    @Override
    public boolean put(final String typeName,final String key,final JsonNode value) {
        return (client.post("/" + indexName + "/" + typeName + "/" + key, value)!=null);
    }

    @Override
    public void remove(final String typeName, final String key) {
        client.delete("/" + indexName + "/" + typeName + "/" + key);
    }

    private JsonNode convert(String queryString) {
        JsonNode j= JsonUtils.json("{\"query\":{\"bool\":{ \"must\" : [ {\"bool\":{ \"should\" : [] }},{\"bool\":{ \"should\" : [] }} ] }}}");

        if (queryString==null) return j;
        Query q = new Query(URLDecoder.decode(queryString));
        queryString= normalize(q.queryString);
        if (q.size!=null) ((ObjectNode)j).put("size",q.size);
        if (q.from!=null) ((ObjectNode)j).put("from",q.from);

        Matcher m = Pattern.compile("[A-Za-z0-9:%@]+").matcher(queryString);
        while (m.find()) {
            String s = m.group();
            array(j,0).add(boolTerm(s.replace("%","/")));
        }
        return j;
    }

    private ArrayNode array(JsonNode n,int index) {
        return (ArrayNode) n.get("query").get("bool").get("must").get(index).get("bool").get("should");
    }

    private class Query {
        public String queryString=null;
        public Integer size= null;
        public Integer from= null;

        public Query(String qs) {
            if (qs!=null) {
                queryString = qs;
                size= mod("([&|?]size=\\d+)");
                from= mod("([&|?]from=\\d+)");
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



    private JsonNode boolTerm(String pair) {
        String field="";
        String value="";
        if (pair.split(":").length<2) {
            field="_all";
            value=pair;
        } else {
            field= pair.split(":")[0];
            value= pair.split(":")[1];
        }
        return JsonUtils.json("{ \"term\" : {\""+field+"\" : \""+value+"\"} }");
    }

    private String normalize(String s) {
        String q= s+" ";
        q=q.replace("\"","");
        q=q.replaceAll("/","%");
        q=q.replaceFirst("q="," ");
        q=q.replaceAll("&"," ");
        q=q.replaceAll("\\?"," ");
        return q;
    }

    private JsonNode include(JsonNode n, List<String> excludes) {
        ArrayNode b= array(n,1);
        for (String e:excludes) {
            b.add(boolTerm(e));
        }
        return n;
    }
}

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
public class ElasticsearchDatastore implements Datastore {

    private JsonRestClient client;

    private final String indexName;

    @SuppressWarnings("unused")
    private ElasticsearchDatastore() {indexName=null;};

    public ElasticsearchDatastore(
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

    /**
     * @param queryString an elastic search url query string.
     *                    Should contain the only part after
     *                    "_search?".
     * @return a JsonNode with a top level field named results which
     *   is an array containing objects representing the search hits.
     *   The results array can be empty if there where no results.
     *   When errors occur, null gets returned.
     */
    public Results search(
            final String typeName,
            String queryString,
            final List<String> includes) {

        JsonNode requestBody= baseRequestBody();
        requestBody= inflateRequestBody(requestBody,
                URLDecoder.decode((queryString!=null) ? queryString : "" ));
        if (includes!=null) requestBody= incorporateIncludes(requestBody,includes);

        return makeResultsFrom(searchHits(
                client.post("/" + indexName + "/" + typeName + "/_search", requestBody)));
    }

    private ArrayNode searchHits(JsonNode response) {
        if ((response==null)||
                (response.get("hits")==null)) return null;

        ArrayNode searchHits= (ArrayNode) response.get("hits").get("hits");
        if (searchHits==null)
            return null;
        return searchHits;
    }

    private Results makeResultsFrom(final ArrayNode searchHits) {
        if (searchHits==null) return null;

        Results results = new Results("results");
        for (JsonNode o:searchHits) {
            results.add(o.get("_source"));
        }
        return results;
    }

    private JsonNode baseRequestBody() {
        return JsonUtils.json(
                "{\"query\":{\"bool\":{ \"must\" : [ " +
                        "{\"bool\":{ \"should\" : [] }}," +
                        "{\"bool\":{ \"should\" : [] }} " +
                        "] }}}");
    }

    private JsonNode inflateRequestBody(final JsonNode j, final String queryString) {

        if (queryString.isEmpty()) return j;
        Query q = new Query(queryString);
        if (q.size!=null) ((ObjectNode)j).put("size",q.size);
        if (q.from!=null) ((ObjectNode)j).put("from",q.from);

        Matcher m = Pattern.compile("[A-Za-z0-9:%@]+").matcher(
                q.queryString.replace("\"","").replaceAll("/","%"));
        while (m.find()) array(j, 0).add(termQueryElement(m.group().replace("%","/")));
        return j;
    }

    private ArrayNode array(final JsonNode n,final int index) {
        return (ArrayNode) n.get("query").get("bool").get("must").get(index).get("bool").get("should");
    }

    private class Query {
        public String queryString=null;
        public Integer size= null;
        public Integer from= null;

        public Query(final String qs) {
            queryString = qs;
            size= extractElement("([&|?]*size=\\d+)");
            from= extractElement("([&|?]*from=\\d+)");
        }

        private Integer extractElement(final String pattern) {
            Integer nr= null;
            Matcher m = Pattern.compile(pattern).matcher(queryString);
            while (m.find()) {
                String s = m.group(1);
                nr = Integer.parseInt(s.split("=")[1]);
                queryString = queryString.replace(s, "");
            }
            return nr;
        }
    }

    private JsonNode termQueryElement(final String pair) {
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

    private JsonNode incorporateIncludes(final JsonNode n, final List<String> includes) {
        ArrayNode b= array(n,1);
        for (String e:includes) {
            b.add(termQueryElement(e));
        }
        return n;
    }
}

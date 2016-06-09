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
        JsonNode result= client.get("/" + indexName+ "/" + typeName + "/" + key);
        if (result==null) return null;
        return result.get("_source");
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
     * Performs a search for documents in one of the types of an elasticsearch index.
     *
     * @param type search will be only performed on documents of the given <code>type</code>.
     * @param queryString a query string consisting of one or more
     *   query elements, combining the query
     *   elements with "&". A single query element can be one of the following
     *   <ul>
     *   <li>&lt;searchTerm&gt; - search for the given term in all fields</li>
     *   <li>&lt;fieldName&gt;:&lt;searchTerm&gt; - search for the given term in specific fields</li>
     *   <li>size=&lt;size&gt; - size of search result set</li>
     *   <li>from=&lt;startIndex&gt; - search result set starting at index</li>
     *   </ul>
     *   Examples for valid query strings are
     *   <ul>
     *   <li>size=4&from=3&abc</li>
     *   <li>name:anton&abc</li>
     *   <li>anton</li>
     *   <li>size=10&name:anton&dataset:dataset3</li>
     *   </ul>
     *   Asides from "size" and "from", search elements are comined with logical "or", that means
     *   that if two or more query partials are given, documents matching at least one of these
     *   will be included in the result set.
     * @param includes when beeing non empty, the list elements get combined with logical "or". The
     *   <code>boolean</code> result of this will then combined with logical "and" to the query as
     *   specified by <code>queryString</code>. An example is
     *
     *   <code>
     *   queryString -> anton&user=tim
     *   includes.get(0) -> dataset:dataset1
     *   includes.get(1) -> dataset:dataset2
     *   </code>
     *
     *   The search will match all documents which have some value "anton" <b>or</b> some field
     *   named "user" containing a value named "tim" <b>and</b> at the same time meeting the condition
     *   that the document a field named dataset containing either the value "dataset1" <b>or</b> "dataset2".
     *
     * @return a JsonNode with a top level field named results which
     *   is an array containing objects representing the search hits.
     *   The results array can be empty if there where no results.
     *   When errors occur, null gets returned.
     */
    public Results search(
            final String type,
            String queryString,
            final List<String> includes) {

        JsonNode requestBody= baseRequestBody();
        requestBody= inflateRequestBody(requestBody,
                URLDecoder.decode((queryString!=null) ? queryString : "" ));
        if (includes!=null) requestBody= incorporateIncludes(requestBody,includes);

        final JsonNode response = client.post("/" + indexName + "/" + type + "/_search", requestBody);

        return makeResultsFrom(searchHits(response), response.get("hits").get("total").asInt());
    }

    private ArrayNode searchHits(JsonNode response) {
        if ((response==null)||
                (response.get("hits")==null)) return null;

        ArrayNode searchHits= (ArrayNode) response.get("hits").get("hits");
        if (searchHits==null)
            return null;
        return searchHits;
    }

    private Results makeResultsFrom(final ArrayNode searchHits, int total) {
        if (searchHits==null) return null;

        Results results = new Results("results", total);
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
        SizeFromExtractor q = new SizeFromExtractor(queryString);
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

    /**
     * Removes "size" and "from" elements (including possible "&" or "?" prefixes)
     * and provides the result as <code>queryString</code> as well as the size
     * and from values as Integers. When size or from are specified more than once, only
     * the value of their last match is taken.
     */
    private class SizeFromExtractor {

          // the original query string "qs" without "size" or "from" elements.
        public String queryString=null;
          // size as derived from the last size element in queryString, null if not found.
        public Integer size= null;
          // from as derived from the last from element in queryString, null if not found.
        public Integer from= null;

        public SizeFromExtractor(final String qs) {
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

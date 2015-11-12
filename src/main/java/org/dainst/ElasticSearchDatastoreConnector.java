package org.dainst;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


/**
 * @author Daniel M. de Oliveira
 */
public class ElasticSearchDatastoreConnector {

    private String indexName = null;

    private final TransportClient client = new ESClientUtil("elasticsearch","localhost").getClient();

    private ElasticSearchDatastoreConnector() {};

    public ElasticSearchDatastoreConnector(final String indexName) {
        this.indexName= indexName;
    }

    /**
     * @param key identifies the item uniquely.
     * @return null if item not found.
     * @throws IOException
     */
    public JsonNode get(final String typeName,final String key) {
        GetResponse res= client.prepareGet(indexName, typeName,key).execute().actionGet();

        if (res.getSourceAsString()==null) return null;

        JsonNode result;
        try {
            result = new ObjectMapper().readTree(res.getSourceAsString());
        } catch (IOException e) {
            return null;
        }
        return result;
    };


    private String normalizeQueryTerm(String qt) throws UnsupportedEncodingException {
        return java.net.URLDecoder.decode(qt, "UTF-8").replaceAll("\"","");
    }

    /**
     * @param queryString some string like a:b for searching b
     *                    in all records a fields. Can also be just b
     *                    for searching for b in all fields.
     * @param size number of results to fetch. Set to a number < 0
     *             to mark there should be no restriction.
     * @return a JsonNode with a top level field named results which
     *   is an array containing objects representing the search hits.
     */
    public JsonNode search(
            final String typeName,
            final String queryString,
            final int size) throws IOException {

        client.admin().indices().prepareRefresh().execute().actionGet();

        String fieldToSearch= queryString.contains(":") ? queryString.split(":")[0] : "_all";
        String termToSearchFor= queryString.contains(":") ? queryString.split(":")[1] : queryString;

        MatchQueryBuilder tq = QueryBuilders.matchPhraseQuery(
                fieldToSearch, normalizeQueryTerm(termToSearchFor));

        SearchRequestBuilder srb= client.prepareSearch(indexName).setTypes(typeName)
                .setQuery(tq);
        srb.setSize(size);

        return makeResultsNode(srb.execute().actionGet());
    }

    private class Results {
        private JsonNode json;

        public Results() throws IOException {
            json = json("{\"results\":[]}");
        }

        public JsonNode add(final JsonNode jsonToAdd)
                throws JsonProcessingException {
            ArrayNode data=(ArrayNode) json.get("results");
            data.add(jsonToAdd);
            return json;
        }

        public JsonNode j() {
            return json;
        }
    }

    private JsonNode makeResultsNode(final SearchResponse response) throws IOException {
        Results results = new Results();
        for (SearchHit hit:response.getHits().getHits()) {
            results.add(json(hit.getSourceAsString()));
        }
        return results.j();
    }

    private JsonNode json(final String s) throws IOException {
        return new ObjectMapper().readTree(s);
    }



    public void put(final String typeName,final String key,final JsonNode value) {
        IndexResponse ir= client.prepareIndex(indexName, typeName)
                .setSource(value.toString()).setId(key).execute().actionGet();
    }

    public void delete(final String typeName,final String key) {
        client.prepareDelete(indexName,typeName,key).execute();
    }
}

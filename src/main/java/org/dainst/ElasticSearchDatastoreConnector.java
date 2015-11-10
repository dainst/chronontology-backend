package org.dainst;

import static org.dainst.C.*;

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
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;

import java.io.IOException;


/**
 * @author Daniel M. de Oliveira
 */
public class ElasticSearchDatastoreConnector {

    private String indexName = null;

    private final TransportClient client = new ESClientUtil("elasticsearch_daniel","localhost").getClient();

    private ElasticSearchDatastoreConnector() {};

    public ElasticSearchDatastoreConnector(final String indexName) {
        this.indexName= indexName;
    }

    /**
     * @param key identifies the item uniquely.
     * @return null if item not found.
     * @throws IOException
     */
    public JsonNode get(final String key) {
        GetResponse res= client.prepareGet(indexName, TYPE_NAME,key).execute().actionGet();

        if (res.getSourceAsString()==null) return null;

        JsonNode result;
        try {
            result = new ObjectMapper().readTree(res.getSourceAsString());
        } catch (IOException e) {
            return null;
        }
        return result;
    };

    /**
     *
     * @param queryString some string like a:b&c:d&e:f
     * @param size number of results to fetch.
     *             Set false to mark there should be no restriction.
     * @return a JsonNode with a top level field named results which
     *   is an array containing objects representing the search hits.
     */
    public JsonNode search(
            final String queryString,
            final Integer size) throws IOException {

        client.admin().indices().prepareRefresh().execute().actionGet();

        String[] queryTerms = queryString.split(":");
        MatchQueryBuilder tq = QueryBuilders.matchPhraseQuery(
                queryTerms[0], java.net.URLDecoder.decode(queryTerms[1], "UTF-8"));

        SearchRequestBuilder srb= client.prepareSearch(indexName).setTypes(TYPE_NAME)
                .setQuery(tq);
        if (size!=null) srb.setSize(size);

        return makeResultsNode(srb.execute().actionGet());
    }

    private class ResultJson {
        private JsonNode json;

        public ResultJson() throws IOException {
            json = json("{\"results\":[]}");
        }

        public JsonNode addNodeToResultsArray(final JsonNode jsonToAdd)
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
        ResultJson results = new ResultJson();
        for (SearchHit hit:response.getHits().getHits()) {
            results.addNodeToResultsArray(json(hit.getSourceAsString()));
        }
        return results.j();
    }

    private JsonNode json(final String s) throws IOException {
        return new ObjectMapper().readTree(s);
    }



    public void put(final String key,final JsonNode value) {
        IndexResponse ir= client.prepareIndex(indexName, TYPE_NAME)
                .setSource(value.toString()).setId(key).execute().actionGet();
    }

    public void delete(final String key) {
        client.prepareDelete(indexName,TYPE_NAME,key).execute();
    }
}

package org.dainst.chronontology.store;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.dainst.chronontology.handler.model.Query;
import org.dainst.chronontology.handler.model.Results;
import org.dainst.chronontology.store.rest.JsonRestClient;
import org.dainst.chronontology.util.JsonUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Accesses elastic search via its rest api.
 *
 * @author Daniel M. de Oliveira
 * @author Sebastian Cuy
 */
public class ElasticsearchDatastore implements Datastore {

    private static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchDatastore.class);

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
        JsonNode result = client.get("/" + indexName+ "/" + typeName + "/" + key);
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
     * The provided query object determines the query string, size and from values
     * and the datasets that are used for filtering the result.
     *
     * The given query string supports full lucene query syntax.
     *
     * Only results that belong to one of the given datasets are returned.
     * If no datasets are given, every matching resource regardless of its dataset
     * will be returned.
     *
     * @param type search will be only performed on documents of the given <code>type</code>.
     * @param query a query object consisting of query string and other parameters
     * @return a JsonNode with two top level fields. "results"
     *   is an array containing objects representing the search hits.
     *   "total" gives the total number of hits in the datastore.
     *   The results array can be empty if there were no results.
     *   When errors occur, null gets returned.
     */
    public Results search(
            final String type,
            Query query) {

        JsonNode json = JsonUtils.json(buildESRequest(query));
        final JsonNode response = client.post("/" + indexName + "/" + type + "/_search", json);

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

    private String buildESRequest(Query query) {

        SearchSourceBuilder sb = SearchSourceBuilder.searchSource()
                .from(query.getFrom())
                .size(query.getSize());

        QueryBuilder qb = QueryBuilders.queryStringQuery(query.getQ());
        sb.query(qb);

        if (!query.getDatasets().isEmpty()) {
            BoolFilterBuilder fb = FilterBuilders.boolFilter();
            for (String dataset : query.getDatasets()) {
                fb.should(FilterBuilders.termFilter("dataset", dataset));
            }
            sb.postFilter(fb);
        }

        return sb.toString();
    }

}

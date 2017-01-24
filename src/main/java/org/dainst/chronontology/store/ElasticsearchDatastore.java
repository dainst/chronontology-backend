package org.dainst.chronontology.store;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.dainst.chronontology.handler.model.Query;
import org.dainst.chronontology.handler.model.Results;
import org.dainst.chronontology.store.rest.JsonRestClient;
import org.dainst.chronontology.util.JsonUtils;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequestBuilder;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

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

        String searchSegment;
        if (type.equals("_")) {
            searchSegment = "_search";
        } else {
            searchSegment = type + "/_search";
        }

        JsonNode json = JsonUtils.json(buildESRequest(query));
        final JsonNode response = client.post("/" + indexName + "/" + searchSegment, json);

        return makeResultsFrom(
                searchHits(response),
                response.get("hits").get("total").asInt(),
                response.get("aggregations")
                // trimFacetOutOfAggregations(response.get("aggregations"))
        );
    }

    public JsonNode clearIndex() {
        return client.delete("/" + indexName);
    }

    public JsonNode initializeIndex() {
        return client.put("/" + indexName, JsonUtils.json());
    }

    public JsonNode postMapping(String type, JsonNode mapping) {
        return client.post("/" + indexName + "/" + type + "/_mapping", mapping);
    }

    private ArrayNode searchHits(JsonNode response) {
        if ((response==null)||
                (response.get("hits")==null)) return null;

        ArrayNode searchHits= (ArrayNode) response.get("hits").get("hits");
        if (searchHits==null)
            return null;
        return searchHits;
    }

    private Results makeResultsFrom(final ArrayNode searchHits, int total, final JsonNode facets) {
        if (searchHits==null) return null;

        Results results = new Results("results", total);
        for (JsonNode o:searchHits) {
            results.add(o.get("_source"));
        }
        if(facets != null){
            results.addFacet(facets);
        }
        return results;
    }

    private String buildESRequest(Query query) {

        SearchSourceBuilder sb = SearchSourceBuilder.searchSource()
                .from(query.getFrom())
                .size(query.getSize());

        if (query.getSortField() != null) {
            sb.sort(query.getSortField());
        }

        if (query.getPart() != "") {
            sb.fetchSource(query.getPart(), null);
        }

        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        qb.must(QueryBuilders.queryStringQuery(query.getQ()));

        for (String facet : query.getFacets()) {
            sb.aggregation(AggregationBuilders.terms(facet).field(facet));
        }

        if (!query.getDatasets().isEmpty()) {
            BoolQueryBuilder fq = QueryBuilders.boolQuery();
            for (String dataset : query.getDatasets()) {
                fq = fq.should(QueryBuilders.termQuery("dataset", dataset));
            }
            qb = qb.filter(fq);
        }

        for (Map.Entry<String, String> entry : query.getFacetQueries().entrySet()) {
            qb = qb.filter(QueryBuilders.termQuery(entry.getKey(), entry.getValue()));
            // TODO: Parse for possible integer values?
        }

        sb.query(qb);

        return sb.toString();
    }

}

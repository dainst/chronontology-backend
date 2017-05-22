package org.dainst.chronontology.store;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.dainst.chronontology.handler.model.Query;
import org.dainst.chronontology.handler.model.Results;
import org.dainst.chronontology.store.rest.JsonRestClient;
import org.dainst.chronontology.util.JsonUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static java.lang.Math.sqrt;

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
        ObjectNode doc = (ObjectNode) value;
        doc.put("boost", this.calcBoost(value));
        return (client.post("/" + indexName + "/" + typeName + "/" + key, doc)!=null);
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

    private double calcBoost(JsonNode value) {
        double boost = 1;
        JsonNode resource = value.get("resource");
        if (resource.has("hasTimespan")) boost *= 2;
        if (resource.has("hasCoreArea")
                || resource.has("isNamedAfter")
                || resource.has("spatiallyPartOf"))
            boost *= 2;
        if (resource.has("relations"))
            boost *= sqrt(resource.get("relations").size());
        if (resource.has("provenacne")
                && resource.get("provenance").toString().equals("aat"))
            boost *= 0.5;
        return boost;
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
            sb.fetchSource(query.getPart().split(","), null);
        }

        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        qb.must(QueryBuilders.queryStringQuery(query.getQ()));
        qb.should(QueryBuilders.matchQuery("resource.names.en", query.getQ()).boost(2));
        qb.should(QueryBuilders.matchQuery("resource.names.de", query.getQ()).boost(2));

        for (String facet : query.getFacets()) {
            sb.aggregation(AggregationBuilders.terms(facet).field(facet));
        }

        if (!query.getDatasets().isEmpty()) {
            BoolQueryBuilder fq = QueryBuilders.boolQuery();
            for (String dataset : query.getDatasets()) {
                fq = fq.should(QueryBuilders.termQuery("dataset", dataset));
            }
            fq = fq.mustNot(QueryBuilders.termQuery("deleted", true));
            qb = qb.filter(fq);
        }

        for (Map.Entry<String, String> entry : query.getFacetQueries().entrySet()) {
            qb = qb.filter(QueryBuilders.termQuery(entry.getKey(), entry.getValue()));
            // TODO: Parse for possible integer values?
        }

        for (String existsQuery : query.getExistsQueries()) {
            qb = qb.filter(QueryBuilders.existsQuery(existsQuery));
        }

        ScoreFunctionBuilder scoreFunction = ScoreFunctionBuilders.fieldValueFactorFunction("boost");
        FunctionScoreQueryBuilder fqb = new FunctionScoreQueryBuilder(qb).add(scoreFunction);

        sb.query(fqb);

        return sb.toString();
    }

}

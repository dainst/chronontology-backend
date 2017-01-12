package org.dainst.chronontology.handler.model;

import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.aggregations.*;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filters.FiltersAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregator;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregatorFactory;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The search query that is to be handled by the datastore
 *
 * @author Sebastian Cuy
 */
public class Query {

    private static final String DEFAULT_Q = "*";
    private static final int DEFAULT_FROM = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final String[] DEFAULT_FACETS = new String[0];
    private static final String[] DEFAULT_FACET_QUERY = new String[0];
    private static final String FACET_QUERY_SPLIT_REGEX = ":";

    private final String q;

    private final int from;

    private final int size;

    private String sortField = null;

    private final String[] facets;

    private final Map<String, String> facetQueries;

    private List<String> datasets = new ArrayList<String>();


    /**
     * @param q the query string (default "*")
     * @param from the offset of the results to be returned from
     *             the total result set (default 0)
     * @param size the maximum number results to be returned
     *             (default 10)
     * @param facets list of facets which are to be returned with the query
     * @param facetQueries list of facet queries, restricting the search result
     */

    public Query(String q, int from, int size, String[] facets, String[] facetQueries){
        this.q = q.isEmpty() ? "*" : stripQuotes(q);
        this.from = from;
        this.size = size;

        for(int i = 0; i < facets.length; i++){
            facets[i] = stripQuotes(facets[i]);
        }
        this.facets = facets;

        this.facetQueries = new HashMap<>();
        for(String facetQuery : facetQueries) {
            String[] parts = facetQuery.split(FACET_QUERY_SPLIT_REGEX,2);
            this.facetQueries.put(
                    stripQuotes(parts[0]),
                    stripQuotes(parts[1]).replace("\\:", ":")
            );
        }
    }

    public Query(String q, int from, int size) {
        this(q, from, size, DEFAULT_FACETS, DEFAULT_FACET_QUERY);
    }

    /**
     * Factory method for constructing a query from a parameter map
     * @param params a parameter map, e.g. parsed from an HTTP requests GET parameters
     * @return a new query
     */
    public static Query fromParams(Map<String, String[]> params) {
        String q = params.containsKey("q") ? params.get("q")[0] : DEFAULT_Q;
        int from = params.containsKey("from") ? Integer.parseInt(params.get("from")[0]) : DEFAULT_FROM;
        int size = params.containsKey("size") ? Integer.parseInt(params.get("size")[0]) : DEFAULT_SIZE;
        String[] facets = params.containsKey("facet") ? params.get("facet") : DEFAULT_FACETS;
        String[] facetQueries = params.containsKey("fq") ? params.get("fq") : DEFAULT_FACET_QUERY;
        return new Query(q, from, size, facets, facetQueries);
    }

    private String stripQuotes(final String q) {
        String ret = q;
        if (q.startsWith("\"") && q.endsWith("\"")) {
            ret = ret.substring(1);
            ret = ret.substring(0,ret.length()-1);
        }
        return ret;
    }

    public String getQ() {
        return q;
    }

    public int getFrom() {
        return from;
    }

    public int getSize() {
        return size;
    }

    public String[] getFacets() {
        return facets;
    }

    public Map<String, String> getFacetQueries(){
        return facetQueries;
    }

    /**
     * Add a dataset that should be used as a filter when interpreting the query
     * @param dataset the dataset name
     */
    public void addDataset(String dataset) {
        this.datasets.add(dataset);
    }

    public List<String> getDatasets() {
        return datasets;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

}

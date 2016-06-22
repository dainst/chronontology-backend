package org.dainst.chronontology.handler.model;

import java.util.ArrayList;
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

    private final String q;

    private final int from;

    private final int size;

    private List<String> datasets = new ArrayList<String>();

    /**
     * @param q the query string (default "*")
     * @param from the offset of the results to be returned from
     *             the total result set (default 0)
     * @param size the maximum number results to be returned
     *             (default 10)
     */
    public Query(String q, int from, int size) {
        this.q = q.isEmpty() ? "*" : q;
        this.from = from;
        this.size = size;
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
        return new Query(q, from, size);
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

}

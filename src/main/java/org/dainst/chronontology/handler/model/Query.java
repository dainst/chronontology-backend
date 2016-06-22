package org.dainst.chronontology.handler.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by scuy on 22/06/16.
 */
public class Query {

    private static final String DEFAULT_Q = "*";
    private static final int DEFAULT_FROM = 0;
    private static final int DEFAULT_SIZE = 10;

    private final String q;

    private final int from;

    private final int size;

    private List<String> datasets = new ArrayList<String>();

    public Query(String q, int from, int size) {
        this.q = q.isEmpty() ? "*" : q;
        this.from = from;
        this.size = size;
    }

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

    public void addDataset(String dataset) {
        this.datasets.add(dataset);
    }

    public List<String> getDatasets() {
        return datasets;
    }

}

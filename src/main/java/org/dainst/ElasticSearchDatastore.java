package org.dainst;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;

/**
 * @author Daniel M. de Oliveira
 */
public class ElasticSearchDatastore {

    public static final String TYPE_NAME = "period";
    public static final String INDEX_NAME = "jeremy";

    private final TransportClient client = new ESClientUtil("elasticsearch_daniel","localhost").getClient();

    public String get(String key) {
        GetResponse res= client.prepareGet(INDEX_NAME, TYPE_NAME,key).execute().actionGet();
        return res.getSourceAsString();
    };

    public void put(String key,String value) {
        IndexResponse ir= client.prepareIndex(INDEX_NAME, TYPE_NAME)
                .setSource(value).setId(key).execute().actionGet();
    }

    public void delete(String key) {
        client.prepareDelete(INDEX_NAME,TYPE_NAME,key).execute();
    }
}

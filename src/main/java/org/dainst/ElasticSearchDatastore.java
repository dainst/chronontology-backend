package org.dainst;

import static org.dainst.C.*;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;


/**
 * @author Daniel M. de Oliveira
 */
public class ElasticSearchDatastore {

    private String indexName = null;

    private final TransportClient client = new ESClientUtil("elasticsearch_daniel","localhost").getClient();

    private ElasticSearchDatastore() {};

    public ElasticSearchDatastore(String indexName) {
        this.indexName= indexName;
    }

    public String get(String key) {
        GetResponse res= client.prepareGet(indexName, TYPE_NAME,key).execute().actionGet();
        return res.getSourceAsString();
    };

    public void put(String key,String value) {
        IndexResponse ir= client.prepareIndex(indexName, TYPE_NAME)
                .setSource(value).setId(key).execute().actionGet();
    }

    public void delete(String key) {
        client.prepareDelete(indexName,TYPE_NAME,key).execute();
    }
}

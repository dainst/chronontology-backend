package org.dainst;

import static org.dainst.C.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;

import java.io.IOException;


/**
 * @author Daniel M. de Oliveira
 */
public class ElasticSearchDatastoreConnector {

    private String indexName = null;

    private final TransportClient client = new ESClientUtil("elasticsearch_daniel","localhost").getClient();

    private ElasticSearchDatastoreConnector() {};

    public ElasticSearchDatastoreConnector(String indexName) {
        this.indexName= indexName;
    }

    public JsonNode get(String key) throws IOException {
        GetResponse res= client.prepareGet(indexName, TYPE_NAME,key).execute().actionGet();

        ObjectMapper mapper = new ObjectMapper();
        if (res.getSourceAsString()==null) return null;

        return mapper.readTree(res.getSourceAsString());
    };

    public void put(String key,JsonNode value) {
        IndexResponse ir= client.prepareIndex(indexName, TYPE_NAME)
                .setSource(value.toString()).setId(key).execute().actionGet();
    }

    public void delete(String key) {
        client.prepareDelete(indexName,TYPE_NAME,key).execute();
    }
}

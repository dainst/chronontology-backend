package org.dainst.chronontology.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.log4j.Logger;
import org.dainst.chronontology.Results;
import org.dainst.chronontology.connect.JsonRestClient;

import java.io.IOException;


/**
 * Accesses elastic search via its rest api.
 *
 * @author Daniel M. de Oliveira
 */
public class ESRestSearchableKeyValueStore implements JsonSearchableBucketKeyValueStore {


    final static Logger logger = Logger.getLogger(ESRestSearchableKeyValueStore.class);

    private JsonRestClient client;

    private final String indexName;

    @SuppressWarnings("unused")
    private ESRestSearchableKeyValueStore() {indexName=null;};

    public ESRestSearchableKeyValueStore(
            final JsonRestClient client,
            final String indexName) {

        this.indexName= indexName;
        this.client= client;
    }


    /**
     * @param key identifies the item uniquely.
     * @return null if item not found.
     * @throws IOException
     */
    @Override
    public JsonNode get(final String typeName,final String key) {
        return client.get("/" + indexName+ "/" + typeName + "/" + key).get("_source");
    };



    /**
     * @param queryString an elastic search url query string.
     *                    Should contain the only part after
     *                    "_search?".
     * @return a JsonNode with a top level field named results which
     *   is an array containing objects representing the search hits.
     *   The results array can be empty if there where no results.
     *   When errors occur, null gets returned.
     */
    @Override
    public JsonNode search(
            final String typeName,
            final String queryString) {

        JsonNode response= client.get("/" + indexName + "/" + typeName + "/_search?" +
                queryString);
        if ((response==null)||
                (response.get("hits")==null)) return null;

        ArrayNode searchHits= (ArrayNode) response.get("hits").get("hits");
        if (searchHits==null)
            return null;

        return makeResults(searchHits);
    }

    private JsonNode makeResults(ArrayNode searchHits) {
        Results results = new Results("results");
        for (JsonNode o:searchHits) {
            try {
                results.add(o.get("_source"));
            } catch (JsonProcessingException e) {
                return null;
            }
        }
        return results.j();
    }

    @Override
    public boolean isConnected() {
        if (client.get("/")==null) return false;
        return true;
    }



    @Override
    public void put(final String typeName,final String key,final JsonNode value) {
        client.post("/" + indexName + "/" + typeName + "/" + key, value);
    }

    @Override
    public void remove(final String typeName, final String key) {
        client.delete("/" + indexName + "/" + typeName + "/" + key);
    }
}

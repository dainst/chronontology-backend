package org.dainst.chronontology.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.log4j.Logger;
import org.dainst.chronontology.handler.model.Results;
import org.dainst.chronontology.store.rest.JsonRestClient;

import java.io.IOException;


/**
 * Accesses elastic search via its rest api.
 *
 * @author Daniel M. de Oliveira
 */
public class ESRestSearchableDatastore implements SearchableDatastore {

    private JsonRestClient client;

    private final String indexName;

    @SuppressWarnings("unused")
    private ESRestSearchableDatastore() {indexName=null;};

    public ESRestSearchableDatastore(
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
    public Results search(
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

    private Results makeResults(ArrayNode searchHits) {
        Results results = SearchableDatastore.results();
        for (JsonNode o:searchHits) {
            results.add(o.get("_source"));
        }
        return results;
    }

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
}

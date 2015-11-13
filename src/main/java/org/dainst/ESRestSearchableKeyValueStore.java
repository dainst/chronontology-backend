package org.dainst;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.log4j.Logger;

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
        Results results = new Results();
        for (JsonNode o:searchHits) {
            try {
                results.add(o.get("_source"));
            } catch (JsonProcessingException e) {
                return null;
            }
        }
        return results.j();
    }

    private class Results {
        private JsonNode json;

        public Results() {
            try {
                json = new ObjectMapper().readTree("{\"results\":[]}");
            } catch (IOException e) {} // WILL NOT HAPPEN
        }

        public JsonNode add(final JsonNode jsonToAdd)
                throws JsonProcessingException {
            ArrayNode data=(ArrayNode) json.get("results");
            data.add(jsonToAdd);
            return json;
        }

        public JsonNode j() {
            return json;
        }
    }

    public void put(final String typeName,final String key,final JsonNode value) {
        client.post("/" + indexName + "/" + typeName + "/" + key, value);
    }

    public void remove(final String typeName, final String key) {
        client.delete("/" + indexName + "/" + typeName + "/" + key);
    }
}

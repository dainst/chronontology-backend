package org.dainst.chron.store;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Daniel M. de Oliveira
 */
public interface JsonSearchableBucketKeyValueStore extends JsonBucketKeyValueStore {

    /**
     * @param bucket
     * @param queryString an elasticsearch type query string.
     *                    Contains the bit after "_search?".
     * @return
     */
    JsonNode search(final String bucket, final String queryString);
}

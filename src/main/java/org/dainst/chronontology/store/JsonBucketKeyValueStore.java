package org.dainst.chronontology.store;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Daniel m. de Oliveira
 */
public interface JsonBucketKeyValueStore {

    /**
     * Creates or updates the item with key.
     *
     * @param bucket
     * @param key
     * @param value
     */
    void put(final String bucket,final String key,final JsonNode value);

    /**
     * @param bucket
     * @param key
     */
    void remove(final String bucket, final String key);

    /**
     * @param bucket
     * @param key
     * @return value
     */
    JsonNode get(final String bucket,final String key);
}

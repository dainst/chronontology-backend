package org.dainst.chronontology.store;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Daniel M. de Oliveira
 */
public interface VersionedDatastore extends Datastore {

    /**
     * @param bucket
     * @param key
     * @param version
     * @return null if the requested version is not available
     */
    JsonNode get(final String bucket, final String key, final Integer version);
}

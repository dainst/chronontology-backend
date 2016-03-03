package org.dainst.chronontology.store;

import com.fasterxml.jackson.databind.JsonNode;
import org.dainst.chronontology.util.Results;

/**
 * @author Daniel M. de Oliveira
 */
public interface SearchableDatastore extends Datastore {

    /**
     * @param bucket
     * @param queryString an elasticsearch type query string.
     *                    Contains the bit after "_search?".
     * @return
     */
    Results search(final String bucket, final String queryString);
}

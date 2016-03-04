package org.dainst.chronontology.store;

import org.dainst.chronontology.handler.model.Results;

/**
 * @author Daniel M. de Oliveira
 */
public interface SearchableDatastore extends Datastore {

    /**
     * Implementations must make sure the search
     * results are returned in the same order every time.
     *
     * @param bucket
     * @param queryString an elasticsearch type query string.
     *                    Contains the bit after "_search?".
     * @return
     */
    Results search(final String bucket, final String queryString);
}

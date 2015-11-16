package org.dainst.chronontology.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Daniel M. de Oliveira
 */
public interface DocumentModel {

    /**
     * Merges an old node representing an older
     * version of a document into the current one.
     *
     * @param oldNode
     * @return
     */
    DocumentModel merge(final JsonNode oldNode);

    /**
     * @return the current node.
     */
    JsonNode j();
}

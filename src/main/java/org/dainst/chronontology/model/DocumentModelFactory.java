package org.dainst.chronontology.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Creates concrete instances of DocumentModel.
 *
 * @author Daniel M. de Oliveira
 */
public class DocumentModelFactory {

    /**
     * Creates a DocumentModel instance for a type named
     * typeName.
     *
     * @param typeName
     * @param id
     * @param jsonNode
     * @return null if no DocumentModel for typeName has been found.
     */
    public static DocumentModel create(String typeName,String id,JsonNode jsonNode) {
        return new GenericTypeDocumentModel(typeName,id,jsonNode);
    }
}

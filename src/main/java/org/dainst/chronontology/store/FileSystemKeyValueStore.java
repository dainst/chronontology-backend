package org.dainst.chronontology.store;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Key value store where the values are of type JsonNode
 * and get stored on a file system.
 *
 * @author Daniel M. de Oliveira
 */
public class FileSystemKeyValueStore implements JsonBucketKeyValueStore {

    private static final String SUFFIX = ".txt";
    private final String baseFolder;

    public FileSystemKeyValueStore(String baseFolder) {
        this.baseFolder=baseFolder;
    }

    /**
     * @param key
     * @return null if there exists no item for key.
     */
    public JsonNode get(
            final String typeName,
            final String key) {

        String content= "";
        try {
            content = new String(Files.readAllBytes(
                    path(typeName,key)));
        } catch (IOException e) {
            return null;
        }

        JsonNode n= null;
        try {
            n = new ObjectMapper().readTree(content);
        } catch (IOException e) {
            return null;
        }
        return n;
    };

    private Path path(final String typeName, final String key) {
        return Paths.get(baseFolder + typeName + "/" + key + SUFFIX);
    }

    /**
     * Stores or updates the item for key.
     * @param key
     * @param value
     */
    public void put(
            final String typeName,
            String key,
            JsonNode value) {
        try {
            Files.write( path(typeName,key)
                    , value.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void remove(String bucket, String key) {
        throw new NotImplementedException();
    }
}

package org.dainst;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

/**
 * Key value store where the values are of type JsonNode
 * and get stored on a file system.
 *
 * @author Daniel M. de Oliveira
 */
public class FileSystemDatastoreConnector {

    private static final String SUFFIX = ".txt";
    private final String baseFolder;

    public FileSystemDatastoreConnector(String baseFolder) {
        this.baseFolder=baseFolder;
    }

    /**
     * @param key
     * @return null if there exists no item for key.
     * @throws IOException
     */
    public JsonNode get(String key) throws IOException {

        String content= "";
        try {
            content = new String(Files.readAllBytes(Paths.get(baseFolder + key + SUFFIX)));
        } catch (NoSuchFileException e) {
            return null;
        }

        return new ObjectMapper().readTree(content);
    };

    /**
     * Stores or updates the item for key.
     * @param key
     * @param value
     */
    public void put(String key,JsonNode value) {
        try {
            Files.write(Paths.get(baseFolder + key + SUFFIX), value.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

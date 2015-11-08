package org.dainst;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Daniel M. de Oliveira
 */
public class FileSystemDatastoreConnector {

    private static final String SUFFIX = ".txt";
    private final String baseFolder;

    public FileSystemDatastoreConnector(String baseFolder) {
        this.baseFolder=baseFolder;
    }

    public JsonNode get(String key) throws IOException {

        String content="";
        try {
            content = new String(Files.readAllBytes(Paths.get(baseFolder + key + SUFFIX)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(content);
    };

    public void put(String key,JsonNode value) {
        try {
            Files.write(Paths.get(baseFolder + key + SUFFIX), value.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package org.dainst;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Daniel M. de Oliveira
 */
public class FileSystemDatastore {

    private static final String SUFFIX = ".txt";
    private final String baseFolder;

    public FileSystemDatastore(String baseFolder) {
        this.baseFolder=baseFolder;
    }

    public String get(String key) {

        String content="";
        try {
            content = new String(Files.readAllBytes(Paths.get(baseFolder + key + SUFFIX)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    };

    public void put(String key,String value) {
        try {
            Files.write(Paths.get(baseFolder + key + SUFFIX), value.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

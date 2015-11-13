package org.dainst.chronontology.store;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertEquals;

/**
 * @author Daniel M. de Oliveira
 */
public class FileSystemKeyValueStoreTest {

    private static final String TYPE_NAME = "period";
    private static final String BASE_FOLDER = "src/test/resources/";

    @AfterClass
    public static void afterClass() {
        new File(BASE_FOLDER + TYPE_NAME +"/a.txt").delete();
    }

    private JsonNode sampleJson(String sampleFieldValue) throws IOException {
        return new ObjectMapper().readTree
                ("{\"a\":\"" + sampleFieldValue + "\"}");
    }

    @Test
    public void putAndGet() throws IOException {

        FileSystemKeyValueStore store = new FileSystemKeyValueStore(BASE_FOLDER);
        store.put(TYPE_NAME,"a",sampleJson("a"));
        assertEquals(store.get(TYPE_NAME,"a"), sampleJson("a"));
    }
}

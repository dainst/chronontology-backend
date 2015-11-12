package org.dainst;

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
public class FileSystemDatastoreConnectorComponentTest {

    private static final String BASE_FOLDER = "src/test/resources/";

    @AfterClass
    public static void afterClass() {
        new File(BASE_FOLDER + "a.txt").delete();
    }

    private JsonNode sampleJson(String sampleFieldValue) throws IOException {
        return new ObjectMapper().readTree
                ("{\"a\":\"" + sampleFieldValue + "\"}");
    }

    @Test
    public void putAndGet() throws IOException {

        FileSystemDatastoreConnector store = new FileSystemDatastoreConnector(BASE_FOLDER);
        store.put("a",sampleJson("a"));
        assertEquals(store.get("a"), sampleJson("a"));
    }
}
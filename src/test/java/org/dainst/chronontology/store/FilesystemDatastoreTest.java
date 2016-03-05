package org.dainst.chronontology.store;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.dainst.chronontology.JsonTestUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertEquals;

/**
 * @author Daniel M. de Oliveira
 */
public class FilesystemDatastoreTest {

    private static final String TYPE_NAME= "typename";
    private static final String BASE_FOLDER = "src/test/resources/datastoretest/";
    private static final FileSystemDatastore store = new FileSystemDatastore(BASE_FOLDER);

    @AfterClass
    public static void afterClass() throws IOException {
        FileUtils.deleteDirectory(new File(BASE_FOLDER));
    }

    @Test
    public void putAndGet() throws IOException {
        store.put(TYPE_NAME,"a", JsonTestUtils.sampleDocument("a"));
        assertEquals(store.get(TYPE_NAME,"a"), JsonTestUtils.sampleDocument("a"));
    }
}

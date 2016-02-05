package org.dainst.chronontology;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dainst.chronontology.connect.JsonRestClient;
import org.dainst.chronontology.store.ESServerTestUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.testng.Assert.fail;
import static org.dainst.chronontology.TestConstants.*;

/**
 * @author Daniel M. de Oliveira
 */
public class ESClientTestUtil {

    private static final JsonRestClient esClient = new JsonRestClient(ESServerTestUtil.getUrl());

    public static String getIndexName() {
        return TEST_INDEX;
    }

    public static JsonRestClient getClient() {
        return esClient;
    }

    private static JsonNode loadTestTypeMapping(String path) {
        JsonNode n= null;
        try {
            String content = new String(Files.readAllBytes(
                    Paths.get(path)));
            n= new ObjectMapper().readTree(content);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return n;
    }

    public static void createEsTypeAndMapping() {
        esClient.post("/"+ TEST_INDEX +"/"+ TestConstants.TEST_TYPE,
                loadTestTypeMapping(TestConstants.TEST_FOLDER+"mapping.json"));
    }

    public static void deleteESTypeAndMapping() {
        esClient.delete("/"+ TEST_INDEX +"/"+TestConstants.TEST_TYPE);
    }
}

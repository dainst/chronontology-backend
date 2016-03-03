package org.dainst.chronontology.it;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import org.dainst.chronontology.TestConstants;
import org.dainst.chronontology.store.rest.JsonRestClient;
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
        esClient.put("/"+ TEST_INDEX +"/_mapping/"+ TestConstants.TEST_TYPE,
                loadTestTypeMapping(TestConstants.TEST_FOLDER+"mapping.json"));
    }

    public static void deleteESTypeAndMapping() {
        esClient.delete("/"+ TEST_INDEX +"/"+TestConstants.TEST_TYPE);
        esClient.delete("/"+ TEST_INDEX +"/"+TestConstants.TEST_TYPE);
    }

    public static void refreshES() {
        RequestBody body = RequestBody.create(TestConstants.JSON, "{}");
        Request.Builder b = new Request.Builder()
                .url(ESServerTestUtil.getUrl()+ "/" + ESClientTestUtil.getIndexName() + "/_refresh").post(body);
        try {
            new OkHttpClient().newCall(b.build()).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

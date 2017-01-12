package org.dainst.chronontology.it;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.*;
import org.dainst.chronontology.TestConstants;
import org.dainst.chronontology.store.rest.JsonRestClient;
import org.dainst.chronontology.store.ESServerTestUtil;
import org.dainst.chronontology.util.JsonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.testng.Assert.fail;
import static org.dainst.chronontology.TestConstants.*;

/**
 * @author Daniel M. de Oliveira
 */
public class ESClientTestUtil {

    private static final JsonRestClient esClient = new JsonRestClient(ESServerTestUtil.getUrl(),new OkHttpClient(),false);

    public static String getIndexName() {
        return TEST_INDEX;
    }

    public static void deleteESTypeAndMapping() {
        esClient.delete("/"+ TEST_INDEX);
    }

    public static void refreshES() {
        RequestBody body = RequestBody.create(TestConstants.JSON, "{}");
        Request.Builder b = new Request.Builder()
                .url(ESServerTestUtil.getUrl()+ "/" + ESClientTestUtil.getIndexName() + "/_refresh").post(body);

        Response response;
        ResponseBody responseBody = null;
        try {
            response=new OkHttpClient().newCall(b.build()).execute();
            responseBody=response.body();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (responseBody!=null)
                try {
                    responseBody.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
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
        esClient.put("/"+ TEST_INDEX +"/", JsonUtils.json());
        System.out.println(esClient.put("/"+ TEST_INDEX +"/_mapping/"+ TestConstants.TEST_TYPE,
                loadTestTypeMapping(TestConstants.TEST_FOLDER+"mapping.json")));
    }
}

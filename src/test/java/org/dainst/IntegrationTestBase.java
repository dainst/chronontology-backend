package org.dainst;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.*;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import spark.Spark;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.fail;

/**
 * @author Daniel M. de Oliveira
 */
public class IntegrationTestBase {

    private static final String TEST_FOLDER = "src/test/resources/";

    protected static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    protected static final String URL = "http://0.0.0.0:4567";
    protected static final OkHttpClient client = new OkHttpClient();

    protected static final ElasticSearchDatastoreConnector connectDatastore
            = new ElasticSearchDatastoreConnector("jeremy_test");

    protected static final FileSystemDatastoreConnector mainDatastore
            = new FileSystemDatastoreConnector(TEST_FOLDER);
    protected static JsonNode jsonNode(String s) throws IOException {
        return new ObjectMapper().readTree(s);
    }

    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        startServer();
    }

    @AfterClass
    public static void afterClass() throws InterruptedException {
        stopServer();
    }

    @AfterMethod
    public static void afterMethod() {
        cleanDatastores();
    }


    protected static final void cleanDatastores() {

        new File(TEST_FOLDER + "1.txt").delete();
        new File(TEST_FOLDER + "2.txt").delete();
        new File(TEST_FOLDER + "3.txt").delete();

        connectDatastore.delete("1");
        connectDatastore.delete("2");
        connectDatastore.delete("3");
    }

    protected static void startServer() throws InterruptedException {

        new Router(
                mainDatastore,
                connectDatastore
        );
        Thread.sleep(200);
    }

    protected static void stopServer() throws InterruptedException {
        Thread.sleep(200);
        Spark.stop();
    }

    /**
     *
     * @param path
     * @param method
     * @param json
     * @return null if a JsonNode could not get generated properly
     *   from the response body.
     */
    private JsonNode restApi(String path,String method, JsonNode json) {

        Request.Builder b = getBuilder(method, json).url(URL + path);

        Response response = null;
        try {
            response = client.newCall(b.build()).execute();
            String body= response.body().string();
            if (body.isEmpty()) return null;
            return jsonNode(body);
        } catch (IOException e) {
            fail(e.getMessage());
            return null;
        }
    }

    protected Request.Builder getBuilder(String method, JsonNode json) {
        Request.Builder b = new Request.Builder();

        if (method.equals("GET")) {
            b.get();
        } else {
            RequestBody body = RequestBody.create(JSON, json.toString());
            if (method.equals("POST")) {
                b.post(body);
            }
            if (method.equals("PUT")) {
                b.put(body);
            }
        }
        return b;
    }


    protected JsonNode post(String path, JsonNode json) {
        return restApi(path,"POST",json);
    }

    protected JsonNode put(String path, JsonNode json) throws IOException {
        return restApi(path,"PUT",json);
    }

    protected JsonNode get(String path) throws IOException {
        return restApi(path,"GET",null);
    }

    protected String route(String id) {
        return "/"+ C.TYPE_NAME+"/"+id;
    }

    protected JsonNode sampleJson(String sampleFieldValue) throws IOException {
        return new ObjectMapper().readTree
                ("{\"a\":\"" + sampleFieldValue + "\"}");
    }



    protected void jsonAssertEquals(JsonNode actual,JsonNode expected) {
        try {
            JSONAssert.assertEquals(
                    expected.toString(),
                    actual.toString(), false);
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }
}

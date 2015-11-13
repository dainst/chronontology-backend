package org.dainst;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.*;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import spark.Spark;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.fail;

/**
 * @author Daniel M. de Oliveira
 */
public class IntegrationTestBase {

    protected static final String USER_NAME = "admin";
    protected static final String PASS_WORD = "s3cr3t";
    protected static final String TYPE_NAME = "period";

    private static final String TEST_FOLDER = "src/test/resources/";

    protected static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    protected static final String URL = "http://0.0.0.0:4567";
    protected static final JsonRestClient client = new JsonRestClient(URL);

    protected static final OkHttpClient ok= new OkHttpClient();


    protected static final String ES_URL= "http://localhost:9200";
    protected static final String INDEX_NAME = "jeremy_test";
    private static final JsonRestClient esClient = new JsonRestClient(ES_URL);
    protected static final ESRestSearchableKeyValueStore connectDatastore
            = new ESRestSearchableKeyValueStore(esClient,INDEX_NAME);


    protected static final FileSystemKeyValueStore mainDatastore
            = new FileSystemKeyValueStore(TEST_FOLDER);
    protected static JsonNode json(String s) throws IOException {
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

    @BeforeMethod
    public static void beforeMethod() {
        client.authenticate(USER_NAME, PASS_WORD);
    }

    @AfterMethod
    public static void afterMethod() {
        cleanDatastores();
    }


    protected static final void cleanDatastores() {

        new File(TEST_FOLDER + TYPE_NAME + "/1.txt").delete();
        new File(TEST_FOLDER + TYPE_NAME + "/2.txt").delete();
        new File(TEST_FOLDER + TYPE_NAME + "/3.txt").delete();

        connectDatastore.remove(TYPE_NAME, "1");
        connectDatastore.remove(TYPE_NAME, "2");
        connectDatastore.remove(TYPE_NAME, "3");
    }

    protected static void startServer() throws InterruptedException {

        new Router(
                mainDatastore,
                connectDatastore,
                new String[]{TYPE_NAME},
                new String[]{USER_NAME+":"+PASS_WORD}
        );
        Thread.sleep(200);
    }

    protected static void stopServer() throws InterruptedException {
        Thread.sleep(200);
        Spark.stop();
    }

    protected String route(String id) {
        return "/"+ TYPE_NAME+"/"+id;
    }

    protected JsonNode sampleJson(String sampleFieldValue) {
        JsonNode json= null;
        try {
            json = new ObjectMapper().readTree
                    ("{\"a\":\"" + sampleFieldValue + "\"}");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
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

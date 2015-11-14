package org.dainst.chronontology;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.*;
import org.dainst.chronontology.connect.JsonRestClient;
import org.dainst.chronontology.store.ESRestSearchableKeyValueStore;
import org.dainst.chronontology.store.FileSystemKeyValueStore;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import spark.Spark;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.testng.Assert.fail;

/**
 * @author Daniel M. de Oliveira
 */
public class IntegrationTestBase {

    protected static final String USER_NAME = "admin";
    protected static final String PASS_WORD = "s3cr3t";
    protected static final String TYPE_NAME = "period";
    protected static final String TYPE_ROUTE = "/" + TYPE_NAME + "/";
    protected static final String INDEX_NAME = "jeremy_test";
    private static final String TEST_FOLDER = "src/test/resources/";
    protected static final String URL = "http://0.0.0.0:4567";
    protected static final String ES_URL= "http://localhost:9200";


    protected static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    protected static final JsonRestClient client = new JsonRestClient(URL);

    protected static final OkHttpClient ok= new OkHttpClient();


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

    private JsonNode loadTestTypeMapping(String path) {
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

    private void createEsTypeAndMapping() {
        esClient.post("/"+INDEX_NAME+"/"+TYPE_NAME,
                loadTestTypeMapping(TEST_FOLDER+"mapping.json"));
    }

    private void deleteESTypeAndMapping() {
        esClient.delete("/"+INDEX_NAME+"/"+TYPE_NAME);
    }


    @BeforeMethod
    public void beforeMethod() {
        client.authenticate(USER_NAME, PASS_WORD);
        createEsTypeAndMapping();
    }

    @AfterMethod
    public void afterMethod() {
        cleanFileSystemDatastore();
        deleteESTypeAndMapping();
    }


    protected static final void cleanFileSystemDatastore() {

        for (File f : new File(TEST_FOLDER+TYPE_NAME).listFiles())
            if (f.getName().endsWith(".txt")) f.delete();
    }

    protected static void startServer() throws InterruptedException {

        Controller controller= new Controller(
                mainDatastore,connectDatastore);

        new Router(
                controller,
                new String[]{TYPE_NAME},
                new String[]{USER_NAME+":"+PASS_WORD}
        );
        Thread.sleep(1000);
    }

    protected static void stopServer() throws InterruptedException {
        Thread.sleep(200);
        Spark.stop();
    }

    protected JsonNode sampleJson(final String sampleFieldValue) {
        JsonNode json= null;
        try {
            json = new ObjectMapper().readTree
                    ("{\"a\":\"" + sampleFieldValue + "\"}");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    protected void jsonAssertEquals(final JsonNode actual,final JsonNode expected) {
        try {
            JSONAssert.assertEquals(
                    expected.toString(),
                    actual.toString(), false);
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }

    protected String idOf(final JsonNode n) {
        return (String) n.get("@id").textValue();
    }
}

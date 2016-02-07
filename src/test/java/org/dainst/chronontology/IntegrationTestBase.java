package org.dainst.chronontology;

import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import org.dainst.chronontology.connect.JsonRestClient;
import org.dainst.chronontology.store.ESRestSearchableKeyValueStore;
import org.dainst.chronontology.store.ESServerTestUtil;
import org.dainst.chronontology.store.FileSystemKeyValueStore;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import spark.Spark;

import java.io.File;


/**
 * @author Daniel M. de Oliveira
 */
public class IntegrationTestBase {

    protected static final String USER_NAME = "admin";
    protected static final String PASS_WORD = "s3cr3t";

    protected static final String TYPE_ROUTE = "/" + TestConstants.TEST_TYPE + "/";

    protected static final String URL = "http://0.0.0.0:4567";



    protected static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    protected static final JsonRestClient client = new JsonRestClient(URL);

    protected static final OkHttpClient ok= new OkHttpClient();



    protected static final ESRestSearchableKeyValueStore connectDatastore
            = new ESRestSearchableKeyValueStore(ESClientTestUtil.getClient(),ESClientTestUtil.getIndexName());


    protected static final FileSystemKeyValueStore mainDatastore
            = new FileSystemKeyValueStore(TestConstants.TEST_FOLDER);





    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        ESServerTestUtil.startElasticSearchServer();
        startServer();
    }

    @AfterClass
    public static void afterClass() throws InterruptedException {
        stopServer();
        ESServerTestUtil.stopElasticSearchServer();
    }




    @BeforeMethod
    public void beforeMethod() {
        client.authenticate(USER_NAME, PASS_WORD);
        ESClientTestUtil.createEsTypeAndMapping();
    }

    @AfterMethod
    public void afterMethod() {
        cleanFileSystemDatastore();
        ESClientTestUtil.deleteESTypeAndMapping();
    }


    protected static final void cleanFileSystemDatastore() {

        for (File f : new File(TestConstants.TEST_FOLDER+TestConstants.TEST_TYPE).listFiles())
            if (f.getName().endsWith(".txt")) f.delete();
    }



    protected static void startServer() throws InterruptedException {

        Controller controller= new ConnectController(
                mainDatastore,connectDatastore);

        new Router(
                controller,
                new String[]{TestConstants.TEST_TYPE},
                new String[]{USER_NAME+":"+PASS_WORD}
        );
        Thread.sleep(1000);
    }

    protected static void stopServer() throws InterruptedException {
        Thread.sleep(200);
        Spark.stop();
    }


    protected String idOf(final JsonNode n) {
        return (String) n.get("@id").textValue();
    }
}

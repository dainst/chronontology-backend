package org.dainst.chronontology.it;

import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.okhttp.OkHttpClient;
import org.dainst.chronontology.App;
import org.dainst.chronontology.TestConstants;
import org.dainst.chronontology.config.*;
import org.dainst.chronontology.util.JsonRestClient;
import org.dainst.chronontology.controller.ConnectController;
import org.dainst.chronontology.store.ESRestSearchableDatastore;
import org.dainst.chronontology.store.ESServerTestUtil;
import org.dainst.chronontology.store.FileSystemDatastore;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import spark.Spark;

import java.io.File;
import java.util.Properties;


/**
 * @author Daniel M. de Oliveira
 */
public class IntegrationTestBase {

    protected static final String TYPE_ROUTE = "/" + TestConstants.TEST_TYPE + "/";

    // A client speaking to the rest api of the app under design
    protected static final JsonRestClient client = new JsonRestClient(TestConstants.SERVER_URL);

    // To allow direct data manipulation for testing purposes
    protected static ESRestSearchableDatastore connectDatastore = null;
    // To allow direct data manipulation for testing purposes
    protected static FileSystemDatastore mainDatastore = null;


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
        client.authenticate(TestConstants.USER_NAME_ADMIN, TestConstants.PASS_WORD);
        ESClientTestUtil.createEsTypeAndMapping(); // TODO seems to not be necessary anymore
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


    private static AppConfig makeAppConfig() {

        Properties props= new Properties();
        props.put("serverPort",TestConstants.SERVER_PORT);
        props.put("datastores.0.indexName",ESClientTestUtil.getIndexName());
        props.put("datastores.0.url",ESServerTestUtil.getUrl());
        props.put("datastores.1.path",TestConstants.TEST_FOLDER);
        props.put("datastores.1.type", ConfigConstants.DATASTORE_TYPE_FS);
        props.put("typeNames",TestConstants.TEST_TYPE);
        props.put("credentials", makeCredentials());
        props.put("dataset.dataset1.editor", TestConstants.USER_NAME_1);
        props.put("dataset.dataset1.reader", TestConstants.USER_NAME_3);

        AppConfig config= new AppConfig();
        config.validate(props);
        return config;
    }

    private static String makeCredentials() {
        return TestConstants.USER_NAME_ADMIN + ":" + TestConstants.PASS_WORD
                + "," + TestConstants.USER_NAME_1 + ":" + TestConstants.PASS_WORD
                + "," + TestConstants.USER_NAME_2 + ":" + TestConstants.PASS_WORD
                + "," + TestConstants.USER_NAME_3 + ":" + TestConstants.PASS_WORD;
    }

    protected static void startServer() throws InterruptedException {

        App app=  new AppConfigurator().configure(makeAppConfig());
        ConnectController controller= (ConnectController) app.getRouter().getController();

        mainDatastore= (FileSystemDatastore) controller.getDatatores()[1];
        connectDatastore= (ESRestSearchableDatastore) controller.getDatatores()[0];

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

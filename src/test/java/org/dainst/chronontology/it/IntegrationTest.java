package org.dainst.chronontology.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.FileUtils;
import org.dainst.chronontology.App;
import org.dainst.chronontology.TestConstants;
import org.dainst.chronontology.config.*;
import org.dainst.chronontology.store.rest.JsonRestClient;
import org.dainst.chronontology.handler.dispatch.ConnectDispatcher;
import org.dainst.chronontology.store.ElasticsearchDatastore;
import org.dainst.chronontology.store.ESServerTestUtil;
import org.dainst.chronontology.store.FilesystemDatastore;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import spark.Spark;

import java.io.File;
import java.io.IOException;
import java.util.Properties;


/**
 * @author Daniel M. de Oliveira
 */
public abstract class IntegrationTest {

    protected static final String TYPE_ROUTE = "/" + TestConstants.TEST_TYPE + "/";

    // A client speaking to the rest api of the app under design
    protected static final JsonRestClient client = new JsonRestClient(TestConstants.SERVER_URL);

    // To allow direct data manipulation for testing purposes
    protected static ElasticsearchDatastore connectDatastore = null;
    // To allow direct data manipulation for testing purposes
    protected static FilesystemDatastore mainDatastore = null;


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
    }

    @AfterMethod
    public void afterMethod() throws IOException {
        ESClientTestUtil.deleteESTypeAndMapping();
        FileUtils.deleteDirectory(new File("src/test/resources/period"));
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
        ConnectDispatcher controller= (ConnectDispatcher) app.getController().getDispatcher();

        mainDatastore= (FilesystemDatastore) controller.getDatatores()[1];
        connectDatastore= (ElasticsearchDatastore) controller.getDatatores()[0];

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

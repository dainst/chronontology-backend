import static org.testng.Assert.assertEquals;

import com.squareup.okhttp.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import spark.Spark;

import java.io.File;
import java.io.IOException;

/**
 * @author Daniel M. de Oliveira
 */
public class ChronontologyIntegrationTest extends IntegrationTestBase {

    private static final String TEST_FOLDER = "src/test/resources/";

    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        startServer();
    }

    @AfterClass
    public static void afterClass() throws InterruptedException {
        stopServer();

        new File(TEST_FOLDER + "1.txt").delete();
        new File(TEST_FOLDER + "2.txt").delete();
    }


    @Test
    public void respondWithEnrichedJSONonPost() throws IOException {
        final String json = "{\"a\":\"b\"}";

        final String expectedJson = "{\"a\":\"b\",\"@id\":\"/period/1\"}";
        assertEquals(postJSON("/period/1", json), expectedJson);

    }

    @Test
    public void storeAndRetrieveOneResource() throws IOException {

        final String json = "{\"a\":\"b\"}";
        postJSON("/period/1",json);

        final String expectedJson = "{\"a\":\"b\",\"@id\":\"/period/1\"}";
        assertEquals(getJSON("/period/1"), expectedJson);
    }

    @Test
    public void storeAndRetrieveMoreThanOneResource() throws IOException {

        final String json = "{\"a\":\"b\"}";
        final String json2 = "{\"b\":\"a\"}";
        postJSON("/period/1",json);
        postJSON("/period/2",json2);

        final String expectedJson = "{\"a\":\"b\",\"@id\":\"/period/1\"}";
        assertEquals(getJSON("/period/1"),expectedJson);
    }
}

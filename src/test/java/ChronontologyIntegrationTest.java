import static org.testng.Assert.assertEquals;

import com.squareup.okhttp.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import spark.Spark;

import java.io.IOException;

/**
 * @author Daniel M. de Oliveira
 */
public class ChronontologyIntegrationTest extends IntegrationTestBase {

    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        startServer();
    }

    @AfterClass
    public static void afterClass() throws InterruptedException {
       stopServer();
    }

    @Test
    public void storeAndRetrieveOneResource() throws IOException {

        final String json = "{\"a\":\"b\"}";

        postJSON("/resource/1",json);
        assertEquals(getJSON("/resource/1"),json);
    }

    @Test
    public void storeAndRetrieveMoreThanOneResource() throws IOException {

        final String json = "{\"a\":\"b\"}";
        final String json2 = "{\"b\":\"a\"}";

        postJSON("/resource/1",json);
        postJSON("/resource/2",json2);
        assertEquals(getJSON("/resource/1"),json);
    }
}

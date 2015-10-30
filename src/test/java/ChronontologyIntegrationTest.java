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
public class ChronontologyIntegrationTest {

    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private static final String URL = "http://0.0.0.0:4567";

    private static final OkHttpClient client = new OkHttpClient();

    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        Chronontology.main(null);
        Thread.sleep(200);
    }

    @AfterClass
    public static void afterClass() throws InterruptedException {
        Thread.sleep(200);
        Spark.stop();
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

    private void postJSON(String path,String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(URL + path)
                .post(body)
                .build();
        client.newCall(request).execute();
    }

    private String getJSON(String path) throws IOException {
        Request request = new Request.Builder()
                .url(URL+path)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}

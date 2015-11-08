package org.dainst;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.*;
import spark.Spark;

import java.io.IOException;

/**
 * @author Daniel M. de Oliveira
 */
public class IntegrationTestBase {

    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private static final String URL = "http://0.0.0.0:4567";

    private static final OkHttpClient client = new OkHttpClient();

    protected static void startServer() throws InterruptedException {

        new Router(new FileSystemDatastore("src/test/resources/"));
        Thread.sleep(200);
    }

    protected static void stopServer() throws InterruptedException {
        Thread.sleep(200);
        Spark.stop();
    }

    protected JsonNode postJSON(String path,JsonNode json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json.toString());
        Request request = new Request.Builder()
                .url(URL + path)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.body().string());
        return jsonNode;
    }

    protected JsonNode getJSON(String path) throws IOException {
        Request request = new Request.Builder()
                .url(URL+path)
                .build();
        Response response = client.newCall(request).execute();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.body().string());
        return jsonNode;
    }
}

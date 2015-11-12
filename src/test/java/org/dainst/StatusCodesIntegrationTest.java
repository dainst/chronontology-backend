package org.dainst;

import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.dainst.C.*;
import static org.testng.Assert.assertEquals;

/**
 * @author Daniel M. de Oliveira
 */
public class StatusCodesIntegrationTest extends IntegrationTestBase {

    private Response restApiResponse(String path,String method, JsonNode json) {

        Request.Builder b= getBuilder(method, json).url(URL + path);

        Response response= null;
        try {
            response = client.newCall(b.build()).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }


    @Test
    public void documentNotFound() throws IOException {
        assertEquals(
                restApiResponse(route("1"), "GET", null).code(),
                HTTP_NOT_FOUND
        );
    }

    @Test
    public void documentFound() throws IOException {
        post(route("1"),sampleJson("a"));
        assertEquals(
                restApiResponse(route("1"), "GET", null).code(),
                HTTP_OK
        );
    }

    @Test
    public void oneTimePost() throws IOException {
        assertEquals(
                restApiResponse(route("1"), "POST", sampleJson("b")).code(),
                HTTP_CREATED
        );
    }

    @Test
    public void repeatedPost() throws IOException {

        post(route("1"),sampleJson("a"));
        assertEquals(
                restApiResponse(route("1"), "POST", sampleJson("b")).code(),
                HTTP_FORBIDDEN
        );
    }

    @Test
    public void update() throws IOException {

        post(route("1"),sampleJson("a"));
        assertEquals(
                restApiResponse(route("1"), "PUT", sampleJson("b")).code(),
                HTTP_OK
        );
    }

    @Test
    public void createWithPut() throws IOException {

        assertEquals(
                restApiResponse(route("1"), "PUT", sampleJson("b")).code(),
                HTTP_CREATED
        );
    }
}

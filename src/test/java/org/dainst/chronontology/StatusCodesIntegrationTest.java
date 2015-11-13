package org.dainst.chronontology;


import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Base64;

import static org.dainst.chronontology.Constants.*;
import static org.testng.Assert.assertEquals;

/**
 * @author Daniel M. de Oliveira
 */
public class StatusCodesIntegrationTest extends IntegrationTestBase {

    private OkHttpClient ok= new OkHttpClient();
    private void authorize(String username,String password,Request.Builder b) {
        b.addHeader("Authorization","Basic "+ new String(
                Base64.getEncoder().encode((username+":"+password).getBytes())));
    }

    private Response restApiResponse(String path,String method, JsonNode json,String username,String password) {

        Request.Builder b= client.getBuilder(method, json).url(URL + path);
        authorize(username,password,b);

        Response response= null;
        try {
            response = ok.newCall(b.build()).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private Response restApiResponse(String path,String method, JsonNode json) {
        return restApiResponse(path,method,json,USER_NAME,PASS_WORD);
    }

    @Test
    public void putUnauthorized() throws IOException {
        assertEquals(
                restApiResponse(route("1"), "PUT", json("{}"),USER_NAME,"wrong").code(),
                HTTP_UNAUTHORIZED
        );
    }

    @Test
    public void postUnauthorized() throws IOException {
        assertEquals(
                restApiResponse(route("1"), "POST", json("{}"),USER_NAME,"wrong").code(),
                HTTP_UNAUTHORIZED
        );
    }

    @Test
    public void deleteUnauthorized() throws IOException {
        assertEquals(
                restApiResponse(route("1"), "DELETE", json("{}"),USER_NAME,"wrong").code(),
                HTTP_UNAUTHORIZED
        );
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
        client.post(route("1"),sampleJson("a"));
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

        client.post(route("1"),sampleJson("a"));
        assertEquals(
                restApiResponse(route("1"), "POST", sampleJson("b")).code(),
                HTTP_FORBIDDEN
        );
    }

    @Test
    public void update() throws IOException {

        client.post(route("1"),sampleJson("a"));
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

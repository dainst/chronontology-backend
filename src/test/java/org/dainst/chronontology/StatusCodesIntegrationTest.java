package org.dainst.chronontology;


import com.squareup.okhttp.OkHttpClient;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.dainst.chronontology.Constants.*;
import static org.testng.Assert.assertEquals;

/**
 * @author Daniel M. de Oliveira
 */
public class StatusCodesIntegrationTest extends ResponseIntegrationTestBase {

    @Test
    public void putUnauthorized() throws IOException {
        assertEquals(
            rest(TYPE_ROUTE, "PUT", json(),USER_NAME,"wrong").code(),
            HTTP_UNAUTHORIZED
        );
    }

    @Test
    public void postUnauthorized() throws IOException {
        assertEquals(
            rest(TYPE_ROUTE, "POST", json(),USER_NAME,"wrong").code(),
            HTTP_UNAUTHORIZED
        );
    }

    @Test
    public void deleteUnauthorized() throws IOException {
        assertEquals(
            rest(TYPE_ROUTE, "DELETE", json(),USER_NAME,"wrong").code(),
            HTTP_UNAUTHORIZED
        );
    }


    @Test
    public void documentNotFound() throws IOException {
        assertEquals(
            rest(TYPE_ROUTE+"1", "GET", null).code(),
            HTTP_NOT_FOUND
        );
    }

    @Test
    public void documentFound() throws IOException {
        String id= idOf(client.post(TYPE_ROUTE,sampleJson("a")));
        assertEquals(
            rest(id, "GET", null).code(),
            HTTP_OK
        );
    }

    @Test
    public void oneTimePost() throws IOException {
        assertEquals(
            rest(TYPE_ROUTE, "POST", sampleJson("b")).code(),
            HTTP_CREATED
        );
    }

    @Test
    public void update() throws IOException {

        String id= idOf(client.post(TYPE_ROUTE,sampleJson("a")));
        assertEquals(
                rest(id, "PUT", sampleJson("b")).code(),
                HTTP_OK
        );
    }

    @Test
    public void createWithPut() throws IOException {

        assertEquals(
            rest(TYPE_ROUTE+"1", "PUT", sampleJson("b")).code(),
            HTTP_CREATED
        );
    }
}

package org.dainst.chronontology.it;


import org.dainst.chronontology.TestConstants;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.dainst.chronontology.Constants.*;
import static org.dainst.chronontology.util.JsonUtils.json;
import static org.dainst.chronontology.it.ResponseUtil.*;
import static org.testng.Assert.assertEquals;

/**
 * @author Daniel de Oliveira
 */
public class StatusCodesIntegrationTest extends IntegrationTest {

    @Test
    public void putUnauthorized() throws IOException {
        assertEquals(
            getResponse(TYPE_ROUTE, "PUT", json(), TestConstants.USER_NAME_ADMIN,"wrong").code(),
            HTTP_UNAUTHORIZED
        );
    }

    @Test
    public void postUnauthorized() throws IOException {
        assertEquals(
            getResponse(TYPE_ROUTE, "POST", json(), TestConstants.USER_NAME_ADMIN,"wrong").code(),
            HTTP_UNAUTHORIZED
        );
    }

    @Test
    public void deleteUnauthorized() throws IOException {
        assertEquals(
            getResponse(TYPE_ROUTE, "DELETE", json(), TestConstants.USER_NAME_ADMIN,"wrong").code(),
            HTTP_UNAUTHORIZED
        );
    }


    @Test
    public void documentNotFound() throws IOException {
        assertEquals(
            getResponse(TYPE_ROUTE+"1", "GET").code(),
            HTTP_NOT_FOUND
        );
    }

    @Test
    public void documentFound() throws IOException {
        String id= idOf(client.post(TYPE_ROUTE,json()));
        assertEquals(
            getResponse(TYPE_ROUTE+id, "GET").code(),
            HTTP_OK
        );
    }

    @Test
    public void oneTimePost() throws IOException {
        assertEquals(
            getResponse(TYPE_ROUTE, "POST", json()).code(),
            HTTP_CREATED
        );
    }

    @Test
    public void postWithMalformedJSON() throws IOException {
        assertEquals(
                getResponse(TYPE_ROUTE, "POST", "{").code(),
                HTTP_BAD_REQUEST
        );
    }

    @Test
    public void putWithMalformedJSON() throws IOException {

        String id= idOf(client.post(TYPE_ROUTE,json()));
        assertEquals(
                getResponse(TYPE_ROUTE+id, "PUT", "{").code(),
                HTTP_BAD_REQUEST
        );
    }

    @Test
    public void update() throws IOException {

        String id= idOf(client.post(TYPE_ROUTE,json()));
        assertEquals(
                getResponse(TYPE_ROUTE+id, "PUT", json()).code(),
                HTTP_OK
        );
    }

    @Test
    public void createWithPut() throws IOException {

        assertEquals(
            getResponse(TYPE_ROUTE+"1", "PUT", json()).code(),
            HTTP_CREATED
        );
    }
}

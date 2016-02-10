package org.dainst.chronontology.it;


import org.dainst.chronontology.TestConstants;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.dainst.chronontology.Constants.*;
import static org.dainst.chronontology.JsonTestUtils.*;
import static org.testng.Assert.assertEquals;

/**
 * @author Daniel M. de Oliveira
 */
public class StatusCodesIntegrationTest extends ResponseIntegrationTestBase {

    @Test
    public void putUnauthorized() throws IOException {
        assertEquals(
            rest(TYPE_ROUTE, "PUT", json(), TestConstants.USER_NAME,"wrong").code(),
            HTTP_UNAUTHORIZED
        );
    }

    @Test
    public void postUnauthorized() throws IOException {
        assertEquals(
            rest(TYPE_ROUTE, "POST", json(), TestConstants.USER_NAME,"wrong").code(),
            HTTP_UNAUTHORIZED
        );
    }

    @Test
    public void deleteUnauthorized() throws IOException {
        assertEquals(
            rest(TYPE_ROUTE, "DELETE", json(), TestConstants.USER_NAME,"wrong").code(),
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
        String id= idOf(client.post(TYPE_ROUTE,json()));
        assertEquals(
            rest(id, "GET", null).code(),
            HTTP_OK
        );
    }

    @Test
    public void oneTimePost() throws IOException {
        assertEquals(
            rest(TYPE_ROUTE, "POST", json()).code(),
            HTTP_CREATED
        );
    }

    @Test
    public void update() throws IOException {

        String id= idOf(client.post(TYPE_ROUTE,json()));
        assertEquals(
                rest(id, "PUT", json()).code(),
                HTTP_OK
        );
    }

    @Test
    public void createWithPut() throws IOException {

        assertEquals(
            rest(TYPE_ROUTE+"1", "PUT", json()).code(),
            HTTP_CREATED
        );
    }
}

package org.dainst.chronontology.it;


import com.squareup.okhttp.Response;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.dainst.chronontology.Constants.*;
import static org.dainst.chronontology.it.ResponseUtil.getResponse;
import static org.dainst.chronontology.util.JsonUtils.json;
import static org.testng.Assert.*;

/**
 * A test suite of tests covering jeremy's
 * response headers.
 *
 * @author Daniel de Oliveira
 */
public class HeaderIntegrationTest extends IntegrationTest {

    @Test
    public void putHeaders() throws IOException {
        Response res = getResponse(TYPE_ROUTE+"1", "PUT", json());
        assertEquals(
                res.header(HEADER_CT),
                HEADER_JSON
        );
        assertEquals(
                res.header(HEADER_LOC),
                TYPE_ROUTE+"1"
        );
    }

    @Test
    public void postHeaders() throws IOException {
        Response res = getResponse(TYPE_ROUTE, "POST", json());
        assertEquals(
                res.header(HEADER_CT),
                HEADER_JSON
        );
        assertNotNull(
                res.header(HEADER_LOC)
        );
    }

    @Test
    public void getHeaders() throws IOException {
        Response res = getResponse(TYPE_ROUTE+"1", "GET");
        assertEquals(
                res.header(HEADER_CT),
                HEADER_JSON
        );
        assertEquals(
                res.header(HEADER_LOC),
                TYPE_ROUTE+"1"
        );
    }

    @Test
    public void searchHeaders() throws IOException {
        Response res = getResponse(TYPE_ROUTE, "GET");
        assertEquals(
                res.header(HEADER_CT),
                HEADER_JSON
        );
        assertNull(
                res.header(HEADER_LOC)
        );
    }

    @Test
    public void serverStatusHeaders() throws IOException {
        Response res = getResponse("/", "GET");
        assertEquals(
                res.header(HEADER_CT),
                HEADER_JSON
        );
        assertNull(
                res.header(HEADER_LOC)
        );
    }
}

package org.dainst.chronontology.it;


import com.squareup.okhttp.Response;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.dainst.chronontology.Constants.*;
import static org.dainst.chronontology.util.JsonUtils.*;
import static org.testng.Assert.*;

/**
 * @author Daniel M. de Oliveira
 */
public class HeaderIntegrationTest extends ResponseIntegrationTestBase {

    @Test
    public void putHeaders() throws IOException {
        Response res = rest(TYPE_ROUTE+"1", "PUT", json());
        assertEquals(
                res.header(HEADER_CT),
                HEADER_JSON
        );
        assertEquals(
                res.header(HEADER_LOC),
                "1"
        );
    }

    @Test
    public void postHeaders() throws IOException {
        Response res = rest(TYPE_ROUTE, "POST", json());
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
        Response res = rest(TYPE_ROUTE+"1", "GET");
        assertEquals(
                res.header(HEADER_CT),
                HEADER_JSON
        );
        assertEquals(
                res.header(HEADER_LOC),
                "1"
        );
    }

    @Test
    public void searchHeaders() throws IOException {
        Response res = rest(TYPE_ROUTE, "GET");
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
        Response res = rest("/", "GET");
        assertEquals(
                res.header(HEADER_CT),
                HEADER_JSON
        );
        assertNull(
                res.header(HEADER_LOC)
        );
    }
}

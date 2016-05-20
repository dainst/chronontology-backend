package org.dainst.chronontology.store.rest;

import com.squareup.okhttp.*;
import org.dainst.chronontology.util.JsonUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * @author Daniel de Oliveira
 */
public class JsonRestClientTest {

    private OkHttpClient http= mock(OkHttpClient.class);

    private JsonRestClient jrc = new JsonRestClient("http://abc.de",http,true);

    /**
     * A httpCall created with this method can only get "consumed" once
     * due to the stream nature of the response body.
     * @param statusCode
     */
    private void mockHttpCall(Integer statusCode) {
        Request.Builder reqb= new Request.Builder();
        reqb.url("http://localhost"); reqb.put(RequestBody.create(MediaType.parse(""),"{}"));
        Request req=reqb.build();
        Response.Builder resb = new Response.Builder();
        resb.protocol(Protocol.HTTP_1_1);
        resb.code(statusCode);
        resb.request(req);
        resb.body(ResponseBody.create(MediaType.parse("application/json"),"{}"));
        Response res= resb.build();

        Call c= mock(Call.class);
        when(http.newCall(any())).thenReturn(c);
        try {
            when(c.execute()).thenReturn(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void responseStatusIndicatingSuccessfulOperation() throws IOException {

        mockHttpCall(200);
        assertNotNull(jrc.put("/", JsonUtils.json("{}")));
        mockHttpCall(201);
        assertNotNull(jrc.put("/", JsonUtils.json("{}")));
        mockHttpCall(220);
        assertNotNull(jrc.post("/", JsonUtils.json("{}")));
        mockHttpCall(270);
        assertNotNull(jrc.get("/"));
    }

    @Test
    public void responseStatusIndicatingUnsuccessfulOperation() throws IOException {

        mockHttpCall(400);
        assertNull(jrc.put("/", JsonUtils.json("{}")));
        mockHttpCall(300);
        assertNull(jrc.put("/", JsonUtils.json("{}")));
        mockHttpCall(320);
        assertNull(jrc.post("/", JsonUtils.json("{}")));
        mockHttpCall(3300);
        assertNull(jrc.get("/"));
    }
}
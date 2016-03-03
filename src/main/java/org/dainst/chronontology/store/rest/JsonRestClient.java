package org.dainst.chronontology.store.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.okhttp.*;
import org.apache.log4j.Logger;
import org.dainst.chronontology.util.JsonUtils;

import java.io.IOException;
import java.util.*;

/**
 * @author Daniel M. de Oliveira
 */
public class JsonRestClient {

    private final static Logger logger = Logger.getLogger(JsonRestClient.class);

    private final String url;
    private static final OkHttpClient client = new OkHttpClient();

    private String username= null;
    private String password= null;

    public JsonRestClient(
            final String url) {
        this.url = url;
    }

    public void authenticate(
            final String username,
            final String password) {
        this.username= username;
        this.password= password;
    }

    private void authorize(Request.Builder b) {

        if (password==null||username==null) return;

        b.addHeader("Authorization","Basic "+ new String(
                Base64.getEncoder().encode((username+":"+password).getBytes())));
    }

    /**
     *
     * @param path
     * @param method
     * @param json
     * @return null if a JsonNode could not get generated properly
     *   from the response body.
     */
    private JsonNode restApi(String path, String method, JsonNode json) {

        String s=  (json!=null) ? json.toString() : null  ;
        Request.Builder b = RestUtils.getRequestBuilder(method, s).url(url + path);

        authorize(b);

        Response response = null;
        try {
            response = client.newCall(b.build()).execute();
            String body= response.body().string();
            if (body.isEmpty()) return null;
            return JsonUtils.json(body);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    public JsonNode post(String path, JsonNode json) {
        return restApi(path,"POST",json);
    }

    public JsonNode put(String path, JsonNode json) {
        return restApi(path,"PUT",json);
    }

    /**
     *
     * @param path
     * @return null if there is no response or the response is no proper json.
     */
    public JsonNode get(String path) {
        return restApi(path,"GET",null);
    }

    /**
     * Performs a delete request on url+path
     * @param path must begin with a forward slash.
     * @return the response, if any. Null otherwise
     */
    public JsonNode delete(String path) {
        return restApi(path,"DELETE",null);
    }
}

package org.dainst.chronontology.store.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.okhttp.*;
import org.apache.log4j.Logger;
import org.dainst.chronontology.util.JsonUtils;

import java.io.IOException;
import java.util.*;

/**
 * @author Daniel de Oliveira
 */
public class JsonRestClient {

    private final static Logger logger = Logger.getLogger(JsonRestClient.class);

    private final String url;
    private final OkHttpClient client;

    private String username= null;
    private String password= null;

    private boolean returnNullOnError;

    /**
     * @param url
     * @param client
     * @param returnNullOnError influences how {@link #put(String, JsonNode)},{@link #post(String, JsonNode)} and {@link #get(String)}.
     *   react in case of error return codes.
     */
    public JsonRestClient(
            final String url,
            final OkHttpClient client,
            final boolean returnNullOnError) {
        this.url = url;
        this.client = client;
        this.returnNullOnError = returnNullOnError;
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

    private JsonNode performHttpCall(String path, String method, JsonNode json) {

        String s=  (json!=null) ? json.toString() : null  ;
        Request.Builder b = RestUtils.getRequestBuilder(method, s).url(url + path);

        authorize(b);

        Response response;
        ResponseBody body = null;
        try {
            response = client.newCall(b.build()).execute();
            body = response.body();

            String content= body.string();
            if (content.isEmpty()) return null;

            if (shouldReturnNull(response.code())) {
                logger.error("Got "+response.code()+" with "+ method +" on "+url+path+". Response body was: "+content);
                return null;
            }
            return JsonUtils.json(content);

        } catch (IOException e) {
            logger.error("Got an error with "+method+" on " +url+path+". Error message was: "+e.getMessage());
            return null;

        } finally {
            if (body!=null) try {
                body.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    private boolean shouldReturnNull(final int code) {
        return (returnNullOnError&&!(code>=200 && code<300));
    }

    /**
     * @param path
     * @param json
     * @return <code>null</code> if at least one of the following conditions is not met
     *   <ol>
     *     <li>there is no response
     *     <li>a JsonNode could not get generated properly from the response body
     *     <li><code>returnNullOnError</code> is <code>true</code> and the response code was not OK(200-299).
     */
    public JsonNode post(String path, JsonNode json) {
        return performHttpCall(path,"POST",json);
    }

    /**
     * @param path
     * @param json
     * @return <code>null</code> if at least one of the following conditions is not met
     *   <ol>
     *     <li>there is no response
     *     <li>a JsonNode could not get generated properly from the response body
     *     <li><code>returnNullOnError</code> is <code>true</code> and the response code was not OK(200-299).
     */
    public JsonNode put(String path, JsonNode json) {
        return performHttpCall(path,"PUT",json);
    }

    /**
     *
     * @param path
     * @return <code>null</code> if at least one of the following conditions is not met
     *   <ol>
     *     <li>there is no response
     *     <li>a JsonNode could not get generated properly from the response body
     *     <li><code>returnNullOnError</code> is <code>true</code> and the response code was not OK(200-299).
     */
    public JsonNode get(String path) {
        return performHttpCall(path,"GET",null);
    }

    /**
     * Performs a delete request on url+path
     * @param path must begin with a forward slash.
     * @return the response, if any. Null otherwise
     */
    public JsonNode delete(String path) {
        return performHttpCall(path,"DELETE",null);
    }
}

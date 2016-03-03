package org.dainst.chronontology.it;

import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.dainst.chronontology.TestConstants;
import org.dainst.chronontology.util.RestUtils;

import java.io.IOException;
import java.util.Base64;

/**
 * @author Daniel M. de Oliveira
 */
public class ResponseUtil {

    private static void authorize(String username,String password,Request.Builder b) {
        b.addHeader("Authorization","Basic "+ new String(
                Base64.getEncoder().encode((username+":"+password).getBytes())));
    }

    /**
     * Performs a restful operation against the system under test.
     *
     * @param path
     * @param method
     * @param json
     * @param username
     * @param password
     * @return the response object.
     */
    protected static Response getResponse(String path, String method, JsonNode json, String username, String password) {
        return getResponse(path,method,json.toString(),username,password);
    }

    /**
     * Performs a restful operation against the system under test.
     *
     * @param path
     * @param method
     * @param json
     * @param username if null, the request will be done without authentication.
     * @param password
     * @return the response object.
     */
    private static Response getResponse(String path, String method, String json, String username, String password) {

        Request.Builder b= RestUtils.getRequestBuilder(method,json).url(TestConstants.SERVER_URL + path);
        if (username!=null) authorize(username,password,b);

        Response response= null;
        try {
            response = new OkHttpClient().newCall(b.build()).execute();
            response.body().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Performs a restful operation against the system under test.
     *
     * @param path
     * @param method
     * @return the response object.
     */
    protected static Response getResponse(String path, String method) {
        return getResponse(path,method,(String) null, TestConstants.USER_NAME_ADMIN, TestConstants.PASS_WORD);
    }

    /**
     * Performs a restful operation against the system under test.
     *
     * @param path
     * @param method
     * @param json
     * @return the response object.
     */
    protected static Response getResponse(String path, String method, JsonNode json) {
        return getResponse(path,method,json, TestConstants.USER_NAME_ADMIN, TestConstants.PASS_WORD);
    }

    /**
     * Performs a restful operation against the system under test.
     *
     * @param path
     * @param method
     * @param json
     * @return the response object.
     */
    protected static Response getResponse(String path, String method, String json) {
        return getResponse(path,method,json, TestConstants.USER_NAME_ADMIN, TestConstants.PASS_WORD);
    }
}

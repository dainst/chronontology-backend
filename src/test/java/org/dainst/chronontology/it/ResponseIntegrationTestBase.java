package org.dainst.chronontology.it;

import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.dainst.chronontology.it.IntegrationTestBase;

import java.io.IOException;
import java.util.Base64;

/**
 * @author Daniel M. de Oliveira
 */
public class ResponseIntegrationTestBase extends IntegrationTestBase {

    private void authorize(String username,String password,Request.Builder b) {
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
    protected Response rest(String path, String method, JsonNode json, String username, String password) {

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

    /**
     * Performs a restful operation against the system under test.
     *
     * @param path
     * @param method
     * @param json
     * @return the response object.
     */
    protected Response rest(String path, String method, JsonNode json) {
        return rest(path,method,json,USER_NAME,PASS_WORD);
    }
}

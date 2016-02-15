package org.dainst.chronontology.util;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

/**
 * @author Daniel M. de Oliveira
 */
public class RestUtils {

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public static Request.Builder getRequestBuilder(String method, String json) {
        Request.Builder b = new Request.Builder();

        if (method.equals("GET")) {
            b.get();
        } else if
                (method.equals("DELETE")) {
            b.delete();
        }
        else {
            RequestBody body = RequestBody.create(JSON, json);
            if (method.equals("POST")) {
                b.post(body);
            }
            if (method.equals("PUT")) {
                b.put(body);
            }
        }
        return b;
    }
}

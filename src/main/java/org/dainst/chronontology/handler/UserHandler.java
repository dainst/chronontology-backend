package org.dainst.chronontology.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.dainst.chronontology.handler.dispatch.Dispatcher;
import org.dainst.chronontology.util.JsonUtils;
import spark.Request;
import spark.Response;
import java.io.IOException;
import static org.dainst.chronontology.Constants.*;

/**
 * Created by Simon Hohl on 26.04.17.
 */

public class UserHandler implements Handler {
    protected final Dispatcher dispatcher;

    public UserHandler(Dispatcher dispatcher) {
        this.dispatcher= dispatcher;
    }

    /**
     * Renders information about the internal server state.
     *
     * @param res
     * @return json object with server state details
     * @throws IOException
     */
    @Override
    public Object handle(Request req, Response res) throws IOException {

        res.status(HTTP_OK);
        JsonNode n= JsonUtils.json();
        ((ObjectNode)n).put("status","success");
        return n;
    }

}

package org.dainst.chronontology.handler;

import org.dainst.chronontology.handler.dispatch.Dispatcher;
import spark.Request;
import spark.Response;
import java.io.IOException;
import static org.dainst.chronontology.Constants.*;
import static org.dainst.chronontology.util.JsonUtils.json;

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
        return json();
    }

}

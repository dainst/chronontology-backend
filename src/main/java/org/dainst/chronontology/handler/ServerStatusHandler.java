package org.dainst.chronontology.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.dainst.chronontology.controller.Dispatcher;
import org.dainst.chronontology.store.Connector;
import org.dainst.chronontology.util.JsonUtils;
import org.dainst.chronontology.util.Results;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static org.dainst.chronontology.Constants.*;

/**
 * @author Daniel M. de Oliveira
 */
public class ServerStatusHandler implements Handler {

    protected final Dispatcher dispatcher;

    public ServerStatusHandler(Dispatcher dispatcher) {
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

        JsonNode serverStatus= makeServerStatusJson();
        res.status(HTTP_OK);
        if (serverStatus.toString().contains(DATASTORE_STATUS_DOWN))
            res.status(HTTP_NOT_FOUND);
        return serverStatus;
    }

    private JsonNode makeServerStatusJson() throws IOException {
        Results datastores= new Results("datastores");
        dispatcher.addDatatoreStatus(this,datastores);
        return datastores.j();
    }


    public JsonNode makeDataStoreStatus(String type, Connector store) throws IOException {
        String status = DATASTORE_STATUS_DOWN;
        if (store.isConnected()) status = DATASTORE_STATUS_OK;
        return JsonUtils.json("{ \"type\" : \""+type+"\", \"status\" : \""+status+"\" }");
    }
}

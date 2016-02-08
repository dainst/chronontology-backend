package org.dainst.chronontology.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.dainst.chronontology.CredentialsDecoder;
import org.dainst.chronontology.util.Results;
import org.dainst.chronontology.model.DocumentModel;
import org.dainst.chronontology.model.DocumentModelFactory;
import org.dainst.chronontology.store.Connector;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Random;

import static org.dainst.chronontology.Constants.*;

/**
 * Template methods are used so subclasses can implement
 * concrete behaviour for datastore handling.
 *
 * @author Daniel M. de Oliveira
 */
public abstract class Controller {

    final static Logger logger = Logger.getLogger(Controller.class);

    public static final String ID = ":id";


    // Template methods
    abstract protected JsonNode _get(String bucket, String key);
    abstract protected void _handlePost(final String bucket,final String key,final JsonNode value);
    abstract protected void _handlePut(final String bucket,final String key,final JsonNode value);
    abstract protected JsonNode _handleGet(final String bucket,final String key,Request req);
    abstract protected JsonNode _handleSearch(String bucket,String query);
    abstract protected void _addDatatoreStatus(Results r) throws IOException;
    // Template methods

    private String generateId() {
        byte[] r = new byte[9];
        new Random().nextBytes(r);
        String s = Base64.encodeBase64String(r);
        return s.replaceAll("/", "_");
    }

    private String determineFreeId(String typeName) {
        String id;
        JsonNode existingDoc= null;
        do {
            id= generateId();
            existingDoc = _get(typeName,id);
        } while (existingDoc!=null);
        return id;
    }


    /**
     * Renders information about the internal server state.
     *
     * @param res
     * @return json object with server state details
     * @throws IOException
     */
    public Object handleServerStatus(
            final Response res) throws IOException {

        JsonNode serverStatus= makeServerStatusJson();
        res.status(HTTP_OK);
        if (serverStatus.toString().contains(DATASTORE_STATUS_DOWN))
            res.status(HTTP_NOT_FOUND);
        return serverStatus;
    }

    private JsonNode makeServerStatusJson() throws IOException {
        Results datastores= new Results("datastores");
        _addDatatoreStatus(datastores);
        return datastores.j();
    }


    protected JsonNode makeDataStoreStatus(String type, Connector store) throws IOException {
        String status = DATASTORE_STATUS_DOWN;
        if (store.isConnected()) status = DATASTORE_STATUS_OK;
        return json("{ \"type\" : \""+type+"\", \"status\" : \""+status+"\" }");
    }

    private String user(Request req) {
        return CredentialsDecoder.decode(req.headers(HEADER_AUTH)).split(":")[0];
    }

    public Object handlePost(
            final String typeName,
            final Request req,
            final Response res) throws IOException {

        String id= determineFreeId(typeName);

        JsonNode doc =
                DocumentModelFactory.create(
                        typeName,id,json(req.body()), user(req)).j();

        _handlePost(typeName,id,doc);

        res.header("location", id);
        res.status(HTTP_CREATED);
        return doc;
    }


    public Object handlePut(
            final String typeName,
            final Request req,
            final Response res) throws IOException {

        String id = req.params(ID);
        JsonNode oldDoc = _get(typeName,id);

        DocumentModel dm = DocumentModelFactory.create(
                typeName,id,json(req.body()), user(req));
        JsonNode doc = null;
        if (oldDoc!=null) {
            doc= dm.merge(oldDoc).j();
            res.status(HTTP_OK);
        } else {
            doc= dm.j();
            res.status(HTTP_CREATED);
        }

        _handlePut(typeName,id,doc);

        return doc;
    }


    public Object handleGet(
            final String typeName,
            final Request req,
            final Response res) throws IOException {

        String id = req.params(ID);

        JsonNode result= _handleGet(typeName,id,req);

        if (result==null){
            res.status(HTTP_NOT_FOUND);
            return "";
        }
        return result;
    }


    public Object handleSearch(
            final String typeName,
            final Request req,
            final Response res) throws IOException {

        return _handleSearch(typeName,req.queryString());
    }


    private static JsonNode json(String s) throws IOException {
        return new ObjectMapper().readTree(s);
    }


}

package org.dainst.chronontology.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.dainst.chronontology.store.Connector;
import org.dainst.chronontology.util.JsonUtils;
import org.dainst.chronontology.util.Results;
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

    private RightsValidator rightsValidator= new RightsValidator();


    // Template methods
    abstract protected JsonNode _get(String bucket, String key);
    abstract protected boolean _handlePost(final String bucket,final String key,final JsonNode value);
    abstract protected boolean _handlePut(final String bucket,final String key,final JsonNode value);
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
        return JsonUtils.json("{ \"type\" : \""+type+"\", \"status\" : \""+status+"\" }");
    }

    public Object handlePost(
            final String typeName,
            final Request req,
            final Response res) throws IOException {

        JsonNode n= validateIncomingJson(req,res);
        if (n==null) return JsonUtils.json();

        String id= determineFreeId(typeName);

        JsonNode doc =
                new DocumentModel(
                        typeName,id,n, req.attribute("user")).j();

        if (!_handlePost(typeName,id,doc))
            res.status(HTTP_INTERNAL_SERVER_ERROR);
        else
            res.status(HTTP_CREATED);

        res.header("location", id);
        return doc;
    }


    private boolean userPermittedToModifyDataset(Request req, JsonNode n) {
        if (n.get("dataset")!=null &&
                !rightsValidator.hasEditorPermission(req.attribute("user"),
                        n.get("dataset").toString().replaceAll("\"",""))) {
            return false;
        }
        return true;
    }

    private JsonNode validateIncomingJson(Request req, Response res) {
        JsonNode n= JsonUtils.json(req.body());
        if (n==null) {
            res.status(HTTP_BAD_REQUEST);
            return null;
        }
        if (!userPermittedToModifyDataset(req,n)) {
            res.status(HTTP_FORBIDDEN);
            return null;
        }
        return n;
    }


    public Object handlePut(
            final String typeName,
            final Request req,
            final Response res) throws IOException {


        JsonNode n= validateIncomingJson(req,res);
        if (n==null) return JsonUtils.json();


        DocumentModel dm = new DocumentModel(
                typeName,req.params(ID),JsonUtils.json(req.body()), req.attribute("user"));

        JsonNode doc = null;
        int status;
        JsonNode oldDoc = _get(typeName,req.params(ID));
        if (oldDoc!=null) {

            if (!userPermittedToModifyDataset(req,oldDoc)) {
                res.status(HTTP_FORBIDDEN);
                return JsonUtils.json();
            } else {
                doc= dm.merge(oldDoc).j();
                status= HTTP_OK;
            }

        } else {
            doc= dm.j();
            status= HTTP_CREATED;
        }

        if (!_handlePut(typeName,req.params(ID),doc))
            res.status(HTTP_INTERNAL_SERVER_ERROR);
        else
            res.status(status);

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



}

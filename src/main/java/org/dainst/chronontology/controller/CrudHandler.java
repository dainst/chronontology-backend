package org.dainst.chronontology.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.codec.binary.Base64;
import org.dainst.chronontology.util.JsonUtils;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Random;

import static org.dainst.chronontology.Constants.*;

/**
 * @author Daniel M. de Oliveira
 */
public class CrudHandler extends Handler {

    public static final String ID = ":id";

    public CrudHandler(Dispatcher dispatcher, RightsValidator rightsValidator) {
        super(dispatcher,rightsValidator);
    }


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
            existingDoc = dispatcher.get(typeName,id);
        } while (existingDoc!=null);
        return id;
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

        if (!dispatcher.handlePost(typeName,id,doc))
            res.status(HTTP_INTERNAL_SERVER_ERROR);
        else
            res.status(HTTP_CREATED);

        res.header("location", id);
        return doc;
    }



    private JsonNode validateIncomingJson(Request req, Response res) {
        JsonNode n= JsonUtils.json(req.body());
        if (n==null) {
            res.status(HTTP_BAD_REQUEST);
            return null;
        }
        if (!super.userAccessLevelSufficient(req,n,RightsValidator.Rights.EDITOR)) {
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
        JsonNode oldDoc = dispatcher.get(typeName,req.params(ID));
        if (oldDoc!=null) {

            if (!super.userAccessLevelSufficient(req,oldDoc,RightsValidator.Rights.EDITOR)) {
                res.status(HTTP_FORBIDDEN);
                return JsonUtils.json();
            } else {
                doc= dm.merge(oldDoc).j(); // TODO Review neccessary to clarify what
                // happens if an enriched version gets fetched in connect mode and got merged with the incoming
                // json. Does the enriched version gets send to the main datastore then?
                status= HTTP_OK;
            }

        } else {
            doc= dm.j();
            status= HTTP_CREATED;
        }

        if (!dispatcher.handlePut(typeName,req.params(ID),doc))
            res.status(HTTP_INTERNAL_SERVER_ERROR);
        else
            res.status(status);

        return doc;
    }


    public Object handleGet(
            final String typeName,
            final Request req,
            final Response res) throws IOException {

        JsonNode result= dispatcher.handleGet(typeName,req.params(ID),req);
        if (result==null){
            res.status(HTTP_NOT_FOUND);
            return "";
        }
        if (!super.userAccessLevelSufficient(req,result,RightsValidator.Rights.READER)) {
            res.status(HTTP_FORBIDDEN);
            return JsonUtils.json();
        }
        return result;
    }
}

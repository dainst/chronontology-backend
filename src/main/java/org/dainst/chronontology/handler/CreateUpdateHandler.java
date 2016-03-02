package org.dainst.chronontology.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.codec.binary.Base64;
import org.dainst.chronontology.controller.Dispatcher;
import org.dainst.chronontology.util.JsonUtils;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Random;

import static org.dainst.chronontology.Constants.*;
import static org.dainst.chronontology.util.JsonUtils.*;

/**
 * @author Daniel M. de Oliveira
 */
public class CreateUpdateHandler extends Handler {

    public CreateUpdateHandler(Dispatcher dispatcher, RightsValidator rightsValidator) {
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
            existingDoc = dispatcher.dispatchGet(typeName,id);
        } while (existingDoc!=null);
        return id;
    }




    public Object handlePost(
            final String typeName,
            final Request req,
            final Response res) throws IOException {

        DocumentModel dm= makeDocumentModel(typeName,req,res);
        if (dm==null) return JsonUtils.json();

        if (!dispatcher.dispatchPost(typeName,dm.getId().replace("/"+typeName+"/",""),dm.j()))
            res.status(HTTP_INTERNAL_SERVER_ERROR);
        else {
            res.status(HTTP_CREATED);
        }

        res.header("location", dm.getId());
        return dm;
    }



    private DocumentModel makeDocumentModel(String typeName, Request req, Response res) {

        JsonNode n= json(req.body());
        if (n==null) {
            res.status(HTTP_BAD_REQUEST);
            return null;
        }

        String id= (req.params(ID)!=null) ? req.params(ID) : determineFreeId(typeName);
        DocumentModel dm = new DocumentModel(
                "/"+typeName+"/"+id,json(req.body()), req.attribute("user"));

        if (!userAccessLevelSufficient(req,dm, RightsValidator.Operation.EDIT)) {
            res.status(HTTP_FORBIDDEN);
            return null;
        }
        return dm;
    }


    public Object handlePut(
            final String typeName,
            final Request req,
            final Response res) throws IOException {


        DocumentModel dm= makeDocumentModel(typeName,req,res);
        if (dm==null) return json();


        int status;
        DocumentModel oldDm = DocumentModel.from(dispatcher.dispatchGet(typeName,req.params(ID)));

        if (oldDm!=null) {

            if (!userAccessLevelSufficient(req,oldDm, RightsValidator.Operation.EDIT)) {
                res.status(HTTP_FORBIDDEN);
                return json();
            } else {
                dm.merge(oldDm); // TODO Review neccessary to clarify what
                // happens if an enriched version gets fetched in connect mode and got merged with the incoming
                // json. Does the enriched version gets send to the main datastore then?
                status= HTTP_OK;
            }

        } else {
            status= HTTP_CREATED;
        }

        if (!dispatcher.dispatchPut(typeName,req.params(ID),dm.j()))
            res.status(HTTP_INTERNAL_SERVER_ERROR);
        else
            res.status(status);

        return dm;
    }
}

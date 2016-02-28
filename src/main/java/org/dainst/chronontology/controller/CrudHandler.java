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
public class CrudHandler {

    public static final String ID = ":id";

    public CrudHandler(Controller controller) {
        this.controller = controller;
    }

    private final Controller controller;


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
            existingDoc = controller.get(typeName,id);
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

        if (!controller.handlePost(typeName,id,doc))
            res.status(HTTP_INTERNAL_SERVER_ERROR);
        else
            res.status(HTTP_CREATED);

        res.header("location", id);
        return doc;
    }


    private boolean userPermittedToModifyDataset(Request req, JsonNode n) {
        if (n.get("dataset")!=null &&
                !controller.getRightsValidator().hasEditorPermission(req.attribute("user"),
                        n.get("dataset").toString().replaceAll("\"",""))) {
            return false;
        }
        return true;
    }

    private boolean userPermittedToReadDataset(Request req, JsonNode n) {
        if (n.get("dataset")!=null &&
                !controller.getRightsValidator().hasReaderPermission(req.attribute("user"),
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
        JsonNode oldDoc = controller.get(typeName,req.params(ID));
        if (oldDoc!=null) {

            if (!userPermittedToModifyDataset(req,oldDoc)) {
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

        if (!controller.handlePut(typeName,req.params(ID),doc))
            res.status(HTTP_INTERNAL_SERVER_ERROR);
        else
            res.status(status);

        return doc;
    }


    public Object handleGet(
            final String typeName,
            final Request req,
            final Response res) throws IOException {

        JsonNode result= controller.handleGet(typeName,req.params(ID),req);
        if (result==null){
            res.status(HTTP_NOT_FOUND);
            return "";
        }
        if (!userPermittedToReadDataset(req,result)) {
            res.status(HTTP_FORBIDDEN);
            return JsonUtils.json();
        }
        return result;
    }
}

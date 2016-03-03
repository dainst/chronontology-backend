package org.dainst.chronontology.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.codec.binary.Base64;
import org.dainst.chronontology.controller.Dispatcher;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Random;

import static org.dainst.chronontology.Constants.HTTP_BAD_REQUEST;
import static org.dainst.chronontology.Constants.HTTP_FORBIDDEN;
import static org.dainst.chronontology.util.JsonUtils.json;

/**
 * @author Daniel M. de Oliveira
 */
public abstract class DocumentHandler implements Handler {

    private static final String ID = ":id";

    public DocumentHandler(Dispatcher dispatcher, RightsValidator rightsValidator) {
        this.dispatcher = dispatcher;
        this.rightsValidator= rightsValidator;
    }

    protected final Dispatcher dispatcher;
    protected final RightsValidator rightsValidator;


    protected final Document makeDocumentModel(
            Request req,
            Response res,
            boolean createId) {

        JsonNode n= json(req.body());
        if (n==null) {
            res.status(HTTP_BAD_REQUEST);
            return null;
        }

        String resourceId= (createId) ? req.pathInfo()+determineFreeId(req) : req.pathInfo();

        Document dm = new Document(
                resourceId, json(req.body()), req.attribute("user"));

        if (!userAccessLevelSufficient(req,dm, RightsValidator.Operation.EDIT)) {
            res.status(HTTP_FORBIDDEN);
            return null;
        }
        return dm;
    }

    protected final boolean userAccessLevelSufficient(Request req, Document dm, RightsValidator.Operation operation) {
        if (dm.getDataset()!=null &&
                !rightsValidator.hasPermission(req.attribute("user"),
                        dm.getDataset(), operation)) {
            return false;
        }
        return true;
    }

    protected String type(Request req) {
        return req.pathInfo().replace(req.params(ID),"");
    }

    protected String simpleId(Request req) {
        return req.params(ID);
    }

    private static String generateId() {
        byte[] r = new byte[9];
        new Random().nextBytes(r);
        String s = Base64.encodeBase64String(r);
        return s.replaceAll("/", "_");
    }

    private String determineFreeId(Request req) {
        String id;
        JsonNode existingDoc= null;
        do {
            id= generateId();
            existingDoc = dispatcher.dispatchGet(req.pathInfo(),id);
        } while (existingDoc!=null);
        return id;
    }

    @Override
    public abstract Object handle(Request req, Response res) throws IOException;
}

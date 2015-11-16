package org.dainst.chronontology;

import static org.dainst.chronontology.Constants.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.dainst.chronontology.model.DocumentModel;
import org.dainst.chronontology.model.DocumentModelFactory;
import org.dainst.chronontology.store.JsonBucketKeyValueStore;
import org.dainst.chronontology.store.JsonSearchableBucketKeyValueStore;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Random;

/**
 * @author Daniel M. de Oliveira
 */
public class Controller {

    final static Logger logger = Logger.getLogger(Controller.class);

    public static final String ID = ":id";

    private final JsonBucketKeyValueStore mainDatastore;
    private final JsonSearchableBucketKeyValueStore connectDatastore;

    public Controller(
            final JsonBucketKeyValueStore mainDatastore,
            final JsonSearchableBucketKeyValueStore connectDatastore
    ) {
        this.mainDatastore= mainDatastore;
        this.connectDatastore= connectDatastore;
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
            existingDoc = mainDatastore.get(typeName,id);
        } while (existingDoc!=null);
        return id;
    }

    Object handlePost(
            final String typeName,
            final Request req,
            final Response res) throws IOException {

        String id= determineFreeId(typeName);

        JsonNode doc =
                DocumentModelFactory.create(typeName,id,json(req.body())).j();

        mainDatastore.put(typeName,id, doc);
        connectDatastore.put(typeName,id, doc);

        res.header("location", id);
        res.status(HTTP_CREATED);
        return doc;
    }

    Object handlePut(
            final String typeName,
            final Request req,
            final Response res) throws IOException {

        String id = req.params(ID);
        JsonNode oldDoc = mainDatastore.get(typeName,id);

        DocumentModel dm = DocumentModelFactory.create(typeName,id,json(req.body()));
        JsonNode doc = null;
        if (oldDoc!=null) {
            doc= dm.merge(oldDoc).j();
            res.status(HTTP_OK);
        } else {
            doc= dm.j();
            res.status(HTTP_CREATED);
        }

        mainDatastore.put(typeName,id, doc);
        connectDatastore.put(typeName,id, doc);

        return doc;
    }

    Object handleGet(
            final String typeName,
            final Request req,
            final Response res) throws IOException {

        String id = req.params(ID);

        JsonNode result= shouldBeDirect(req.queryParams("direct"))
                ? mainDatastore.get(typeName,id)
                : connectDatastore.get(typeName,id);

        if (result==null){
            res.status(HTTP_NOT_FOUND);
            return "";
        }
        return result;
    }

    Object handleSearch(
            final String typeName,
            final Request req,
            final Response res) throws IOException {

        return connectDatastore.search( typeName,
                req.queryString()
        );
    }

    private static JsonNode json(String s) throws IOException {
        return new ObjectMapper().readTree(s);
    }

    private boolean shouldBeDirect(final String directParam) {
        return (directParam!=null&&
                directParam.equals("true"));
    }
}

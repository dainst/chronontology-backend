package org.dainst.chron;

import static org.dainst.chron.Constants.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.dainst.chron.store.JsonBucketKeyValueStore;
import org.dainst.chron.store.JsonSearchableBucketKeyValueStore;
import spark.Request;
import spark.Response;

import java.io.IOException;

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

    Object handlePost(
            final String typeName,
            final Request req,
            final Response res) throws IOException {

        String id = req.params(ID);
        JsonNode oldDoc = mainDatastore.get(typeName,id);

        if (oldDoc!=null) {
            res.status(HTTP_FORBIDDEN);
            return "";
        }

        JsonNode doc = new DocumentModel(typeName,json(req.body()))
                .addStorageInfo(id);

        mainDatastore.put(typeName,id, doc);
        connectDatastore.put(typeName,id, doc);

        res.status(HTTP_CREATED);

        return doc;
    }

    Object handlePut(
            final String typeName,
            final Request req,
            final Response res) throws IOException {

        String id = req.params(ID);
        JsonNode oldDoc = mainDatastore.get(typeName,id);

        DocumentModel dm = new DocumentModel(typeName,json(req.body()));
        JsonNode doc = null;
        if (oldDoc!=null) {
            doc= dm.addStorageInfo(oldDoc, id);
            res.status(HTTP_OK);
        } else {
            doc= dm.addStorageInfo(id);
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

        JsonNode result= null;
        if (shouldBeDirect(req.queryParams("direct")))
            result= mainDatastore.get(typeName,id);
        else {
            result= connectDatastore.get(typeName,id);

        }
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

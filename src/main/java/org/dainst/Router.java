package org.dainst;

import static org.dainst.C.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

/**
 * @author Daniel M. de Oliveira
 */
public class Router {

    final static Logger logger = Logger.getLogger(Router.class);
    public static final String ID = ":id";

    final JsonBucketKeyValueStore mainDatastore;
    final JsonSearchableBucketKeyValueStore connectDatastore;

    private static JsonNode json(String s) throws IOException {
        return new ObjectMapper().readTree(s);
    }

    private boolean shouldBeDirect(final String directParam) {
        return (directParam!=null&&
                directParam.equals("true"));
    }

    private Object handleGet(
            final String typeName,
            final Request req,
            final Response res) throws IOException {

        String id = req.params(ID);

        if (shouldBeDirect(req.queryParams("direct")))
            return mainDatastore.get(typeName,id);
        else
            return connectDatastore.get(typeName,id);
    }

    private Object handleSearch(
            final String typeName,
            final Request req,
            final Response res) throws IOException {

        return connectDatastore.search( typeName,
                req.queryString()
                );
    }

    private Object handlePut(
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

    private Object handlePost(
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

    private void setUpTypeRoutes(
            final String typeName
    ) {
        get( "/" + typeName + "/", (req,res) -> {
            res.header("location", req.params(ID));
            return handleSearch(typeName,req,res);
        });
        get( "/" + typeName + "/" + ID, (req,res) -> {
            res.header("location", req.params(ID));
            return handleGet(typeName,req,res);
        });
        post("/" + typeName + "/" + ID, (req, res) ->  {
            res.header("location", req.params(ID));
            return handlePost(typeName,req,res);
        });
        put( "/" + typeName + "/" + ID, (req, res) -> {
            res.header("location", req.params(ID));
            return handlePut(typeName,req,res);
        });
    }

    public Router(
        final JsonBucketKeyValueStore mainDatastore,
        final JsonSearchableBucketKeyValueStore connectDatastore,
        final String[] typeNames
    ){

        this.mainDatastore=mainDatastore;
        this.connectDatastore=connectDatastore;

        for (String typeName:typeNames)
            setUpTypeRoutes(typeName);
    }
}

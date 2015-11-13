package org.dainst;

import static org.dainst.C.*;
import static spark.Spark.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Base64;

/**
 * @author Daniel M. de Oliveira
 */
public class Router {

    final static Logger logger = Logger.getLogger(Router.class);
    public static final String ID = ":id";

    private final JsonBucketKeyValueStore mainDatastore;
    private final JsonSearchableBucketKeyValueStore connectDatastore;

    private final Controller controller;

    private void setUpTypeRoutes(
            final String typeName
    ) {
        get( "/" + typeName + "/", (req,res) -> {
            res.header("location", req.params(ID));
            return controller.handleSearch(typeName,req,res);
        });
        get( "/" + typeName + "/" + ID, (req,res) -> {
            res.header("location", req.params(ID));
            return controller.handleGet(typeName,req,res);
        });
        post("/" + typeName + "/" + ID, (req, res) ->  {
            res.header("location", req.params(ID));
            return controller.handlePost(typeName,req,res);
        });
        put( "/" + typeName + "/" + ID, (req, res) -> {
            res.header("location", req.params(ID));
            return controller.handlePut(typeName,req,res);
        });
    }

    private void setUpAuthorization(String[] credentials) {

        before("/*", (request, response) -> {

            if (request.requestMethod().equals("GET"))
                return;

            boolean authenticated = false;
            if(request.headers("Authorization") != null
                    && request.headers("Authorization").startsWith("Basic"))
            {
                String decodedCredentials = new String(
                        Base64.getDecoder().decode(
                                request.headers("Authorization").substring("Basic".length()).trim()));

                for (String cred:credentials)
                    if(decodedCredentials.equals(cred)) authenticated = true;
            }
            if(!authenticated) {
                response.header("WWW-Authenticate", "Basic realm=\"Restricted\"");
                response.status(HTTP_UNAUTHORIZED);
                halt(HTTP_UNAUTHORIZED);
            }
        });
    }

    public Router(
        final JsonBucketKeyValueStore mainDatastore,
        final JsonSearchableBucketKeyValueStore connectDatastore,
        final String[] typeNames,
        final String[] credentials
    ){
        controller= new Controller(mainDatastore,connectDatastore);

        this.mainDatastore=mainDatastore;
        this.connectDatastore=connectDatastore;

        for (String typeName:typeNames)
            setUpTypeRoutes(typeName);

        setUpAuthorization(credentials);
    }
}

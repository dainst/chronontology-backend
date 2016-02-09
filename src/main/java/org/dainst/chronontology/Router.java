package org.dainst.chronontology;

import static spark.Spark.*;
import static org.dainst.chronontology.Constants.*;

import org.apache.log4j.Logger;
import org.dainst.chronontology.controller.Controller;
import spark.Request;
import spark.Response;

import java.util.Base64;



/**
 * @author Daniel M. de Oliveira
 */
public class Router {

    private final static Logger logger = Logger.getLogger(Router.class);
    public static final String ID = ":id";

    private final Controller controller;

    private void setUpTypeRoutes(
            final String typeName
    ) {
        get( "/", (req,res) -> {
            setHeader(res);
            return controller.handleServerStatus(res);
        });
        get( "/" + typeName + "/", (req,res) -> {
            setHeader(res);
            return controller.handleSearch(typeName,req,res);
        });
        get( "/" + typeName + "/" + ID, (req,res) -> {
            setHeader(req,res);
            return controller.handleGet(typeName,req,res);
        });
        post("/" + typeName + "/", (req, res) ->  {
            setHeader(res);
            return controller.handlePost(typeName,req,res);
        });
        put( "/" + typeName + "/" + ID, (req, res) -> {
            setHeader(req,res);
            return controller.handlePut(typeName,req,res);
        });
    }

    private void setHeader(Response res) {
        res.header(HEADER_CT, HEADER_JSON);
    }

    private void setHeader(Request req, Response res) {
        res.header(HEADER_CT, HEADER_JSON);
        res.header(HEADER_LOC, req.params(ID));
    }

    private void setUpAuthorization(String[] credentials) {

        before("/*", (req, res) -> {

            if (req.requestMethod().equals("GET"))
                return;

            boolean authenticated = false;
            if(req.headers(HEADER_AUTH) != null
                    && req.headers(HEADER_AUTH).startsWith("Basic"))
            {
                for (String cred:credentials)
                    if(decode(req.headers(HEADER_AUTH)).equals(cred)) {
                        req.attribute("user",cred.split(":")[0]);
                        authenticated = true;
                    }
            }

            if(!authenticated) {
                res.header("WWW-Authenticate", "Basic realm=\"Restricted\"");
                res.status(HTTP_UNAUTHORIZED);
                halt(HTTP_UNAUTHORIZED);
            }
        });
    }

    /**
     * @param toDecode the value of request header "Authorization".
     * @return
     */
    private String decode(String toDecode) {
        return new String(
                java.util.Base64.getDecoder().decode(
                        toDecode.substring("Basic".length()).trim()));
    }

    public Router(
        final Controller controller,
        final String[] typeNames,
        final String[] credentials
    ){
        this.controller= controller;

        for (String typeName:typeNames)
            setUpTypeRoutes(typeName);

        setUpAuthorization(credentials);
    }
}

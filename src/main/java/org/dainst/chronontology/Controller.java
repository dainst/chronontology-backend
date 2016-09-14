package org.dainst.chronontology;

import org.apache.log4j.Logger;
import org.dainst.chronontology.config.AppConfig;
import org.dainst.chronontology.handler.*;
import org.dainst.chronontology.handler.dispatch.Dispatcher;
import org.dainst.chronontology.handler.model.RightsValidator;
import spark.Request;
import spark.Response;

import java.util.Arrays;

import static org.dainst.chronontology.Constants.*;
import static spark.Spark.*;


/**
 * @author Daniel de Oliveira
 */
public class Controller {

    private final static Logger logger = Logger.getLogger(Controller.class);
    public static final String ID = ":id";

    private final Dispatcher dispatcher;
    private final SearchDocumentHandler searchDocumentHandler;
    private final ServerStatusHandler serverStatusHandler;
    private final PostDocumentHandler postDocumentHandler;
    private final PutDocumentHandler putDocumentHandler;
    private final GetDocumentHandler getDocumentHandler;

    private void setUpTypeRoutes(
            final String typeName
    ) {

        get( "/", (req,res) -> {
            setHeader(res);
            return serverStatusHandler.handle(req,res);
        });

        get( "/" + typeName, (req,res) -> {
            setHeader(res);
            return searchDocumentHandler.handle(req,res);
        });
        get( "/" + typeName + "/", (req,res) -> {
            setHeader(res);
            return searchDocumentHandler.handle(req,res);
        });

        get( "/" + typeName + "/" + ID, (req,res) -> {
            setHeader(req,res,typeName);
            return getDocumentHandler.handle(req,res);
        });

        post("/" + typeName, (req, res) ->  {
            setHeader(res);
            return postDocumentHandler.handle(req,res);
        });
        post("/" + typeName + "/", (req, res) ->  {
            setHeader(res);
            return postDocumentHandler.handle(req,res);
        });

        put( "/" + typeName + "/" + ID, (req, res) -> {
            setHeader(req,res,typeName);
            return putDocumentHandler.handle(req,res);
        });

    }

    private void setHeader(Response res) {
        res.header(HEADER_CT, HEADER_JSON);
    }

    private void setHeader(Request req, Response res,String typeName) {
        res.header(HEADER_CT, HEADER_JSON);
        res.header(HEADER_LOC, "/"+typeName+"/"+req.params(ID));
    }

    private void setUpAuthorization(String[] credentials) {

        before("/*", (req, res) -> {

            boolean authenticated=
                    (requestHeaderAuth(req)) && isAuthenticatedRequest(req,credentials);
            if(!authenticated) anonymousRequest(req,res);
        });
    }

    private boolean requestHeaderAuth(Request req) {
        return req.headers(HEADER_AUTH) != null
                && req.headers(HEADER_AUTH).startsWith("Basic");
    }

    private boolean isAuthenticatedRequest(Request req,String[] credentials) {
        boolean authenticated= false;
        for (String cred:credentials) {
            if (decode(req.headers(HEADER_AUTH)).equals(cred)) {
                req.attribute("user", cred.split(":")[0]);
                authenticated = true;
            }
        }
        return authenticated;
    }

    private void anonymousRequest(Request req,Response res) {
        req.attribute("user", Constants.USER_NAME_ANONYMOUS);
        if (!req.requestMethod().equals("GET")) {
            res.header("WWW-Authenticate", "Basic realm=\"Restricted\"");
            res.status(HTTP_UNAUTHORIZED);
            halt(HTTP_UNAUTHORIZED);
        }
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

    public Controller(
            final Dispatcher dispatcher,
            final String[] typeNames,
            final String[] credentials,
            final RightsValidator rightsValidator
            ){

        this.dispatcher = dispatcher;
        this.searchDocumentHandler = new SearchDocumentHandler(dispatcher,rightsValidator);
        this.serverStatusHandler= new ServerStatusHandler(dispatcher);
        this.postDocumentHandler = new PostDocumentHandler(dispatcher,rightsValidator);
        this.putDocumentHandler = new PutDocumentHandler(dispatcher,rightsValidator);
        this.getDocumentHandler = new GetDocumentHandler(dispatcher,rightsValidator);

        logger.info("initializing endpoints for types: " + Arrays.toString(typeNames));

        for (String typeName:typeNames)
            setUpTypeRoutes(typeName);

        validateCredentials(credentials);
        setUpAuthorization(credentials);
    }

    private void validateCredentials(String[] credentials) {
        for (String cred:credentials) {
            if (cred.split(":")[0].equals(Constants.USER_NAME_ANONYMOUS)) {
                logger.error(AppConfig.MSG_RESERVED_USER_ANONYMOUS + " Will exit now.");
                System.exit(1);
            }
        }
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }
}

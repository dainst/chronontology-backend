package org.dainst.chronontology;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.log4j.Logger;
import org.dainst.chronontology.config.AppConfig;
import org.dainst.chronontology.handler.*;
import org.dainst.chronontology.handler.dispatch.Dispatcher;
import org.dainst.chronontology.handler.model.RightsValidator;
import org.dainst.chronontology.util.JsonUtils;
import spark.Request;
import spark.Response;
import spark.Spark;

import static org.dainst.chronontology.Constants.*;
import static spark.Spark.*;

import static org.dainst.chronontology.util.JsonUtils.json;


/**
 * @author Daniel de Oliveira
 */
public class Controller {

    private final static Logger logger = Logger.getLogger(Controller.class);
    public static final String ID = ":id";

    private final Dispatcher dispatcher;
    private final SearchDocumentHandler searchDocumentHandler;
    private final ServerStatusHandler serverStatusHandler;
    private final RebuildIndexHandler rebuildIndexHandler;
    private final PostDocumentHandler postDocumentHandler;
    private final PutDocumentHandler putDocumentHandler;
    private final GetDocumentHandler getDocumentHandler;
    private final UserHandler userHandler;

    private void setUpStatusRoute(
            final String routePrefix
    ) {
        logger.info("initializing endpoint for server status at '" + routePrefix +"'...");

        get(routePrefix, (req, res) -> {
            setHeader(res);
            return serverStatusHandler.handle(req, res);
        });
    }

    private void setUpRebuildIndexRoute(final String routePrefix, String[] credentials){
        final String rebuildRoute = routePrefix + "rebuild_index";

        logger.info("initializing endpoint for elastic search reindex at '" + routePrefix + "'...");

        get(rebuildRoute, (req, res) -> {
            setHeader(res);

            boolean authenticated = (hasAuthHeader(req)) && authenticate(req, credentials);
            if(!authenticated){
                return handleFailedAuthentication(req, res);
            }

            return rebuildIndexHandler.handle(req, res);
        });
    }

    private void setUpUserRoutes(
            final String routePrefix,
            String[] credentials
    ) {
        final String userRoute = routePrefix + "user/";

        logger.info("initializing endpoints for user handling at '" + userRoute + "*'...");

        get(userRoute + "login", (req, res) -> {
            setHeader(res);

            boolean authenticated = (hasAuthHeader(req)) && authenticate(req, credentials);

            if(!authenticated){
                return handleFailedAuthentication(req, res);
            }
            return userHandler.handle(req,res);
        });
    }

    private void setUpTypeRoutes(
            final String routePrefix,
            final String typeName,
            String[] credentials

    ) {
        logger.info("initializing endpoints for type: " + typeName + " at '" + routePrefix+typeName+"/*'...");

        before(routePrefix + typeName + "/*", (req, res) -> {
            boolean authenticated = (hasAuthHeader(req)) && authenticate(req, credentials);
            if(!authenticated)
                handleAnonymousTypeRouteRequest(req, res);
        });

        before(routePrefix + typeName, (req, res) -> {
            boolean authenticated = (hasAuthHeader(req)) && authenticate(req, credentials);
            if(!authenticated)
                handleAnonymousTypeRouteRequest(req, res);
        });

        get( routePrefix + typeName, (req,res) -> {
            setHeader(res);
            return searchDocumentHandler.handle(req,res);
        });
        get( routePrefix + typeName + "/", (req,res) -> {
            setHeader(res);
            return searchDocumentHandler.handle(req,res);
        });

        get( routePrefix + typeName + "/" + ID, (req,res) -> {
            setHeader(req,res,typeName);
            return getDocumentHandler.handle(req,res);
        });

        post(routePrefix + typeName, (req, res) ->  {
            setHeader(res);
            return postDocumentHandler.handle(req,res);
        });
        post(routePrefix + typeName + "/", (req, res) ->  {
            setHeader(res);
            return postDocumentHandler.handle(req,res);
        });

        put( routePrefix + typeName + "/" + ID, (req, res) -> {
            setHeader(req,res,typeName);
            return putDocumentHandler.handle(req,res);
        });
    }

    private void setUpAllTypesRoute(
            final String routePrefix,
            final String[] credentials
    ) {
        get( routePrefix + '_' + "/", (req,res) -> {
            boolean authenticated = (hasAuthHeader(req)) && authenticate(req, credentials);
            if(!authenticated)
                handleAnonymousTypeRouteRequest(req, res);

            setHeader(res);
            return searchDocumentHandler.handle(req,res);
        });
    }



    private void setHeader(Response res) {
        res.header(HEADER_CT, HEADER_JSON);
    }

    private void setHeader(Request req, Response res,String typeName) {
        res.header(HEADER_CT, HEADER_JSON);
        res.header(HEADER_LOC, "/"+typeName+"/"+req.params(ID));
    }

    private void handleAnonymousTypeRouteRequest(Request req, Response res) {
        req.attribute("user", Constants.USER_NAME_ANONYMOUS);
        if (!req.requestMethod().equals("GET")) {
            res.header("WWW-Authenticate", "Basic realm=\"Restricted\"");
            res.status(HTTP_UNAUTHORIZED);
            halt(HTTP_UNAUTHORIZED);
        }
    }

    private Object handleFailedAuthentication(Request req, Response res) {
        req.attribute("user", Constants.USER_NAME_ANONYMOUS);
        res.header("WWW-Authenticate", "Basic realm=\"Restricted\"");
        res.status(HTTP_BAD_REQUEST);

        JsonNode n= JsonUtils.json();
        ((ObjectNode)n).put("status","failure");
        return n;
    }

    private boolean hasAuthHeader(Request req) {
        return req.headers(HEADER_AUTH) != null && req.headers(HEADER_AUTH).startsWith("Basic");
    }

    private boolean authenticate(Request req, String[] credentials) {
        boolean authenticated= false;
        for (String cred:credentials) {
            if (decode(req.headers(HEADER_AUTH)).equals(cred)) {
                req.attribute("user", cred.split(":")[0]);
                authenticated = true;
            }
        }
        return authenticated;
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
            final RightsValidator rightsValidator,
            final boolean SPASupport
            ){

        this.dispatcher = dispatcher;
        this.searchDocumentHandler = new SearchDocumentHandler(dispatcher,rightsValidator);
        this.serverStatusHandler= new ServerStatusHandler(dispatcher);
        this.rebuildIndexHandler = new RebuildIndexHandler(dispatcher, typeNames);
        this.userHandler = new UserHandler(dispatcher);
        this.postDocumentHandler = new PostDocumentHandler(dispatcher,rightsValidator);
        this.putDocumentHandler = new PutDocumentHandler(dispatcher,rightsValidator);
        this.getDocumentHandler = new GetDocumentHandler(dispatcher,rightsValidator);


        String routePrefix = "/";
        if (SPASupport) {
            logger.info("Single Page Application Support enabled. Will server ./public folder as /.");
            Spark.externalStaticFileLocation("./public");
            logger.info("The api routes will be accessible under /data/.");
            routePrefix = "/data/";
        }

        setUpStatusRoute(routePrefix);

        setUpRebuildIndexRoute(routePrefix, credentials);
        setUpUserRoutes(routePrefix, credentials);

        setUpAllTypesRoute(routePrefix,credentials);
        for (String typeName:typeNames) {
            setUpTypeRoutes(routePrefix, typeName, credentials);
        }

        validateCredentials(credentials);
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

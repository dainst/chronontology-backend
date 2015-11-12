package org.dainst;

import static org.dainst.C.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import java.io.IOException;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

/**
 * @author Daniel M. de Oliveira
 */
public class Router {

    final static Logger logger = Logger.getLogger(Router.class);

    private static JsonNode json(String s) throws IOException {
        return new ObjectMapper().readTree(s);
    }

    private boolean shouldBeDirect(final String directParam) {
        return (directParam!=null&&
                directParam.equals("true"));
    }

    /**
     * Converts a String to an int.
     *
     * @param sizeParam
     * @return -1 if sizeParam is null
     *   or cannot be parsed properly.
     */
    private Integer sizeAsInt(final String sizeParam) {
        if (sizeParam==null) return -1;
        int size = -1;
        try {
            if (sizeParam != null)
                size = Integer.parseInt(sizeParam);
        } catch (NumberFormatException e) {
            logger.error("Illegal format for number in param: " + sizeParam);
            return -1;
        }
        return size;
    }

    public Router(
            final FileSystemDatastoreConnector mainDatastore,
            final ElasticSearchDatastoreConnector connectDatastore
    ){

        get("/"+TYPE_NAME+"/", (req,res) -> {

                    return connectDatastore.search(
                        req.queryParams("q"), sizeAsInt(req.queryParams("size")));
                }
        );

        get("/"+TYPE_NAME+"/:id", (req,res) -> {

                    if (shouldBeDirect(req.queryParams("direct")))
                        return mainDatastore.get(req.params(":id"));
                    else
                        return connectDatastore.get(req.params(":id"));
                }
        );

        post("/" + TYPE_NAME + "/:id", (req, res) -> {

                    JsonNode json = new DocumentModel(json(req.body()))
                            .addStorageInfo(req.params(":id"));
                    mainDatastore.put(req.params(":id"), json);
                    connectDatastore.put(req.params(":id"), json);

                    res.header("location", req.params(":id"));
                    res.status(200);

                    return json;
                }
        );

        put("/" + TYPE_NAME + "/:id", (req, res) -> {

                    JsonNode oldJson = mainDatastore.get(req.params(":id"));
                    JsonNode json = new DocumentModel(json(req.body()))
                            .addStorageInfo(oldJson, req.params(":id"));

                    mainDatastore.put(req.params(":id"), json);
                    connectDatastore.put(req.params(":id"), json);

                    res.header("location", req.params(":id"));
                    res.status(200);

                    return json;
                }
        );
    }
}

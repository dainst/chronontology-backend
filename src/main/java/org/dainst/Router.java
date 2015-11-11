package org.dainst;

import static org.dainst.C.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import spark.QueryParamsMap;
import spark.Request;

import java.io.IOException;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 * @author Daniel M. de Oliveira
 */
public class Router {

    private static JsonNode jsonNode(String s) throws IOException {
        return new ObjectMapper().readTree(s);
    }

    private static JsonNode addStorageInfo(JsonNode jsonNode, String id) throws IOException {
        ((ObjectNode) jsonNode).put("@id", "/"+TYPE_NAME+"/"+id);
        return jsonNode; 
    }

    private boolean shouldBeDirect(Request req) {
        return ((req.queryParams("direct")!=null)
                &&(req.queryParams("direct").equals("true")));
    }

    private Integer getSize(final QueryParamsMap queryParams) {
        Integer size = null;
        try {
            if (queryParams.get("size") != null)
                size = Integer.parseInt(queryParams.get("size").value());
        } catch (NumberFormatException e) {
            // TODO logger error
            System.out.println("Illegal format: "+queryParams.get("size").value());
            return null;
        }
        if (size>0)
            return size;
        else return null;
    }

    public Router(
            final FileSystemDatastoreConnector mainDatastore,
            final ElasticSearchDatastoreConnector connectDatastore
    ){

        get("/"+TYPE_NAME+"/", (req,res) -> {

                    return connectDatastore.search(
                        req.queryParams("q"), getSize(req.queryMap()));
                }
        );

        get("/"+TYPE_NAME+"/:id", (req,res) -> {

                    if (shouldBeDirect(req))
                        return mainDatastore.get(req.params(":id"));
                    else
                        return connectDatastore.get(req.params(":id"));
                }
        );

        post("/" + TYPE_NAME + "/:id", (req, res) -> {

                    JsonNode enrichedJSON = addStorageInfo(jsonNode(req.body()), req.params(":id"));
                    mainDatastore.put(req.params(":id"), enrichedJSON);
                    connectDatastore.put(req.params(":id"), enrichedJSON);

                    res.header("location", req.params(":id"));
                    res.status(200);

                    return enrichedJSON;
                }
        );
    }
}

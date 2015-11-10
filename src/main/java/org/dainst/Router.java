package org.dainst;

import static org.dainst.C.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import spark.Request;

import java.io.IOException;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 * Created by daniel on 08.11.15.
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

    public Router(
            FileSystemDatastoreConnector mainDatastore,
            ElasticSearchDatastoreConnector connectDatastore
    ){

        get("/"+TYPE_NAME+"/", (req,res) -> {

                    Integer size = null;
                    if (req.queryParams("size")!=null)
                        size= Integer.parseInt(req.queryParams("size"));

                    JsonNode results = connectDatastore.search(
                            req.queryParams("q"),size);
                    return results;
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

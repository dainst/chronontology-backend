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


    private static JsonNode enrichJSON(String body, String id) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(body);
        ((ObjectNode) jsonNode).put("@id", "/"+TYPE_NAME+"/"+id);
        String json = mapper.writeValueAsString(jsonNode);
        return mapper.readTree(json);
    }

    private boolean shouldBeDirect(Request req) {
        return ((req.queryParams("direct")!=null)
                &&(req.queryParams("direct").equals("true")));
    }

    public Router(
            FileSystemDatastoreConnector mainDatastore,
            ElasticSearchDatastoreConnector connectDatastore
    ){

        get("/"+TYPE_NAME+"/:id", (req,res) -> {

                    if (shouldBeDirect(req))
                        return mainDatastore.get(req.params(":id"));
                    else
                        return connectDatastore.get(req.params(":id"));
                }
        );

        post("/" + TYPE_NAME + "/:id", (req, res) -> {

                    JsonNode enrichedJSON = enrichJSON(req.body(), req.params(":id"));
                    mainDatastore.put(req.params(":id"), enrichedJSON);
                    connectDatastore.put(req.params(":id"), enrichedJSON);

                    res.header("location", req.params(":id"));
                    res.status(200);

                    return enrichedJSON;
                }
        );
    }
}

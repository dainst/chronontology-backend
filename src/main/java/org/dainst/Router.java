package org.dainst;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 * Created by daniel on 08.11.15.
 */
public class Router {

    private static final String TYPE_NAME = "period";

    private static String enrichJSON(String body, String id) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(body);
        ((ObjectNode) jsonNode).put("@id", "/"+TYPE_NAME+"/"+id);
        String json = mapper.writeValueAsString(jsonNode);
        return json;
    }

    public Router(FileSystemDatastore store){

        get("/"+TYPE_NAME+"/:id", (req,res) -> {
                    return store.get(req.params(":id"));
                }
        );

//        TODO enable this endpoint when elasticsearch is ready
//        get("/"+TYPE_NAME, (req,res) -> {
//
//
//                    for (String param:req.queryParams()){
//                        System.out.println(param);
//                        String q= req.queryParams(param);
//
//                        System.out.println(q);
//                    }
//
//                    return "";
//                }
//        );

        post("/" + TYPE_NAME + "/:id", (req, res) -> {

                    String enrichedJSON = enrichJSON(req.body(), req.params(":id"));
                    store.put(req.params(":id"), enrichedJSON);

                    res.header("location", req.params(":id"));
                    res.status(200);

                    return enrichedJSON;
                }
        );
    }
}

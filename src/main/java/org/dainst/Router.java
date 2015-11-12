package org.dainst;

import static org.dainst.C.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 * @author Daniel M. de Oliveira
 */
public class Router {

    final static Logger logger = Logger.getLogger(Router.class);

    private static JsonNode jsonNode(String s) throws IOException {
        return new ObjectMapper().readTree(s);
    }

    private static JsonNode addStorageInfo(JsonNode jsonNode, String id) throws IOException {
        ((ObjectNode) jsonNode).put("@id", "/"+TYPE_NAME+"/"+id);
        String nowAsIso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ").format(new Date());

        ((ObjectNode) jsonNode).put("created", nowAsIso8601);
        ArrayNode a = ((ObjectNode) jsonNode).putArray("modified");
        a.add(nowAsIso8601);

        return jsonNode;
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

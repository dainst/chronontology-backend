import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;

import static spark.Spark.get;
import static spark.Spark.post;


/**
 * @author Daniel M. de Oliveira
 */
public class Chronontology {

    private static final String DEFAULT_DATASTORE_PATH = "datastore/";
    private static final String TYPE_NAME = "period";

    private static FileSystemDatastore initDS(String datastorePath) {

        if (!(new File(datastorePath).exists())) {
            System.out.println("The specified path "+datastorePath+" does not exist.");
            return null;
        }
        return new FileSystemDatastore(datastorePath);
    }

    private static String enrichJSON(String body, String id) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(body);
        ((ObjectNode) jsonNode).put("@id", "/"+TYPE_NAME+"/"+id);
        String json = mapper.writeValueAsString(jsonNode);
        return json;
    }

    public static void main(String [] args) {

        final FileSystemDatastore store= (args.length==1) ? initDS(args[0]) : initDS(DEFAULT_DATASTORE_PATH);
        if (store==null) {
            System.out.println("Could not initialize datastore.");
            System.exit(1);
        }



        get("/"+TYPE_NAME+"/:id", (req,res) -> {
                    return store.get(req.params(":id"));
                }
        );

        post("/"+TYPE_NAME+"/:id", (req,res) -> {

                    String enrichedJSON = enrichJSON(req.body(),req.params(":id"));
                    store.put(req.params(":id"),enrichedJSON);

                    res.header("location",req.params(":id"));
                    res.status(200);

                    return enrichedJSON;
                }
        );
    }
}

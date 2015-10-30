import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

/**
 * @author Daniel M. de Oliveira
 */
public class Chronontology {

    private static Map<String,String> store= new HashMap<String,String>();

    public static void main(String [] args) {

        get("/resource/:id", (req,res) -> {
                    return store.get(req.params(":id"));
                }
        );

        post("/resource/:id", (req,res) -> {
                    store.put(req.params(":id"),req.body());
                    return "acknowledged";
                }
        );
    }
}

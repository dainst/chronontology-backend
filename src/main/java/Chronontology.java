import static spark.Spark.*;

/**
 * @author Daniel M. de Oliveira
 */
public class Chronontology {


    private static Datastore store = new Datastore();


    public static void main(String [] args) {

        get("/period/:id", (req,res) -> {
                    return store.get(req.params(":id"));
                }
        );

        post("/period/:id", (req,res) -> {
                    store.put(req.params(":id"),req.body());
                    return "acknowledged";
                }
        );
    }
}

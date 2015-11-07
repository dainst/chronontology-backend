import static spark.Spark.*;

/**
 * @author Daniel M. de Oliveira
 */
public class Chronontology {

    private static final String BASE_FOLDER = "src/test/resources/";
    private static FileSystemDatastore store = new FileSystemDatastore(BASE_FOLDER);

    public static void main(String [] args) {

        get("/period/:id", (req,res) -> {
                    return store.get(req.params(":id"));
                }
        );

        post("/period/:id", (req,res) -> {
                    store.put(req.params(":id"),req.body());
                    res.header("location",req.params(":id"));
                    res.status(200);
                    return req.body();
                }
        );
    }
}

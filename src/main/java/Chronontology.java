import java.io.File;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 * @author Daniel M. de Oliveira
 */
public class Chronontology {

    private static final String DEFAULT_DATASTORE_PATH = "datastore/";
    private static FileSystemDatastore store = null;

    private static FileSystemDatastore initDS(String datastorePath) {

        if (!(new File(datastorePath).exists())) {
            System.out.println("The specified path "+datastorePath+" does not exist.");
            return null;
        }
        return new FileSystemDatastore(datastorePath);
    }

    public static void main(String [] args) {

        store= (args.length==1) ? initDS(args[0]) : initDS(DEFAULT_DATASTORE_PATH);
        if (store==null) {
            System.out.println("Could not initialize datastore.");
            System.exit(1);
        }


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

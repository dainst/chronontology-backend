import java.io.File;

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

    public static void main(String [] args) {

        final FileSystemDatastore store= (args.length==1) ? initDS(args[0]) : initDS(DEFAULT_DATASTORE_PATH);
        if (store==null) {
            System.out.println("Could not initialize datastore.");
            System.exit(1);
        }
        new Router(store);
    }
}

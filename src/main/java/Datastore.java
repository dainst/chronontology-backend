import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel M. de Oliveira
 */
public class Datastore {

    private static Map<String,String> store= new HashMap<String,String>();

    public String get(String key) {
        return store.get(key);
    };

    public void put(String key,String value) {
        store.put(key,value);
    }
}

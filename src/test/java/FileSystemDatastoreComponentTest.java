import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertEquals;

/**
 * @author Daniel M. de Oliveira
 */
public class FileSystemDatastoreComponentTest {

    private static final String BASE_FOLDER = "src/test/resources/";

    @AfterClass
    public static void afterClass() {
        new File(BASE_FOLDER + "a.txt").delete();
    }

    @Test
    public void putAndGet() {

        FileSystemDatastore store = new FileSystemDatastore(BASE_FOLDER);
        store.put("a","a");
        assertEquals(store.get("a"),"a");
    }
}

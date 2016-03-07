package org.dainst.chronontology.config;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Daniel M. de Oliveira
 */
public class FilesystemDatastoreConfigTest {

    Properties props = null;

    @BeforeMethod
    public void before() {
        props= new Properties();
    }


    @Test
    public void testFilesystem() {
        String path= "ds/";
        props.put("datastore.filesystem.path",path);

        FilesystemDatastoreConfig config= new FilesystemDatastoreConfig();
        assertTrue(config.validate(props));
        assertEquals(config.getPath(),path);
    }

    @Test
    public void omitPathWithFilesystem() {

        FilesystemDatastoreConfig config= new FilesystemDatastoreConfig();
        assertTrue(config.validate(props));
        assertEquals(config.getPath(),ConfigConstants.DATASTORE_PATH);
    }
}

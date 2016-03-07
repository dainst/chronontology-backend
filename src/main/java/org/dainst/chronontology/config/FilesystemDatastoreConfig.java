package org.dainst.chronontology.config;

import org.dainst.chronontology.Constants;

import java.util.ArrayList;
import java.util.Properties;

/**
 * @author Daniel M. de Oliveira
 */
public class FilesystemDatastoreConfig extends Config {

    private String path= null;

    public FilesystemDatastoreConfig() {
        this.prefix= "datastore.filesystem.";
    }

    @Override
    public boolean validate(Properties props) {
        if (props==null) throw new IllegalArgumentException(Constants.MSG_PROPS_NOT_NULL);

        return (
            _validate(props,"path", ConfigConstants.DATASTORE_PATH)
        );
    }

    @Override
    public ArrayList<String> getConstraintViolations() {
        return constraintViolations;
    }

    public String getPath() {
        return path;
    }

    void setPath(String path) {
        this.path= path;
    }
}

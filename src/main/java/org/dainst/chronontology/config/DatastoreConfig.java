package org.dainst.chronontology.config;

import org.dainst.chronontology.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

/**
 * @author Daniel M. de Oliveira
 */
public class DatastoreConfig extends Config {

    public static final String TYPE_ELASTICSEARCH = "elasticsearch";
    private String indexName= null;
    private String url= null;
    private String type= null;
    private String path= null;

    public DatastoreConfig(String id) {
        this.prefix= "datastores."+id+".";
    }

    @Override
    public boolean validate(Properties props) {
        if (props==null) throw new IllegalArgumentException(Constants.MSG_PROPS_NOT_NULL);

        if (!_validate(props,"type", ConfigConstants.DATASTORE_TYPE_ES)) return false;

        if (type.equals(TYPE_ELASTICSEARCH))
            return (
                _validate(props,"indexName", ConfigConstants.ES_INDEX_NAME) &&
                _validate(props,"url", ConfigConstants.EMBEDDED_ES_URL)
            );
        else
            return (
                _validate(props,"path", ConfigConstants.DATASTORE_PATH)
            );
    }

    @Override
    public ArrayList<String> getConstraintViolations() {
        return constraintViolations;
    }

    public String getIndexName() {
        return indexName;
    }

    public String getUrl() {
        return url;
    }

    void setUrl(String url) {
        this.url= url;
    }

    void setIndexName(String indexName) {
        this.indexName= indexName;
    }

    public String getPath() {
        return path;
    }

    void setPath(String path) {
        this.path= path;
    }

    void setType(String type) {
        if (!(type.equals(ConfigConstants.DATASTORE_TYPE_ES)
                ||type.equals(ConfigConstants.DATASTORE_TYPE_FS)))
            throw new ConfigValidationException("Datastore must either be of type \""
                    +ConfigConstants.DATASTORE_TYPE_ES+"\" or of type \""+ConfigConstants.DATASTORE_TYPE_FS+"\".");

        this.type= type;
    }

    public String getType() {
        return this.type;
    }
}

package org.dainst.chronontology.config;

import java.util.Properties;

/**
 * @author Daniel M. de Oliveira
 */
public class DatastoreConfig extends Config {

    public static final String TYPE_ELASTICSEARCH = "elasticsearch";
    private String indexName = ConfigConstants.ES_INDEX_NAME;
    private String url = ConfigConstants.EMBEDDED_ES_URL;
    private String type = ConfigConstants.DATASTORE_TYPE_ES;
    private String path = ConfigConstants.DATASTORE_PATH;

    public DatastoreConfig(String id) {
        this.prefix= "datastores."+id+".";
    }

    @Override
    public boolean validate(Properties props) {
        if (!_validate(props,"type", true)) return false;

        if (type.equals(TYPE_ELASTICSEARCH))
            return (
                _validate(props,"indexName", true) &&
                _validate(props,"url", true)
            );
        else
            return (
                _validate(props,"path", true)
            );
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

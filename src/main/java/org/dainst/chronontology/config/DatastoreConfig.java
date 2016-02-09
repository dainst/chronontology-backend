package org.dainst.chronontology.config;

import java.util.Properties;

/**
 * @author Daniel M. de Oliveira
 */
public class DatastoreConfig
        extends Config
        implements ElasticsearchDatastoreConfig, FilesystemDatastoreConfig {

    public static final String TYPE_ELASTICSEARCH = "elasticsearch";
    private String indexName = ConfigConstants.ES_INDEX_NAME;
    private String url = ConfigConstants.EMBEDDED_ES_URL;
    private String type = "elasticsearch";
    private String path = ConfigConstants.DATASTORE_PATH;

    public DatastoreConfig(String id) {
        this.prefix= "datastores."+id+".";
    }

    @Override
    public boolean validate(Properties props) {
        _validate(props,"type", true);

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

    @Override
    public String getIndexName() {
        return indexName;
    }

    @Override
    public String getUrl() {
        return url;
    }

    void setUrl(String url) {
        this.url= url;
    }

    void setIndexName(String indexName) {
        this.indexName= indexName;
    }

    @Override
    public String getPath() {
        return path;
    }

    void setPath(String path) {
        this.path= path;
    }

    void setType(String type) {
        this.type= type;
    }
}

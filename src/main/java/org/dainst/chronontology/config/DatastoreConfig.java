package org.dainst.chronontology.config;

import org.dainst.chronontology.Constants;

import java.util.Properties;

/**
 * @author Daniel M. de Oliveira
 */
public class DatastoreConfig
        extends Config
        implements ElasticSearchConfig, FilesystemDatastoreConfig {

    public static final String TYPE_ELASTICSEARCH = "elasticsearch";
    private String indexName = Constants.ES_INDEX_NAME;
    private String url = Constants.EMBEDDED_ES_URL;
    private String type = "elasticsearch";
    private String path = Constants.DATASTORE_PATH;

    public DatastoreConfig(String prefix) {
        this.prefix= prefix;
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

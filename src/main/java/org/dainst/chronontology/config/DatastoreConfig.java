package org.dainst.chronontology.config;

import org.dainst.chronontology.Constants;

import java.util.Properties;

/**
 * @author Daniel M. de Oliveira
 */
public class DatastoreConfig extends Config implements ElasticSearchConfig {

    Properties props = null;
    private String indexName = Constants.ES_INDEX_NAME;
    private String url = Constants.EMBEDDED_ES_URL;

    public DatastoreConfig(Properties props,String prefix) {
        this.prefix= prefix;
        this.props= props;
    }

    public boolean validate() {
        return (
            _validate(props,"indexName", true) &&
            _validate(props,"url", true)
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


}

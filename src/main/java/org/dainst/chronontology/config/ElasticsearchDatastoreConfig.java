package org.dainst.chronontology.config;

import org.dainst.chronontology.Constants;

import java.util.ArrayList;
import java.util.Properties;

/**
 * @author Daniel M. de Oliveira
 */
public class ElasticsearchDatastoreConfig extends Config {

    private String indexName;
    private String url;

    public ElasticsearchDatastoreConfig() {
        this.prefix= "datastore.elasticsearch.";
    }

    @Override
    public boolean validate(Properties props) {
        if (props==null) throw new IllegalArgumentException(Constants.MSG_PROPS_NOT_NULL);

        return (
            _validate(props,"indexName", ConfigConstants.ES_INDEX_NAME) &&
            _validate(props,"url", ConfigConstants.EMBEDDED_ES_URL)
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
}

package org.dainst.chronontology.config;

import java.util.Properties;

/**
 * @author Daniel M. de Oliveira
 */
public class ElasticsearchServerConfig extends Config {

    private String port = ConfigConstants.EMBEDDED_ES_PORT;
    private String dataPath = ConfigConstants.ES_SERVER_DATA_PATH;
    private String clusterName = ConfigConstants.ES_SERVER_CLUSTER_NAME;

    public ElasticsearchServerConfig() {
        this.prefix= "esServer.";
    }

    @Override
    public boolean validate(Properties props) {
        return (
            _validate(props,"port", true) &&
            _validate(props,"dataPath", true) &&
            _validate(props,"clusterName", true)
        );
    }

    public String getPort() {
        return port;
    }

    void setPort(String port) {
        this.port= port;
    }

    void setDataPath(String dataPath) {
        this.dataPath= dataPath;
    }

    void setClusterName(String clusterName) {
        this.clusterName= clusterName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public String getDataPath() {
        return dataPath;
    }
}
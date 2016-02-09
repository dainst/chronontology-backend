package org.dainst.chronontology.config;

import org.apache.log4j.Logger;

import java.util.Properties;

/**
 * @author Daniel M. de Oliveira
 */
public class AppConfig extends Config {

    final static Logger logger = Logger.getLogger(AppConfig.class);

    private String serverPort = ConfigConstants.SERVER_PORT;
    private String[] credentials = null;
    private String typeNames = null;
    private boolean useEmbeddedES = false;
    private boolean useConnect = true;
    private DatastoreConfig[] datastoreConfigs = new DatastoreConfig[2];
    private ElasticsearchServerConfig elasticsearchServerConfig = null;

    /**
     * @param props
     * @return true if all the properties for running the application
     *   could be loaded properly. false otherwise.
     */
    @Override
    public boolean validate(Properties props) {

        return (
            validateDatastores(props) &&
            _validate(props,"serverPort",true) &&
            validateEsServer(props) &&
            _validate(props,"credentials") &&
            _validate(props,"typeNames")
            );
    }

    private boolean validateDatastores(Properties props) {
        if (!_validate(props,"useConnect",true)) return false;

        boolean datastoresValidated= true;
        datastoreConfigs[0]= new DatastoreConfig("0");
        if (!datastoreConfigs[0].validate(props)) return false;
        if (!datastoreConfigs[0].getType().equals(ConfigConstants.DATASTORE_TYPE_ES)) {
            logger.error(ConfigConstants.MSG_CONSTRAINT_VIOLATION+"datastores.0 must be of type \""+
                    ConfigConstants.DATASTORE_TYPE_ES+"\".");
            return false;
        }

        if (useConnect) {
            datastoreConfigs[1]= new DatastoreConfig("1");
            if (!datastoreConfigs[1].validate(props)) datastoresValidated=false;
        }
        return datastoresValidated;
    }

    private boolean validateEsServer(Properties props) {
        if (!_validate(props,"useEmbeddedES", true)) return false;
        if (useEmbeddedES) {
            elasticsearchServerConfig= new ElasticsearchServerConfig();
            return elasticsearchServerConfig.validate(props);
        }
        return true;
    }

    public String getServerPort() {
        return serverPort;
    }

    void setServerPort(String serverPort) {
        this.serverPort= serverPort;
    }

    public String getTypeNames() {
        return this.typeNames;
    }

    void setTypeNames(String typeNames) {
        this.typeNames= typeNames;
    }

    public String[] getCredentials() {
        return credentials;
    }

    void setCredentials(String credentials) {
        this.credentials= credentials.split(",");
    }

    void setUseEmbeddedES(String useIt) {
        if (useIt.equals("true")) useEmbeddedES= true;
    }

    public boolean isUseConnect() {
        return useConnect;
    }

    void setUseConnect(String useIt) {
        if (useIt.equals("false")) useConnect = false;
    }

    public DatastoreConfig[] getDatastoreConfigs() {
        return datastoreConfigs;
    }

    public ElasticsearchServerConfig getElasticsearchServerConfig() {
        return elasticsearchServerConfig;
    }
}

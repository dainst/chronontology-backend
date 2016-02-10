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
    private ElasticsearchServerConfig elasticsearchServerConfig = null;
    private ControllerConfig controllerConfig = new ControllerConfig();

    /**
     * @param props
     * @return true if all the properties for running the application
     *   could be loaded properly. false otherwise.
     */
    @Override
    public boolean validate(Properties props) {

        return (
            controllerConfig.validate(props) &&
            _validate(props,"serverPort",true) &&
            validateEsServer(props) &&
            _validate(props,"credentials") &&
            _validate(props,"typeNames")
            );
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

    public ElasticsearchServerConfig getElasticsearchServerConfig() {
        return elasticsearchServerConfig;
    }

    public ControllerConfig getControllerConfig() {
        return controllerConfig;
    }
}

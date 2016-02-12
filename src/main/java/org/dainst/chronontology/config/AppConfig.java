package org.dainst.chronontology.config;

import java.util.ArrayList;
import java.util.Properties;

/**
 * @author Daniel M. de Oliveira
 */
public class AppConfig extends Config {

    final static String MSG_SERVER_PORT_NAN= "Server port must be a number, but is: ";

    private String serverPort= null;
    private String[] credentials = null;
    private String typeNames = null;
    private ElasticsearchServerConfig elasticsearchServerConfig = null;
    private ControllerConfig controllerConfig = new ControllerConfig();
    private Properties props = null;

    /**
     * @param props
     * @return true if all the properties for running the application
     *   could be loaded properly. false otherwise.
     */
    @Override
    public boolean validate(Properties props) {
        this.props= props;

        return (
            controllerConfig.validate(props) &
            _validate(props,"serverPort",ConfigConstants.SERVER_PORT) &
            _validate(props,"useEmbeddedES", "false") &
            _validate(props,"credentials") &
            _validate(props,"typeNames")
            );
    }

    @Override
    public ArrayList<String> getConstraintViolations() {
        ArrayList<String> allViolations= new ArrayList<String>();
        allViolations.addAll(constraintViolations);
        allViolations.addAll(controllerConfig.getConstraintViolations());
        return allViolations;
    }



    public String getServerPort() {
        return serverPort;
    }

    void setServerPort(String serverPort) {
        try {
            Integer.parseInt(serverPort);
        } catch (NumberFormatException e) {
            throw new ConfigValidationException(MSG_SERVER_PORT_NAN+"\""+serverPort+"\".");
        }
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
        if (useIt.equals("true")) {
            elasticsearchServerConfig= new ElasticsearchServerConfig();
            if (!elasticsearchServerConfig.validate(props)) throw new ConfigValidationException();
        }
    }

    public ElasticsearchServerConfig getElasticsearchServerConfig() {
        return elasticsearchServerConfig;
    }

    public ControllerConfig getControllerConfig() {
        return controllerConfig;
    }
}

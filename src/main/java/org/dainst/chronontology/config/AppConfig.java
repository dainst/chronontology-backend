package org.dainst.chronontology.config;

import org.dainst.chronontology.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

/**
 * @author Daniel M. de Oliveira
 */
public class AppConfig extends Config {

    final static String MSG_SERVER_PORT_NAN= "Server port must be a number, but is: ";
    public final static String MSG_RESERVED_USER_ANONYMOUS=
            "The user name \""+Constants.USER_NAME_ANONYMOUS+"\" is reserved for internal use and therefore cannot be used.";

    private String serverPort= null;
    private String[] credentials = null;
    private String typeNames = null;
    private ElasticsearchServerConfig elasticsearchServerConfig = null;
    private RightsValidatorConfig rightsValidatorConfig= new RightsValidatorConfig();
    private DispatcherConfig dispatcherConfig = new DispatcherConfig();
    private Properties props = null;

    /**
     * @param props
     * @return true if all the properties for running the application
     *   could be loaded properly. false otherwise.
     */
    @Override
    public boolean validate(Properties props) {
        if (props==null) throw new IllegalArgumentException(Constants.MSG_PROPS_NOT_NULL);
        this.props= props;

        return (
            dispatcherConfig.validate(props) &
            rightsValidatorConfig.validate(props) &
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
        allViolations.addAll(dispatcherConfig.getConstraintViolations());
        allViolations.addAll(rightsValidatorConfig.getConstraintViolations());
        if (elasticsearchServerConfig!=null)
            allViolations.addAll(elasticsearchServerConfig.getConstraintViolations());
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

        for (String credential: Arrays.asList(credentials)) {
            String userName= credential.split(":")[0];
            if (userName.equals(Constants.USER_NAME_ANONYMOUS))
                throw new ConfigValidationException(MSG_RESERVED_USER_ANONYMOUS);
        }
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

    public DispatcherConfig getDispatcherConfig() {
        return dispatcherConfig;
    }

    public RightsValidatorConfig getRightsValidatorConfig() {
        return rightsValidatorConfig;
    }
}

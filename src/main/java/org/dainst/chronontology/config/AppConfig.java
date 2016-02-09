package org.dainst.chronontology.config;

import org.apache.log4j.Logger;
import org.dainst.chronontology.Constants;

import java.util.Properties;

/**
 * @author Daniel M. de Oliveira
 */
public class AppConfig extends Config {

    final static Logger logger = Logger.getLogger(AppConfig.class);

    private String serverPort = Constants.SERVER_PORT;
    private String esPort = Constants.EMBEDDED_ES_PORT;
    private String[] credentials = null;
    private String typeNames = null;
    private boolean useEmbeddedES = false;
    private boolean useConnect = true;
    private DatastoreConfig[] datastoreConfigs = new DatastoreConfig[2];


    private boolean validateDatastores(Properties props) {
        boolean datastoresValidated= true;
        datastoreConfigs[0]= new DatastoreConfig(props,"datastores.0.");
        if (!datastoreConfigs[0].validate()) datastoresValidated=false;
        if (useConnect) {
            datastoreConfigs[1]= new DatastoreConfig(props,"datastores.1.");
            if (!datastoreConfigs[1].validate()) datastoresValidated=false;
        }
        return datastoresValidated;
    }

    /**
     * @param props
     * @return true if all the properties for running the application
     *   could be loaded properly. false otherwise.
     */
    public boolean validate(Properties props) {

        return (
            _validate(props,"useConnect",true) &&
            validateDatastores(props) && // must come after useConnect
            _validate(props,"serverPort",true) &&
            _validate(props,"useEmbeddedES", true) &&
            _validate(props,"esPort",(useEmbeddedES)) &&  // must come after useEmbeddedES
            _validate(props,"credentials") &&
            _validate(props,"typeNames")
            );
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

    public String getEsPort() {
        return esPort;
    }

    void setEsPort(String esPort) {
        this.esPort = esPort;
    }

    public boolean isUseEmbeddedES() {
        return useEmbeddedES;
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
}

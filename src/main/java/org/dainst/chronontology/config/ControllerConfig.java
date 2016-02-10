package org.dainst.chronontology.config;

import java.util.Properties;

/**
 * @author Daniel M. de Oliveira
 */
public class ControllerConfig extends Config {

    private DatastoreConfig[] datastoreConfigs = new DatastoreConfig[2];
    private boolean useConnect = true;





    @Override
    public boolean validate(Properties props) {
        return (
            _validate(props,"useConnect",true) &&
            validateDatastores(props)
        );
    }

    private boolean validateDatastores(Properties props) {
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

    void setUseConnect(String useIt) {
        if (useIt.equals("false")) useConnect = false;
    }

    public boolean isUseConnect() {
        return useConnect;
    }

    public DatastoreConfig[] getDatastoreConfigs() {
        return datastoreConfigs;
    }
}

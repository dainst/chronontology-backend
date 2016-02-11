package org.dainst.chronontology.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

/**
 * @author Daniel M. de Oliveira
 */
public class ControllerConfig extends Config {

    static final String MSG_MUST_TYPE_ES= "datastores.0 must be of type \""+
        ConfigConstants.DATASTORE_TYPE_ES+"\".";
    static final String MSG_ES_CLASH= "When both datastores use the same elasticsearch url, the index names must be different.";

    private DatastoreConfig[] datastoreConfigs = new DatastoreConfig[2];
    private boolean useConnect = true;

    @Override
    public boolean validate(Properties props) {
        return (
            _validate(props,"useConnect",true) &
            validateDatastores(props)
        );
    }

    @Override
    public ArrayList<String> getConstraintViolations() {
        ArrayList<String> allViolations= new ArrayList<String>();
        allViolations.addAll(constraintViolations);
        for (DatastoreConfig config: Arrays.asList(datastoreConfigs)) {
            if (config!=null)
                allViolations.addAll(config.getConstraintViolations());
        }
        return allViolations;
    }

    private boolean esConfigsClash() {
        // TODO implement equals
        return ((datastoreConfigs[0].getType().equals(datastoreConfigs[1].getType()))
                && (datastoreConfigs[0].getUrl().equals(datastoreConfigs[1].getUrl()))
                && (datastoreConfigs[0].getIndexName().equals(datastoreConfigs[1].getIndexName())));
    }

    private boolean validateFirstDatastore(Properties props) {
        datastoreConfigs[0]= new DatastoreConfig("0");
        if (!datastoreConfigs[0].validate(props)) return false;

        if (!datastoreConfigs[0].getType().equals(ConfigConstants.DATASTORE_TYPE_ES)) {
            constraintViolations.add(ConfigConstants.MSG_CONSTRAINT_VIOLATION+MSG_MUST_TYPE_ES);
            return false;
        }
        return true;
    }

    private boolean validateSecondDatastore(Properties props) {
        datastoreConfigs[1]= new DatastoreConfig("1");
        return (datastoreConfigs[1].validate(props));
    }



    private boolean validateDatastores(Properties props) {

        if (!validateFirstDatastore(props)) return false;

        if (useConnect) {
            if (!validateSecondDatastore(props)) return false;
            if (esConfigsClash()) {
                constraintViolations.add(ConfigConstants.MSG_CONSTRAINT_VIOLATION+MSG_ES_CLASH);
                return false;
            }
        }
        return true;
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

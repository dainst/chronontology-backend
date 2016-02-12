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

    private Properties props= null;

    @Override
    public boolean validate(Properties props) {
        this.props= props;
        return (
            _validate(props,"useConnect","true")
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

    private void validateFirstDatastore() {

        datastoreConfigs[0]= new DatastoreConfig("0");
        if (!datastoreConfigs[0].validate(props)) throw new ConfigValidationException();

        if (!datastoreConfigs[0].getType().equals(ConfigConstants.DATASTORE_TYPE_ES)) {
            throw new ConfigValidationException(MSG_MUST_TYPE_ES);
        }
    }

    private boolean validateSecondDatastore() {
        datastoreConfigs[1]= new DatastoreConfig("1");
        return (datastoreConfigs[1].validate(props));
    }


    void setUseConnect(String useIt) {

        validateFirstDatastore();

        if (useIt.equals("false")) useConnect = false;
        if (useConnect) {
            if (!validateSecondDatastore()) throw new ConfigValidationException();
            if (esConfigsClash()) {
                throw new ConfigValidationException(MSG_ES_CLASH);
            }
        }
    }

    public boolean isUseConnect() {
        return useConnect;
    }

    public DatastoreConfig[] getDatastoreConfigs() {
        return datastoreConfigs;
    }
}

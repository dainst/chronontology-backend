package org.dainst.chronontology.config;

import org.dainst.chronontology.Constants;

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
    private RightsValidatorConfig rightsValidatorConfig= new RightsValidatorConfig();
    private boolean useConnect = true;

    private Properties props= null;

    @Override
    public boolean validate(Properties props) {
        if (props==null) throw new IllegalArgumentException(Constants.MSG_PROPS_NOT_NULL);
        this.props= props;

        return (
            rightsValidatorConfig.validate(props) &
            _validate(props,"useConnect","true")
        );
    }

    @Override
    public ArrayList<String> getConstraintViolations() {
        ArrayList<String> allViolations= new ArrayList<String>();
        allViolations.addAll(constraintViolations);
        allViolations.addAll(rightsValidatorConfig.getConstraintViolations());
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

    private void validateDatastore(Integer nr) {
        datastoreConfigs[nr]= new DatastoreConfig(nr.toString());
        if (!datastoreConfigs[nr].validate(props)) throw new ConfigValidationException();
    }


    void setUseConnect(String useIt) {

        validateDatastore(0);
        if (!datastoreConfigs[0].getType().equals(ConfigConstants.DATASTORE_TYPE_ES))
            throw new ConfigValidationException(MSG_MUST_TYPE_ES);

        if (!useIt.equals("false")) {
            validateDatastore(1);
            if (esConfigsClash()) throw new ConfigValidationException(MSG_ES_CLASH);
        } else
            useConnect= false;
    }

    public boolean isUseConnect() {
        return useConnect;
    }

    public DatastoreConfig[] getDatastoreConfigs() {
        return datastoreConfigs;
    }

    public RightsValidatorConfig getRightsValidatorConfig() {
        return rightsValidatorConfig;
    }
}

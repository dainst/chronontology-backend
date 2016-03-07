package org.dainst.chronontology.config;

import org.dainst.chronontology.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

/**
 * @author Daniel M. de Oliveira
 */
public class DispatcherConfig extends Config {

    private boolean useConnect = true;

    private Properties props= null;

    private ElasticsearchDatastoreConfig esConfig= null;

    private FilesystemDatastoreConfig fsConfig= null;


    @Override
    public boolean validate(Properties props) {
        if (props==null) throw new IllegalArgumentException(Constants.MSG_PROPS_NOT_NULL);
        this.props= props;

        return (
            _validate(props,"useConnect","true")
        );
    }

    @Override
    public ArrayList<String> getConstraintViolations() {
        ArrayList<String> allViolations= new ArrayList<String>();
        allViolations.addAll(constraintViolations);
        allViolations.addAll(esConfig.getConstraintViolations());
        if (fsConfig!=null) allViolations.addAll(fsConfig.getConstraintViolations());
        return allViolations;
    }

    void setUseConnect(String useIt) {

        esConfig= new ElasticsearchDatastoreConfig();
        if (!esConfig.validate(props)) throw new ConfigValidationException();

        if (!useIt.equals("false")) {

            fsConfig= new FilesystemDatastoreConfig();
            if (!fsConfig.validate(props)) throw new ConfigValidationException();
        } else
            useConnect= false;
    }

    public boolean isUseConnect() {
        return useConnect;
    }

    public FilesystemDatastoreConfig getFilesystemDatastoreConfig() {
        return fsConfig;
    }

    public ElasticsearchDatastoreConfig getElasticsearchDatastoreConfig() {
        return esConfig;
    }

}

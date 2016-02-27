package org.dainst.chronontology.config;

import org.dainst.chronontology.controller.RightsValidator;

import java.util.*;

/**
 * @author Daniel M. de Oliveira
 */
public class RightsValidatorConfig extends Config {

    public RightsValidatorConfig() {
        this.prefix= "dataset.";
    }

    //  <datasetName<user,permissionGrade>>
    Map<String,Map<String,String>> datasetRules= new HashMap<String,Map<String,String>>();

    private List<String> datasets(Properties props) {
        List<String> datasets= new ArrayList<String>();
        for (Enumeration<?> e = props.propertyNames(); e.hasMoreElements(); ) {
            String name = (String)e.nextElement();
            if (name.startsWith(this.prefix)) {
                if (!datasets.contains(datasetName(name)))
                    datasets.add(datasetName(name));
            }
        }
        return datasets;
    }

    private String datasetName(String propertyName) {
        String nameWithoutPrefix= propertyName.replace(this.prefix,"");
        return nameWithoutPrefix.substring(0,nameWithoutPrefix.indexOf("."));
    }

    @Override
    public boolean validate(Properties props) {

        for (String datasetName:datasets(props)) {

            for (Enumeration<?> e = props.propertyNames(); e.hasMoreElements(); ) {
                String name = (String)e.nextElement();
                if (name.startsWith(this.prefix+datasetName+".editor")) {
                    List<String> editors= Arrays.asList(props.getProperty(name).split(","));
                    Map<String,String> userRules= new HashMap<String,String>();
                    for (String editor: editors) {
                        userRules.put(editor,"editor");
                    }
                    datasetRules.put(datasetName(name),userRules);
                }
            }
        }

        return true;
    }

    public Map<String,Map<String,String>> getEditorRules() {
        return datasetRules;
    }
}

package org.dainst.chronontology.config;

import org.apache.lucene.util.fst.UpToTwoPositiveIntOutputs;

import java.util.*;

/**
 * @author Daniel M. de Oliveira
 */
public class RightsValidatorConfig extends Config {

    private UpToTwoPositiveIntOutputs readerRules;

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

            Map<String,String> userRules= new HashMap<String,String>();


            List list = Collections.list(props.propertyNames());
            Collections.sort(list);

            for (Object propertyName: list ) {
                String name = (String) propertyName;
                if (!name.startsWith(this.prefix+datasetName+".")) continue;




                if (name.equals(this.prefix+datasetName+".editor")) {

                    List<String> editors= Arrays.asList(props.getProperty(name).split(","));
                    for (String editor: editors) {
                        userRules.put(editor,"editor");
                    }

                }
                else if (name.equals(this.prefix+datasetName+".reader")) {
                    List<String> readers= Arrays.asList(props.getProperty(name).split(","));
                    for (String reader: readers) {
                        if (userRules.get(reader) == null || (
                                !userRules.get(reader).equals("editor"))) {
                            userRules.put(reader,"reader");
                        }
                    }

                } else {
                    constraintViolations.add("Permission level unkown in \""+name+"\".");
                }
            }

            if (!userRules.isEmpty()) {
                datasetRules.put(datasetName,userRules);
            }
        }
        return (constraintViolations.size()==0);
    }

    public Map<String,Map<String,String>> getRules() {
        return datasetRules;
    }
}

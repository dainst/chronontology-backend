package org.dainst.chronontology.config;

import org.dainst.chronontology.Constants;

import java.util.*;

/**
 * @author Daniel M. de Oliveira
 */
public class RightsValidatorConfig extends Config {

    public static final String DATASET= "dataset";
    public static final String DATASET_NAME_NONE = "none";

    public static final String MSG_ANONYMOUS_EDITORS_NOT_ALLOWED=
            "Anonymous editor access to datasets not supported.";
    public static final String MSG_PERMISSION_LEVEL_UNKNOWN=
            "Permission level unkown in \"PROPERTY\".";
    public static final String MSG_NONE_RESERVED_TERM=
            "The term \"none\" is a reserved term an cannot be used as a dataset name.";

    public static final String PERMISSION_LEVEL_EDITOR = "editor";
    public static final String PERMISSION_LEVEL_READER = "reader";


    public RightsValidatorConfig() {
        this.prefix= DATASET+".";
    }

    //  <datasetName<user,permissionGrade>>
    Map<String,Map<String,String>> datasetRules= new HashMap<String,Map<String,String>>();


    @Override
    public boolean validate(Properties props) {

        for (String dataset:datasets(props)) {
            Map<String, String> userRules = getUserRulesFor(dataset,props);

            if (!userRules.isEmpty()) {
                datasetRules.put(dataset,userRules);
            }
        }
        return (constraintViolations.size()==0);
    }

    private void addToConstraintViolations(String violationMsg) {
        constraintViolations.add(ConfigConstants.MSG_CONSTRAINT_VIOLATION+violationMsg);
    }

    /**
     * Side effect: can add entries to contraintViolations.
     *
     * @param dataset
     * @param props all properties, whether relevant or not to {@link RightsValidatorConfig}.
     * @return
     */
    private Map<String, String> getUserRulesFor(String dataset,Properties props) {
        Map<String,String> userRules= new HashMap<String,String>();

        List list = Collections.list(props.propertyNames());
        Collections.sort(list);
        for (Object propertyName: list ) {
            if (!((String) propertyName).startsWith(this.prefix+dataset+".")) continue;

            evaluateProperty(props, userRules,(String) propertyName);
        }
        return userRules;
    }


    /**
     * Evaluates a single line of a <code>props</code>.
     *
     * @param props all properties, whether relevant or not to {@link RightsValidatorConfig}.
     * @param userRules is affected. One new userRule is put if the propertyName
     *   can get evaluated successfully.
     * @param propertyName full name of the property line to be evaluated
     */
    private void evaluateProperty(
            Properties props,
            Map<String, String> userRules,
            String propertyName) {

        if (propertyName.contains(DATASET_NAME_NONE))
            addToConstraintViolations(MSG_NONE_RESERVED_TERM);

        if (propertyName.endsWith(PERMISSION_LEVEL_EDITOR)) {

            for (String editor: props.getProperty(propertyName).split(",")) {

                if (editor.equals(Constants.USER_NAME_ANONYMOUS))
                    addToConstraintViolations(MSG_ANONYMOUS_EDITORS_NOT_ALLOWED);
                else
                  userRules.put(editor, PERMISSION_LEVEL_EDITOR);
            }

        }
        else if (propertyName.endsWith(PERMISSION_LEVEL_READER)) {

            for (String reader: props.getProperty(propertyName).split(",")) {

                if (isNotAlreadyEditor(reader,userRules)) {
                    userRules.put(reader, PERMISSION_LEVEL_READER);
                }
            }

        } else {

            addToConstraintViolations(MSG_PERMISSION_LEVEL_UNKNOWN.replace("PROPERTY",propertyName));
        }
    }


    private boolean isNotAlreadyEditor(String user,Map<String, String> userRules) {
        return (userRules.get(user) == null || (
                !userRules.get(user).equals(PERMISSION_LEVEL_EDITOR)));
    }


    /**
     * @param props all properties, whether relevant or not to {@link RightsValidatorConfig}.
     * @return list of all dataset names mentioned in props.
     */
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

    public Map<String,Map<String,String>> getRules() {
        return datasetRules;
    }
}

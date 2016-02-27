package org.dainst.chronontology.controller;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel M. de Oliveira
 */
public class RightsValidator {

    Map<String,Map<String,String>> rules = new HashMap<String,Map<String,String>>();

    public void setRules(Map<String,Map<String,String>> editorRules) {
        this.rules = editorRules;
    }

    public boolean hasEditorPermission(String userName, String dataset) {

        return (userName.equals("admin")

                || ((rules.get(dataset)!=null) &&
                (rules.get(dataset).get(userName)!=null) &&
                (rules.get(dataset).get(userName).equals("editor"))));
    }

    public boolean hasReaderPermission(String userName, String dataset) {

        return (userName.equals("admin")
                || ((rules.get(dataset)!=null) &&
                        (rules.get(dataset).get(userName)!=null) &&
                        (rules.get(dataset).get(userName).equals("reader")))
                || ((rules.get(dataset)!=null) &&
                        (rules.get(dataset).get(userName)!=null) &&
                        (rules.get(dataset).get(userName).equals("editor"))));
    }
}

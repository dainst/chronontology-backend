package org.dainst.chronontology.controller;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel M. de Oliveira
 */
public class RightsValidator {

    public enum Rights {
        EDITOR,
        READER
    }

    Map<String,Map<String,String>> rules = new HashMap<String,Map<String,String>>();

    public void setRules(Map<String,Map<String,String>> rules) {
        this.rules = rules;
    }

    public boolean hasPermission(String userName, String dataset, Rights rights) {

        if (rights.equals(Rights.EDITOR))
            return (userName.equals("admin")

                    || ((rules.get(dataset)!=null) &&
                    (rules.get(dataset).get(userName)!=null) &&
                    (rules.get(dataset).get(userName).equals("editor"))));

        if (rights.equals(Rights.READER))
            return (userName.equals("admin")
                    || ((rules.get(dataset)!=null) &&
                    (rules.get(dataset).get(userName)!=null) &&
                    (rules.get(dataset).get(userName).equals("reader")))
                    || ((rules.get(dataset)!=null) &&
                    (rules.get(dataset).get(userName)!=null) &&
                    (rules.get(dataset).get(userName).equals("editor"))));

        return false;
    }
}

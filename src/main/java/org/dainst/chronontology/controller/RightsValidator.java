package org.dainst.chronontology.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel M. de Oliveira
 */
public class RightsValidator {

    Map<String,Map<String,String>> editorRules= new HashMap<String,Map<String,String>>();

    public void setEditorRules(Map<String,Map<String,String>> editorRules) {
        this.editorRules= editorRules;
    }

    public boolean hasEditorPermission(String userName, String dataset) {

        return (userName.equals("admin")||

                ((editorRules.get(dataset)!=null) &&
                (editorRules.get(dataset).get(userName)!=null) &&
                (editorRules.get(dataset).get(userName).equals("editor"))));
    }
}

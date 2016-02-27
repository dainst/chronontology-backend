package org.dainst.chronontology.controller;

/**
 * @author Daniel M. de Oliveira
 */
public class RightsValidator {

    public boolean hasPermission(String userName,String dataset) {

        if (userName.equals("admin")||(userName.equals("ove")&&dataset.equals("dataset1"))) {
            return true;
        }
        return false;
    }
}

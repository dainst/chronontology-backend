package org.dainst.chronontology.handler.model;

import org.dainst.chronontology.handler.model.RightsValidator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;

/**
 * @author Daniel M. de Oliveira
 */
public class RightsValidatorTest {

    private Map<String,Map<String,String>> rules;
    private Map<String,String> usersRules;
    RightsValidator validator;

    @BeforeMethod
    public void before() {
        rules = new HashMap<String,Map<String,String>>();
        usersRules= new HashMap<String,String>();
        rules.put("dataset1",usersRules);
        validator = new RightsValidator();
        validator.setRules(rules);

    }

    @Test
    public void nonSpecifiedAuthenticatedUserHasAccess() {
        usersRules.put("anonymous","reader");
        assertTrue(validator.hasPermission("karl","dataset1", RightsValidator.Operation.READ));
    }

    @Test
    public void specifiedAuthenticatedUserHasAccessWithAnonymousAlsoConfigured() {
        usersRules.put("anonymous","reader");
        usersRules.put("karl","reader");
        assertTrue(validator.hasPermission("karl","dataset1", RightsValidator.Operation.READ));
    }

    @Test
    public void specifiedAuthenticatedEditorUserHasAccessWithAnonymousAlsoConfigured() {
        usersRules.put("anonymous","reader");
        usersRules.put("karl","editor");
        assertTrue(validator.hasPermission("karl","dataset1", RightsValidator.Operation.READ));
    }


    @Test
    public void specifiedAuthenticatedUserHasAccess() {
        usersRules.put("karl","reader");
        assertTrue(validator.hasPermission("karl","dataset1", RightsValidator.Operation.READ));
    }

    @Test
    public void specifiedAuthenticatedEditorUserHasAccess() {
        usersRules.put("karl","editor");
        assertTrue(validator.hasPermission("karl","dataset1", RightsValidator.Operation.READ));
    }

}

package org.dainst.chronontology.config;

import org.dainst.chronontology.TestConstants;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Properties;

import static org.testng.Assert.assertEquals;

/**
 * @author Daniel M. de Oliveira
 */
public class RightsValidatorConfigTest {

    private RightsValidatorConfig config;
    private Properties props= null;

    @BeforeMethod
    public void before() {
        props= new Properties();
        config= new RightsValidatorConfig();
    }

    @Test
    public void one() {
        props.put("dataset.dataset1.editor", TestConstants.USER_NAME_1);

        config.validate(props);
        assertEquals(config.getEditorRules().get("dataset1").get(TestConstants.USER_NAME_1),
                "editor");
    }

    @Test
    public void two() {
        props.put("dataset.dataset1.editor", TestConstants.USER_NAME_1+","+TestConstants.USER_NAME_2);

        config.validate(props);
        assertEquals(config.getEditorRules().get("dataset1").get(TestConstants.USER_NAME_1), "editor");
        assertEquals(config.getEditorRules().get("dataset1").get(TestConstants.USER_NAME_2), "editor");
    }

    @Test
    public void ignore() {
        props.put("dataset.dataset1.reader", TestConstants.USER_NAME_1);

        config.validate(props);
        assertEquals(config.getEditorRules().get("dataset1"), null);
    }
}

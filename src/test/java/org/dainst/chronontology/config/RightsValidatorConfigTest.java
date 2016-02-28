package org.dainst.chronontology.config;

import org.dainst.chronontology.Constants;
import org.dainst.chronontology.TestConstants;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

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
        assertEquals(config.getRules().get("dataset1").get(TestConstants.USER_NAME_1),
                "editor");
    }

    @Test
    public void two() {
        props.put("dataset.dataset1.editor", TestConstants.USER_NAME_1+","+TestConstants.USER_NAME_2);

        config.validate(props);
        assertEquals(config.getRules().get("dataset1").get(TestConstants.USER_NAME_1), "editor");
        assertEquals(config.getRules().get("dataset1").get(TestConstants.USER_NAME_2), "editor");
    }

    @Test
    public void oneReader() {
        props.put("dataset.dataset1.reader", TestConstants.USER_NAME_1);

        config.validate(props);
        assertEquals(config.getRules().get("dataset1").get(TestConstants.USER_NAME_1), "reader");
    }

    @Test
    public void twoReaders() {
        props.put("dataset.dataset1.reader", TestConstants.USER_NAME_1+","+TestConstants.USER_NAME_2);

        config.validate(props);
        assertEquals(config.getRules().get("dataset1").get(TestConstants.USER_NAME_1), "reader");
        assertEquals(config.getRules().get("dataset1").get(TestConstants.USER_NAME_2), "reader");
    }

    @Test
    public void editorOverwritesReader() {
        props.put("dataset.dataset1.reader", TestConstants.USER_NAME_1);
        props.put("dataset.dataset1.editor", TestConstants.USER_NAME_1);

        config.validate(props);
        assertEquals(config.getRules().get("dataset1").get(TestConstants.USER_NAME_1), "editor");
    }

    @Test
    public void editorOverwritesReaderDifferentOrder() {
        props.put("dataset.dataset1.editor", TestConstants.USER_NAME_1);
        props.put("dataset.dataset1.reader", TestConstants.USER_NAME_1);

        config.validate(props);
        assertEquals(config.getRules().get("dataset1").get(TestConstants.USER_NAME_1), "editor");
    }

    @Test
    public void oneReaderOneWriter() {
        props.put("dataset.dataset1.editor", TestConstants.USER_NAME_1);
        props.put("dataset.dataset1.reader", TestConstants.USER_NAME_2);

        config.validate(props);
        assertEquals(config.getRules().get("dataset1").get(TestConstants.USER_NAME_1), "editor");
        assertEquals(config.getRules().get("dataset1").get(TestConstants.USER_NAME_2), "reader");
    }

    @Test
    public void levelUnknown() {
        props.put("dataset.dataset1.unkown", TestConstants.USER_NAME_1);

        assertFalse(config.validate(props));
    }

    @Test
    public void anonymousEditorNotAllowed() {
        props.put("dataset.dataset1.editor", Constants.USER_NAME_ANONYMOUS);

        assertFalse(config.validate(props));
        assertEquals(config.getConstraintViolations().get(0),
                ConfigConstants.MSG_CONSTRAINT_VIOLATION+RightsValidatorConfig.MSG_ANONYMOUS_EDITORS_NOT_ALLOWED);
    }
}

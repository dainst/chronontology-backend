package org.dainst.chronontology.config;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import static org.dainst.chronontology.Constants.*;
import static org.dainst.chronontology.TestConstants.*;

/**
 * @author Daniel M. de Oliveira
 */
public class RightsValidatorConfigTest {

    private RightsValidatorConfig config;
    private Properties props= null;

    static String datasetProperty(String permissionLevel) {
        return RightsValidatorConfig.DATASET+"."+dataset+"."+permissionLevel;
    }
    static final String dataset= "dataset1";


    @BeforeMethod
    public void before() {
        props= new Properties();
        config= new RightsValidatorConfig();
    }

    @Test
    public void one() {
        props.put(datasetProperty(PERMISSION_LEVEL_EDITOR), USER_NAME_1);

        config.validate(props);
        assertEquals(config.getRules().get(dataset).get(USER_NAME_1),
                PERMISSION_LEVEL_EDITOR);
    }

    @Test
    public void two() {
        props.put(datasetProperty(PERMISSION_LEVEL_EDITOR), USER_NAME_1+","+USER_NAME_2);

        config.validate(props);
        assertEquals(config.getRules().get(dataset).get(USER_NAME_1),PERMISSION_LEVEL_EDITOR);
        assertEquals(config.getRules().get(dataset).get(USER_NAME_2),PERMISSION_LEVEL_EDITOR);
    }

    @Test
    public void oneReader() {
        props.put(datasetProperty(PERMISSION_LEVEL_READER), USER_NAME_1);

        config.validate(props);
        assertEquals(config.getRules().get(dataset).get(USER_NAME_1),PERMISSION_LEVEL_READER);
    }

    @Test
    public void twoReaders() {
        props.put(datasetProperty(PERMISSION_LEVEL_READER), USER_NAME_1+","+USER_NAME_2);

        config.validate(props);
        assertEquals(config.getRules().get(dataset).get(USER_NAME_1),PERMISSION_LEVEL_READER);
        assertEquals(config.getRules().get(dataset).get(USER_NAME_2),PERMISSION_LEVEL_READER);
    }

    @Test
    public void editorOverwritesReader() {
        props.put(datasetProperty(PERMISSION_LEVEL_READER), USER_NAME_1);
        props.put(datasetProperty(PERMISSION_LEVEL_EDITOR), USER_NAME_1);

        config.validate(props);
        assertEquals(config.getRules().get(dataset).get(USER_NAME_1),PERMISSION_LEVEL_EDITOR);
    }

    @Test
    public void editorOverwritesReaderDifferentOrder() {
        props.put(datasetProperty(PERMISSION_LEVEL_EDITOR), USER_NAME_1);
        props.put(datasetProperty(PERMISSION_LEVEL_READER), USER_NAME_1);

        config.validate(props);
        assertEquals(config.getRules().get(dataset).get(USER_NAME_1),PERMISSION_LEVEL_EDITOR);
    }

    @Test
    public void oneReaderOneWriter() {
        props.put(datasetProperty(PERMISSION_LEVEL_EDITOR), USER_NAME_1);
        props.put(datasetProperty(PERMISSION_LEVEL_READER), USER_NAME_2);

        config.validate(props);
        assertEquals(config.getRules().get(dataset).get(USER_NAME_1),PERMISSION_LEVEL_EDITOR);
        assertEquals(config.getRules().get(dataset).get(USER_NAME_2),PERMISSION_LEVEL_READER);
    }

    @Test
    public void levelUnknown() {
        props.put(datasetProperty("unknown"), USER_NAME_1);

        assertFalse(config.validate(props));
    }

    @Test
    public void anonymousEditorNotAllowed() {
        props.put(datasetProperty(PERMISSION_LEVEL_EDITOR),USER_NAME_ANONYMOUS);

        assertFalse(config.validate(props));
        assertEquals(config.getConstraintViolations().get(0),
                ConfigConstants.MSG_CONSTRAINT_VIOLATION+RightsValidatorConfig.MSG_ANONYMOUS_EDITORS_NOT_ALLOWED);
    }
}

package org.dainst.chronontology.config;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Daniel M. de Oliveira
 */
public class ConfigTest {

    private class WorkingConfig extends Config {
        private String test = null;
        @Override
        public boolean validate(Properties props) {
            return _validate(props,"test");
        }

        @Override public ArrayList<String> getConstraintViolations() { return null; }

        void setTest(String test) {
            this.test= test;
        }
        public String getTest() {
            return test;
        }
    }

    private class ExceptionConfig extends Config {
        @Override
        public boolean validate(Properties props) {
            return _validate(props,"test");
        }

        @Override public ArrayList<String> getConstraintViolations() { return constraintViolations; }

        void setTest(String test) {
            if (test.equals("badvalue"))
                throw new ConfigValidationException(test);
        }
    }

    private class ExceptionConfigWithBasicConstructorForException extends Config {
        @Override
        public boolean validate(Properties props) {
            return _validate(props,"test","abc");
            // make sure to use default value constructor to not have false positive
            // by setTest not beeing called.
        }

        @Override public ArrayList<String> getConstraintViolations() { return constraintViolations; }

        void setTest(String test) {
            throw new ConfigValidationException();
        }
    }

    private class MissingPropertyConfig extends Config {
        @Override
        public boolean validate(Properties props) {
            return _validate(props,"test");
        }

        @Override public ArrayList<String> getConstraintViolations() { return null; }

        void setTest(String test) {}
    }

    @Test
    public void ok() {
        WorkingConfig config= new WorkingConfig();
        Properties props= new Properties();
        String value = "abc";
        props.setProperty("test", value);
        assertTrue(config.validate(props));
        assertEquals(config.getTest(), value);
    }

    /**
     * When a validation fails due to a violated constraint.
     */
    @Test
    public void validationFailed() {
        ExceptionConfig config= new ExceptionConfig();
        Properties props= new Properties();
        props.setProperty("test", "badvalue");
        assertFalse(config.validate(props));
        assertEquals(config.getConstraintViolations().get(0),
                ConfigConstants.MSG_CONSTRAINT_VIOLATION+"badvalue");
    }

    @Test
    public void missingPropertyFail() {
        MissingPropertyConfig config= new MissingPropertyConfig();
        Properties props= new Properties();
        assertFalse(config.validate(props));
    }

    @Test
    public void mustFailWithWithBasicConstructorForException() {
        ExceptionConfigWithBasicConstructorForException config=
                new ExceptionConfigWithBasicConstructorForException();
        Properties props= new Properties();
        assertTrue(config.validate(props));
    }
}

package org.dainst.chronontology.config;

import org.testng.annotations.Test;

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
        void setTest(String test) {
            if (test.equals("badvalue"))
                throw new ConfigValidationException(test);
        }
    }

    private class MissingPropertyConfig extends Config {
        @Override
        public boolean validate(Properties props) {
            return _validate(props,"test");
        }
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
    }

    @Test
    public void missingPropertyFail() {
        MissingPropertyConfig config= new MissingPropertyConfig();
        Properties props= new Properties();
        assertFalse(config.validate(props));
    }
}

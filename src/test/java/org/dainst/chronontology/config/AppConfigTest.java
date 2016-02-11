package org.dainst.chronontology.config;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Properties;

import static org.testng.Assert.*;

/**
 * @author Daniel M. de Oliveira
 */
public class AppConfigTest {

    private AppConfig appConfig;
    private Properties props= null;

    @BeforeMethod
    public void before() {
        props= new Properties();
        appConfig= new AppConfig();
    }

    @Test
    public void basic() {
        props.put("serverPort","4567");
        props.put("typeNames","period");
        props.put("credentials","abc:def");

        appConfig.validate(props);
        assertEquals(appConfig.getServerPort(),"4567");
        assertEquals(appConfig.getCredentials()[0],"abc:def");
    }

    /**
     * required: typeNames
     */
    @Test
    public void missingRequiredProperty() {
        props.put("credentials","abc:def");

        assertFalse(appConfig.validate(props));
    }

    @Test
    public void useEmbeddedES() {
        props.put("typeNames","period");
        props.put("credentials","abc:def");
        props.put("useEmbeddedES","true");

        appConfig.validate(props);
        assertNotNull(appConfig.getElasticsearchServerConfig());
    }

    @Test
    public void dontUseEmbeddedESBySettingParamFalse() {
        props.put("useEmbeddedES","false");
        props.put("typeNames","period");
        props.put("credentials","abc:def");

        appConfig.validate(props);
        assertNull(appConfig.getElasticsearchServerConfig());
    }

    /**
     * Omitting param is supported for backward compatibility.
     */
    @Test
    public void dontUseEmbeddedESByOmittingParam() {
        props.put("typeNames","period");
        props.put("credentials","abc:def");
        props.put("useConnect","false");

        assertTrue(appConfig.validate(props));
        assertNull(appConfig.getElasticsearchServerConfig());
    }

    @Test
    public void allowOmitServerPort() {
        props.put("typeNames","period");
        props.put("credentials","abc:def");
        props.put("useEmbeddedES","true");
        props.put("useConnect","false");

        assertTrue(appConfig.validate(props));
        assertEquals(appConfig.getServerPort(), ConfigConstants.SERVER_PORT);
    }


    @Test
    public void elasticSearchServerConfig() {
        props.put("typeNames","period");
        props.put("credentials","abc:def");
        props.put("useEmbeddedES","true");
        props.put("useConnect","false");

        assertTrue(appConfig.validate(props));
        assertEquals(appConfig.getElasticsearchServerConfig().getPort(), ConfigConstants.EMBEDDED_ES_PORT);
    }



    @Test
    public void controller() {
        props.put("typeNames","period");
        props.put("credentials","abc:def");
        props.put("useEmbeddedES","true");
        props.put("useConnect","true");
        props.put("datastores.1.type","filesystem");

        assertTrue(appConfig.validate(props));
        assertEquals(appConfig.getControllerConfig().isUseConnect(),true);
    }

    @Test
    public void serverPortNotANumber() {
        props.put("serverPort","a7");
        props.put("typeNames","period");
        props.put("credentials","abc:def");

        assertFalse(appConfig.validate(props));
        assertTrue(appConfig.getConstraintViolations().get(0).contains(
                ConfigConstants.MSG_CONSTRAINT_VIOLATION+AppConfig.MSG_SERVER_PORT_NAN));
    }

    @Test
    public void datastore0isNotES_single() {
        props.put("useConnect","false");
        props.put("datastores.0.type",ConfigConstants.DATASTORE_TYPE_FS);

        assertFalse(appConfig.validate(props));
        assertTrue(appConfig.getConstraintViolations().contains(
                ConfigConstants.MSG_CONSTRAINT_VIOLATION+ControllerConfig.MSG_MUST_TYPE_ES));
    }

    @Test
    public void datastore0isNotES_connect() {
        props.put("datastores.0.type","filesystem");

        assertFalse(appConfig.validate(props));
        assertTrue(appConfig.getConstraintViolations().contains(
                ConfigConstants.MSG_CONSTRAINT_VIOLATION+ControllerConfig.MSG_MUST_TYPE_ES));
    }

    @Test
    public void mergeMessagesWithControllerMessages() {
        props.put("serverPort","a7");
        props.put("typeNames","period");
        props.put("credentials","abc:def");
        props.put("datastores.0.type","filesystem");

        assertFalse(appConfig.validate(props));
        assertTrue(appConfig.getConstraintViolations().contains(
                ConfigConstants.MSG_CONSTRAINT_VIOLATION+AppConfig.MSG_SERVER_PORT_NAN+"\"a7\"."));
        assertTrue(appConfig.getConstraintViolations().contains(
                ConfigConstants.MSG_CONSTRAINT_VIOLATION+ControllerConfig.MSG_MUST_TYPE_ES));
    }
}

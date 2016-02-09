package org.dainst.chronontology.config;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Daniel M. de Oliveira
 */
public class ElasticsearchServerConfigTest extends ConfigTestBase {

    Properties props = null;

    @BeforeMethod
    public void before() {
        props= new Properties();
    }

    @Test
    public void specifyPort() {
        props.put("esServer.port","9200");

        ElasticsearchServerConfig config= new ElasticsearchServerConfig();
        assertTrue(config.validate(props));
        assertEquals(config.getPort(),"9200");
    }

    @Test
    public void omitPort() {

        ElasticsearchServerConfig config= new ElasticsearchServerConfig();
        assertTrue(config.validate(props));
        assertEquals(config.getPort(), ConfigConstants.EMBEDDED_ES_PORT);
    }
}

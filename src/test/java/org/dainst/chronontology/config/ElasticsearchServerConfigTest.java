package org.dainst.chronontology.config;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Daniel M. de Oliveira
 */
public class ElasticsearchServerConfigTest extends ConfigTestBase {

    @Test
    public void specifyPort() {
        ElasticsearchServerConfig config= new ElasticsearchServerConfig();
        assertTrue(config.validate(props("70")));
        assertEquals(config.getPort(),"9200");
    }

    @Test
    public void omitPort() {
        ElasticsearchServerConfig config= new ElasticsearchServerConfig();
        assertTrue(config.validate(props("71")));
        assertEquals(config.getPort(), ConfigConstants.EMBEDDED_ES_PORT);
    }
}

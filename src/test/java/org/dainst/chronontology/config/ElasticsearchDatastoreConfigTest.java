package org.dainst.chronontology.config;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Daniel M. de Oliveira
 */
public class ElasticsearchDatastoreConfigTest {

    Properties props = null;

    @BeforeMethod
    public void before() {
        props= new Properties();
    }


    @Test
    public void test() {
        props.put("datastore.elasticsearch.indexName",ConfigConstants.ES_INDEX_NAME);
        props.put("datastore.elasticsearch.url","http://localhost:9200");

        ElasticsearchDatastoreConfig config= new ElasticsearchDatastoreConfig();

        assertTrue(config.validate(props));
        assertEquals(config.getIndexName(),ConfigConstants.ES_INDEX_NAME);
        assertEquals(config.getUrl(),"http://localhost:9200");
    }
}

package org.dainst.chronontology.store;

import org.apache.commons.io.FileUtils;
import org.dainst.chronontology.TestConstants;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;

import java.io.File;
import java.io.IOException;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * @author Daniel M. de Oliveira
 */
public class ESServerTestUtil {

    private static Node esNode = null;
    private static final String HTTP_PORT = "9201";
    private static final String ES_URL= "http://localhost:"+HTTP_PORT;
    private static final String TEST_ES_DATA = TestConstants.TEST_FOLDER+"/test_es_data";
    private static final String TEST_ES_HOME = TestConstants.TEST_FOLDER+"/test_es_home";

    public static String getUrl() {
        return ES_URL;
    }

    public static void startElasticSearchServer() {

        Settings.Builder elasticsearchSettings = Settings.settingsBuilder()
                .put("cluster.name", "chronontology_connected_unit_testing_cluster")
                .put("http.port",HTTP_PORT)
                .put("path.data", TEST_ES_DATA)
                .put("path.home", TEST_ES_HOME);

        esNode = nodeBuilder()
                .local(true)
                .settings(elasticsearchSettings.build())
                .node();
    }

    public static void stopElasticSearchServer() {
        if (esNode==null) return;
        esNode.close();
        esNode=null;
        try {
            FileUtils.deleteDirectory(new File("src/test/resources/test_es_data"));
        } catch (IOException e) { }
    }
}

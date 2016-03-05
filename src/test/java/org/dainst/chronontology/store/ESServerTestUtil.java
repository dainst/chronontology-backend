package org.dainst.chronontology.store;

import org.apache.commons.io.FileUtils;
import org.dainst.chronontology.TestConstants;
import org.elasticsearch.common.settings.ImmutableSettings;
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

    public static String getUrl() {
        return ES_URL;
    }

    public static void startElasticSearchServer() {

        ImmutableSettings.Builder elasticsearchSettings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", "chronontology_connected_unit_testing_cluster")
                .put("http.port",HTTP_PORT)
                .put("path.data", TEST_ES_DATA);

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

package org.dainst.chronontology.extra;

import org.dainst.chronontology.Constants;
import org.elasticsearch.common.settings.ImmutableSettings;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * @author Daniel M. de Oliveira
 */
public class EmbeddedES {

    public static final String DATA_PATH = "embedded_es_data";
    public static final String CLUSTER_NAME = "chronontology_connected_embedded_es";

    public EmbeddedES(String port) {

        ImmutableSettings.Builder elasticsearchSettings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", CLUSTER_NAME)
                .put("http.port", port)
                .put("path.data", DATA_PATH);

        nodeBuilder()
                .local(true)
                .settings(elasticsearchSettings.build())
                .node();
    }
}

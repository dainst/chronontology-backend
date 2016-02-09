package org.dainst.chronontology.extra;

import org.dainst.chronontology.Constants;
import org.dainst.chronontology.config.ElasticsearchServerConfig;
import org.elasticsearch.common.settings.ImmutableSettings;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * @author Daniel M. de Oliveira
 */
public class EmbeddedES {



    public EmbeddedES(ElasticsearchServerConfig config) {

        ImmutableSettings.Builder elasticsearchSettings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", config.getClusterName())
                .put("http.port", config.getPort())
                .put("path.data", config.getDataPath());

        nodeBuilder()
                .local(true)
                .settings(elasticsearchSettings.build())
                .node();
    }
}

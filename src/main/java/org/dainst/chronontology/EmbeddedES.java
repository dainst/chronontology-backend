package org.dainst.chronontology;

import org.dainst.chronontology.config.ElasticsearchServerConfig;
import org.elasticsearch.common.settings.Settings;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * @author Daniel M. de Oliveira
 */
public class EmbeddedES {



    public EmbeddedES(ElasticsearchServerConfig config) {

        Settings.Builder elasticsearchSettings = Settings.settingsBuilder()
                .put("cluster.name", config.getClusterName())
                .put("http.port", config.getPort())
                .put("path.data", config.getDataPath())
                .put("path.home", config.getHomePath())
                .put("mapper.allow_dots_in_name", true);

        nodeBuilder()
                .local(true)
                .settings(elasticsearchSettings.build())
                .node();
    }
}

package org.dainst;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

/**
 * Configures an elasticsearch client
 * for usage in other classes.
 *
 * @author: Daniel M. de Oliveira
 */
public class ESClientUtil {

    Integer port = 9300;
    TransportClient client = null;

    // hide default constructor
    private ESClientUtil(){}

    /**
     * @param clusterName
     * @param hostName
     */
    public ESClientUtil(
            String clusterName,
            String hostName
    ) {
        Settings settings = ImmutableSettings.settingsBuilder().
                put("cluster.name", clusterName).build();

        client = new TransportClient(settings);
        client.addTransportAddress(new InetSocketTransportAddress(hostName,port));
    }

    /**
     * @return the fully configured elastic search transport client. Ready to use.
     */
    public TransportClient getClient() {
        return client;
    }
}


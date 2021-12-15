package org.dainst.chronontology.config;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dainst.chronontology.App;
import org.dainst.chronontology.Controller;
import org.dainst.chronontology.EmbeddedES;

import static spark.Spark.port;

/**
 * @author Daniel M. de Oliveira
 */
public class AppConfigurator implements Configurator<App,AppConfig> {

    final static Logger logger = LogManager.getLogger(AppConfigurator.class);

    @Override
    public App configure(AppConfig config) {

        if (config.getElasticsearchServerConfig()!=null) new EmbeddedES(
                config.getElasticsearchServerConfig());

        final int serverPort= Integer.parseInt(config.getServerPort());
        port(serverPort);

        Controller controller = new Controller(
                new DispatcherConfigurator().configure(config.getDispatcherConfig()),
                getTypes(config.getTypeNames()),
                config.getCredentials(),
                new RightsValidatorConfigurator().configure(config.getRightsValidatorConfig()),
                config.getSPASupport());

        return new App(controller);
    }

    private String[] getTypes(final String typesString) {
        return typesString.split(",");
    }
}

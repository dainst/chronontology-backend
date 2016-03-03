package org.dainst.chronontology.config;

import org.apache.log4j.Logger;
import org.dainst.chronontology.App;
import org.dainst.chronontology.controller.Controller;
import org.dainst.chronontology.extra.EmbeddedES;

import static spark.Spark.port;

/**
 * @author Daniel M. de Oliveira
 */
public class AppConfigurator implements Configurator<App,AppConfig> {

    final static Logger logger = Logger.getLogger(AppConfigurator.class);

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
                new RightsValidatorConfigurator().configure(config.getRightsValidatorConfig()));

        return new App(controller);
    }

    private String[] getTypes(final String typesString) {
        return typesString.split(",");
    }
}

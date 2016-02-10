package org.dainst.chronontology.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.dainst.chronontology.App;
import org.dainst.chronontology.Router;
import org.dainst.chronontology.controller.DocumentModel;
import org.dainst.chronontology.extra.EmbeddedES;

import static spark.Spark.port;

/**
 * @author Daniel M. de Oliveira
 */
public class AppConfigurator implements Configurator<App> {

    final static Logger logger = Logger.getLogger(AppConfigurator.class);

    @Override
    public App configure(Config config) {

        AppConfig appConfig= (AppConfig) config;
        if (appConfig.getElasticsearchServerConfig()!=null) new EmbeddedES(
                appConfig.getElasticsearchServerConfig());

        final int serverPort= Integer.parseInt(appConfig.getServerPort());
        port(serverPort);

        Router router= new Router(
                new ControllerConfigurator().configure(appConfig.getControllerConfig()),
                getTypes(appConfig.getTypeNames()),
                appConfig.getCredentials());

        return new App(router);
    }

    private String[] getTypes(final String typesString) {
        String[] types= typesString.split(",");
        for (String typeName:types) {
            DocumentModel dm= new DocumentModel(typeName, "1", new ObjectMapper().createObjectNode(),"admin");
            if (dm==null) {
                logger.error("No document model found for "+typeName);
                System.exit(1);
            }
        }
        return types;
    }
}

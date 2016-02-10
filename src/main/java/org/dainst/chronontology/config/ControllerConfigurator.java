package org.dainst.chronontology.config;

import org.apache.log4j.Logger;
import org.dainst.chronontology.App;
import org.dainst.chronontology.controller.ConnectController;
import org.dainst.chronontology.controller.Controller;
import org.dainst.chronontology.controller.SimpleController;
import org.dainst.chronontology.store.ESRestSearchableDatastore;
import org.dainst.chronontology.store.FileSystemDatastore;
import org.dainst.chronontology.util.JsonRestClient;

import java.io.File;

/**
 * @author Daniel M. de Oliveira
 */
public class ControllerConfigurator {

    final static Logger logger = Logger.getLogger(ControllerConfigurator.class);

    public static Controller configure(ControllerConfig controllerConfig) {

        ESRestSearchableDatastore searchable=
                new ESRestSearchableDatastore(
                        new JsonRestClient(controllerConfig.getDatastoreConfigs()[0].getUrl()),
                        controllerConfig.getDatastoreConfigs()[0].getIndexName());


        Controller controller= null;
        if (controllerConfig.isUseConnect())
            controller= new ConnectController(
                    initDS(controllerConfig.getDatastoreConfigs()[1]),searchable);
        else
            controller= new SimpleController(searchable);

        return controller;
    }

    private static FileSystemDatastore initDS(DatastoreConfig datastoreConfig) {

        String datastorePath= datastoreConfig.getPath();

        if (!(new File(datastorePath).exists())) {
            logger.error("Creating directory \"" + datastorePath + "\" for usage by main datastore.");
            new File(datastorePath).mkdirs();
        }
        return new FileSystemDatastore(datastorePath);
    }
}

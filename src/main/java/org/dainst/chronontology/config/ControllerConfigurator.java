package org.dainst.chronontology.config;

import org.apache.log4j.Logger;
import org.dainst.chronontology.controller.ConnectController;
import org.dainst.chronontology.controller.Controller;
import org.dainst.chronontology.controller.SimpleController;
import org.dainst.chronontology.store.ESRestSearchableDatastore;
import org.dainst.chronontology.store.FileSystemDatastore;

import java.io.File;

/**
 * @author Daniel M. de Oliveira
 */
public class ControllerConfigurator {

    public static Controller configure(ControllerConfig controllerConfig) {

        ESRestSearchableDatastore searchable= (ESRestSearchableDatastore)
                DatastoreConfigurator.configure(controllerConfig.getDatastoreConfigs()[0]);

        Controller controller= null;
        if (controllerConfig.isUseConnect())
            controller= new ConnectController(
                    DatastoreConfigurator.configure(controllerConfig.getDatastoreConfigs()[1]),
                    searchable);
        else
            controller= new SimpleController(searchable);

        return controller;
    }
}

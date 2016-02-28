package org.dainst.chronontology.config;

import org.apache.log4j.Logger;
import org.dainst.chronontology.controller.ConnectController;
import org.dainst.chronontology.controller.Controller;
import org.dainst.chronontology.controller.RightsValidator;
import org.dainst.chronontology.controller.SimpleController;
import org.dainst.chronontology.store.ESRestSearchableDatastore;
import org.dainst.chronontology.store.FileSystemDatastore;

import java.io.File;

/**
 * @author Daniel M. de Oliveira
 */
public class ControllerConfigurator implements Configurator<Controller,ControllerConfig> {

    public Controller configure(ControllerConfig config) {

        ESRestSearchableDatastore searchable= (ESRestSearchableDatastore)
                new DatastoreConfigurator().configure(config.getDatastoreConfigs()[0]);

        Controller controller= null;
        if (config.isUseConnect())
            controller= new ConnectController(
                    new DatastoreConfigurator().configure(config.getDatastoreConfigs()[1]),
                    searchable);
        else
            controller= new SimpleController(searchable);

        return controller;
    }
}

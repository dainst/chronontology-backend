package org.dainst.chronontology.config;

import org.dainst.chronontology.controller.RightsValidator;

/**
 * @author Daniel M. de Oliveira
 */
public class RightsValidatorConfigurator implements Configurator<RightsValidator,RightsValidatorConfig> {

    @Override
    public RightsValidator configure(RightsValidatorConfig config) {
        RightsValidator validator= new RightsValidator();
        validator.setEditorRules(config.getEditorRules());
        return validator;
    }
}

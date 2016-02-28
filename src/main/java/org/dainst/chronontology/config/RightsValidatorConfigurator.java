package org.dainst.chronontology.config;

import org.dainst.chronontology.handler.RightsValidator;

/**
 * @author Daniel M. de Oliveira
 */
public class RightsValidatorConfigurator implements Configurator<RightsValidator,RightsValidatorConfig> {

    @Override
    public RightsValidator configure(RightsValidatorConfig config) {
        RightsValidator validator= new RightsValidator();
        validator.setRules(config.getRules());
        return validator;
    }
}

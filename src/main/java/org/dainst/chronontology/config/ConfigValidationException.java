package org.dainst.chronontology.config;

/**
 * Used to signal constraint violations in configuration.
 *
 * @author Daniel M. de Oliveira
 */
public class ConfigValidationException extends RuntimeException {

    public ConfigValidationException(String violation) {
        super(ConfigConstants.MSG_CONSTRAINT_VIOLATION + violation);
    }

    public ConfigValidationException() {
        super();
    }
}

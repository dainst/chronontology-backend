package org.dainst.chronontology.config;

/**
 * Used to signal constraint violations in configuration.
 *
 * @author Daniel M. de Oliveira
 */
public class ConfigException extends RuntimeException {

    public ConfigException(String violation) {
        super("Constraint violation in configuration: " + violation);
    }
}

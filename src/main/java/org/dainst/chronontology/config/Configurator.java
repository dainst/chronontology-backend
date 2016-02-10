package org.dainst.chronontology.config;

/**
 * @author Daniel M. de Oliveira
 */
interface Configurator<T> {

    T configure(Config config);
}

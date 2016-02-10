package org.dainst.chronontology.config;

/**
 * @author Daniel M. de Oliveira
 */
interface Configurator<T,U> {

    T configure(U config);
}

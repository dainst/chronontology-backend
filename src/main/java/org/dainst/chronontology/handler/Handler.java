package org.dainst.chronontology.handler;

import spark.Request;
import spark.Response;

import java.io.IOException;

/**
 * @author Daniel M. de Oliveira
 */
public interface Handler {

    Object handle(
            final Request req,
            final Response res) throws IOException;
}

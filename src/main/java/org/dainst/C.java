package org.dainst;

import org.elasticsearch.common.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author Daniel M. de Oliveira
 */
public class C {

    // A library for getting the status codes is not used on purpose
    // so we don't have to refactor in case it would change. The status codes themselves
    // are considered to be stable.
    static final int HTTP_CREATED = 201;
    static final int HTTP_FORBIDDEN = 403;
    static final int HTTP_OK = 200;
    static final int HTTP_NOT_FOUND = 404;
}

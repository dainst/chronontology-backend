package org.dainst.chronontology;

/**
 * @author Daniel M. de Oliveira
 */
public class Constants {

    public static final String VERSION = "version";
    public static final String MODIFIED = "modified";
    public static final String CREATED = "created";

    // A library for getting the status codes is not used on purpose
    // to protect against dependency change.
    public static final int HTTP_CREATED = 201;
    static final int HTTP_FORBIDDEN = 403;
    public static final int HTTP_OK = 200;
    public static final int HTTP_NOT_FOUND = 404;
    static final int HTTP_UNAUTHORIZED = 401;

    public static final String HEADER_AUTH = "Authorization";
    public static final String HEADER_CT = "Content-Type";
    public static final String HEADER_LOC = "location";
    public static final String HEADER_JSON = "application/json";

    public static final String DATASTORE_STATUS_OK = "ok";
    public static final String DATASTORE_STATUS_DOWN = "down";

    // Default settings
    public static final String EMBEDDED_ES_PORT = "9202";
    public static final String EMBEDDED_ES_URL = "http://localhost:"+EMBEDDED_ES_PORT;
    public static final String DATASTORE_PATH = "datastore/";
    public static final String SERVER_PORT = "4567";
    public static final String ES_INDEX_NAME = "connect";
}

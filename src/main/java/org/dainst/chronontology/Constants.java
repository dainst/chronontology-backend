package org.dainst.chronontology;

/**
 * @author Daniel M. de Oliveira
 */
public class Constants {

    // A library for getting the status codes is not used on purpose
    // to protect against dependency change.
    public static final int HTTP_CREATED = 201;
    public static final int HTTP_FORBIDDEN = 403;
    public static final int HTTP_OK = 200;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_UNAUTHORIZED = 401;
    public static final int HTTP_INTERNAL_SERVER_ERROR = 500;
    public static final int HTTP_BAD_REQUEST = 400;

    public static final String HEADER_AUTH = "Authorization";
    public static final String HEADER_CT = "Content-Type";
    public static final String HEADER_LOC = "location";
    public static final String HEADER_JSON = "application/json";

    public static final String DATASTORE_STATUS_OK = "ok";
    public static final String DATASTORE_STATUS_DOWN = "down";

    public static final String USER_NAME_ADMIN = "admin";
    public static final String USER_NAME_ANONYMOUS = "anonymous";

    public static final String MSG_NOT_NULL = " must not be null.";
    public static final String MSG_PROPS_NOT_NULL = "The argument props"+MSG_NOT_NULL;
    public static final String MSG_USER_NAME_NOT_NULL = "userName"+MSG_NOT_NULL;
    public static final String MSG_OPERATION_NOT_NULL = "operation"+MSG_NOT_NULL;

}

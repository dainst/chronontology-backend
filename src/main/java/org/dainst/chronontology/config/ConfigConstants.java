package org.dainst.chronontology.config;

/**
 * @author Daniel M. de Oliveira
 */
public class ConfigConstants {

    // Default settings
    public static final String EMBEDDED_ES_PORT = "9202";
    public static final String EMBEDDED_ES_URL = "http://localhost:"+EMBEDDED_ES_PORT;
    public static final String DATASTORE_PATH = "datastore/";
    public static final String SERVER_PORT = "4567";
    public static final String ES_INDEX_NAME = "connect";
    public static final String ES_SERVER_DATA_PATH = "embedded_es_data";
    public static final String ES_SERVER_CLUSTER_NAME = "chronontology_connected_embedded_es";

    public static final String MSG_CONSTRAINT_VIOLATION = "Constraint violation in configuration: ";

    public static final String DATASTORE_TYPE_ES = "elasticsearch";
    public static final String DATASTORE_TYPE_FS = "filesystem";
}

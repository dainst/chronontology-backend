package org.dainst.chronontology;

import com.squareup.okhttp.MediaType;

/**
 * @author Daniel M. de Oliveira
 */
public class TestConstants {
    public static final String TEST_TYPE= "period";
    public static final String TEST_INDEX= "jeremy_test";
    public static final String TEST_FOLDER = "src/test/resources/";

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public static final String USER_NAME = "admin";
    public static final String PASS_WORD = "s3cr3t";
    public static final String SERVER_PORT = "4567";
    public static final String SERVER_URL = "http://0.0.0.0:"+SERVER_PORT;
}

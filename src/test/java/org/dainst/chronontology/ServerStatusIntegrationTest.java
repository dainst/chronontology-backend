package org.dainst.chronontology;

import org.dainst.chronontology.IntegrationTestBase;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.dainst.chronontology.TestUtils.jsonAssertEquals;
import static org.testng.Assert.assertEquals;

/**
 * @author Daniel M. de Oliviera
 */
public class ServerStatusIntegrationTest extends IntegrationTestBase {

    @Test
    public void getServerStatus() throws IOException, InterruptedException {

        jsonAssertEquals(
            client.get("/"),
            json("{ \"datastores\" : [ " +
                    "{ \"type\" : \"main\", \"status\" : \"ok\" }, " +
                    "{ \"type\" : \"connect\",  \"status\" : \"ok\" } "   +
                    "] }")
        );
    }
}
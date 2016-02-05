package org.dainst.chronontology.store;

import org.dainst.chronontology.IntegrationTestBase;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.dainst.chronontology.TestUtils.jsonAssertEquals;
import static org.testng.Assert.assertEquals;

/**
 * @author Daniel M. de Oliviera
 */
public class UnsortedIntegrationTest extends IntegrationTestBase {

    @Test
    public void testBaseRoute() throws IOException, InterruptedException {

        jsonAssertEquals(
            client.get("/"),
            json("{ \"status\" : \"ok\"}")
        );
    }
}
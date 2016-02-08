package org.dainst.chronontology.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.dainst.chronontology.Constants;
import org.dainst.chronontology.it.IntegrationTestBase;
import org.dainst.chronontology.store.ESServerTestUtil;
import org.dainst.chronontology.util.Results;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.dainst.chronontology.JsonTestUtils.jsonAssertEquals;
import static org.dainst.chronontology.JsonTestUtils.*;

/**
 * @author Daniel M. de Oliviera
 */
public class ServerStatusIntegrationTest extends IntegrationTestBase {

    private JsonNode dataStoresJson(String status) {
        Results datastores = new Results("datastores");
        try {
            datastores.add(json("{ \"type\" : \"main\", \"status\" : \""+ Constants.DATASTORE_STATUS_OK+"\" }"));
            datastores.add(json("{ \"type\" : \"connect\", \"status\" : \""+status+"\" }"));
            return datastores.j();
        } catch (IOException e) {}
        return null;
    }

    @Test
    public void getServerStatus() throws IOException, InterruptedException {

        jsonAssertEquals(
                client.get("/"),
                dataStoresJson(Constants.DATASTORE_STATUS_OK)
        );
    }

    /**
     * Note that that it might cause problems at some point when
     * this test finishes while es is down. Then other tests of this
     * class might not get executed properly.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testESisDown() throws IOException, InterruptedException {

        ESServerTestUtil.stopElasticSearchServer();

        jsonAssertEquals(
                client.get("/"),
                dataStoresJson(Constants.DATASTORE_STATUS_DOWN)
        );

        ESServerTestUtil.startElasticSearchServer();

        jsonAssertEquals(
                client.get("/"),
                dataStoresJson(Constants.DATASTORE_STATUS_OK)
        );
    }
}
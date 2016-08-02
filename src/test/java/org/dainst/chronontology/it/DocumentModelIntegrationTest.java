package org.dainst.chronontology.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.dainst.chronontology.JsonTestUtils;
import org.dainst.chronontology.TestConstants;
import org.dainst.chronontology.handler.model.Document;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

/**
 * A collection of integration tests for testing basic
 * properties of the document/resource basic data model
 * and its fixed properties, like version or resource id.
 *
 * @author Daniel de Oliveira
 */
public class DocumentModelIntegrationTest extends IntegrationTest {

    @Test
    public void getAndResultAreSameAfterPostAndPut() {
        JsonNode postResult= client.post(TYPE_ROUTE, JsonTestUtils.sampleDocument("a"));
        String id= idOf(postResult);
        assertEquals(postResult.toString(),client.get(TYPE_ROUTE+id).toString());
        JsonNode putResult= client.put(TYPE_ROUTE+id, JsonTestUtils.sampleDocument("b"));
        assertEquals(putResult.toString(),client.get(TYPE_ROUTE+id).toString());
    }

    @Test
    public void idAndTypeExistAfterPostAndPut() {
        JsonNode postResult= client.post(TYPE_ROUTE, JsonTestUtils.sampleDocument("a"));
        String id= idOf(postResult);
        assertNotNull(id);
        assertEquals(postResult.get(Document.RESOURCE).get(Document.TYPE).textValue(), TestConstants.TEST_TYPE);
        JsonNode putResult= client.put(TYPE_ROUTE+id, JsonTestUtils.sampleDocument("b"));
        assertEquals(putResult.get(Document.RESOURCE).get(Document.ID).textValue(),id);
        assertEquals(putResult.get(Document.RESOURCE).get(Document.TYPE).textValue(),TestConstants.TEST_TYPE);
    }

    @Test
    public void idAndTypeExistAfterCreateWithPut() {
        JsonNode putResult= client.put(TYPE_ROUTE+"a", JsonTestUtils.sampleDocument("a"));
        String id= idOf(putResult);
        assertNotNull(id);
        assertEquals(putResult.get(Document.RESOURCE).get(Document.TYPE).textValue(),TestConstants.TEST_TYPE);
    }

    @Test
    public void userCannotSetOrChangeId() {
        JsonNode n= JsonTestUtils.sampleDocument("a","usersChoice","none");

        String id= idOf(client.post(TYPE_ROUTE, n));
        assertNotEquals(
                id,
                "usersChoice");

        assertNotEquals(
                idOf(client.put(TYPE_ROUTE+id, n))
                ,"usersChoice");

        assertEquals( // create via put
                idOf(client.put(TYPE_ROUTE+"a", n))
                ,"a");
    }
}

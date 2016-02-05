package org.dainst.chronontology.model;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONException;
import com.fasterxml.jackson.databind.JsonNode;
import org.testng.annotations.Test;
import java.io.IOException;

import static org.dainst.chronontology.Constants.*;
import static org.testng.Assert.assertEquals;
import static org.dainst.chronontology.JsonTestUtils.*;
import static org.dainst.chronontology.TestConstants.*;

/**
 * @author Daniel M. de Oliveira
 */
public class GenericTypeDocumentModelTest {

    private String extractDateCreated(JsonNode n) {
        return n.get(CREATED).toString().replace("\"", "");
    }

    /**
     * Produces a node whose modified dates array
     * is a merge of both the arguments nodes created dates.
     * @param old
     * @param niew
     * @return
     */
    private JsonNode nodeWithModifiedDates(JsonNode old, JsonNode niew) {
        ObjectNode example= (ObjectNode) json();
        ArrayNode a = example.putArray(MODIFIED);
        a.add(extractDateCreated(old));
        a.add(extractDateCreated(niew));
        return example;
    }

    private JsonNode makeNodeWithVersion(int version) {
        JsonNode example= json();
        ((ObjectNode)example).put(VERSION,version);
        return example;
    }

    @Test
    public void createdDateStaysSame() throws IOException, InterruptedException {
        JsonNode old=
                new GenericTypeDocumentModel(TEST_TYPE,"1",json()).j();
        Thread.sleep(10);
        GenericTypeDocumentModel dm=
                new GenericTypeDocumentModel(TEST_TYPE,"1",json());

        assertEquals(
                extractDateCreated(dm.merge(old).j()),
                extractDateCreated(old));
    }

    @Test
    public void modifiedDatesMerge() throws IOException, InterruptedException, JSONException {
        JsonNode old=
                new GenericTypeDocumentModel(TEST_TYPE,"1",json()).j();
        Thread.sleep(10);
        GenericTypeDocumentModel dm=
                new GenericTypeDocumentModel(TEST_TYPE,"1",json());

        JsonNode nodeWithDates = nodeWithModifiedDates(old, dm.j());

        jsonAssertEquals(
                dm.merge(old).j(),
                nodeWithDates);
    }

    @Test
    public void setVersionOnCreate() throws IOException, InterruptedException, JSONException {

        jsonAssertEquals(
                new GenericTypeDocumentModel(TEST_TYPE,"1",json()).j(),
                makeNodeWithVersion(1));
    }


    @Test
    public void countVersions() throws IOException, InterruptedException, JSONException {
        JsonNode old=
                new GenericTypeDocumentModel(TEST_TYPE,"1",json()).j();
        GenericTypeDocumentModel dm=
                new GenericTypeDocumentModel(TEST_TYPE,"1",json());

        jsonAssertEquals(dm.merge(old).j(), makeNodeWithVersion(2));
    }
}

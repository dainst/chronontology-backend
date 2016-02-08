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

    public static final String ADMIN = "admin";

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
        a.add(old.get(MODIFIED).get(0));
        a.add(niew.get(MODIFIED).get(0));
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
                new GenericTypeDocumentModel(TEST_TYPE,"1",json(), ADMIN).j();
        Thread.sleep(10);
        GenericTypeDocumentModel dm=
                new GenericTypeDocumentModel(TEST_TYPE,"1",json(), ADMIN);

        jsonAssertEquals(
                dm.merge(old).j().get(CREATED),
                old.get(CREATED));
    }

    @Test
    public void modifiedDatesMerge() throws IOException, InterruptedException, JSONException {
        JsonNode old=
                new GenericTypeDocumentModel(TEST_TYPE,"1",json(), ADMIN).j();
        Thread.sleep(10);
        GenericTypeDocumentModel dm=
                new GenericTypeDocumentModel(TEST_TYPE,"1",json(), ADMIN);

        JsonNode nodeWithDates = nodeWithModifiedDates(old, dm.j());

        jsonAssertEquals(
                dm.merge(old).j(),
                nodeWithDates);
    }

    @Test
    public void setVersionOnCreate() throws IOException, InterruptedException, JSONException {

        jsonAssertEquals(
                new GenericTypeDocumentModel(TEST_TYPE,"1",json(), ADMIN).j(),
                makeNodeWithVersion(1));
    }

    @Test
    public void setCreateUserOnCreate() throws IOException {
        jsonAssertEquals(
                new GenericTypeDocumentModel(TEST_TYPE,"1",json(), ADMIN).j()
                        .get(CREATED),
                json("{ \"user\" : \""+ADMIN+"\" }"));
    }

    @Test
    public void setModifiedUserOnCreate() throws IOException {
        jsonAssertEquals(
                new GenericTypeDocumentModel(TEST_TYPE,"1",json(), ADMIN).j().
                        get(MODIFIED).get(0),
                json("{\"user\":\""+ADMIN+"\"}"));
    }

    @Test
    public void differentUserOnModify() throws IOException {
        JsonNode old=
                new GenericTypeDocumentModel(TEST_TYPE,"1",json(), ADMIN).j();
        GenericTypeDocumentModel dm=
                new GenericTypeDocumentModel(TEST_TYPE,"1",json(), "ove");

        jsonAssertEquals(
                dm.merge(old).j().
                        get(MODIFIED).get(1),
                json("{\"user\":\"ove\"}"));
    }


    @Test
    public void countVersions() throws IOException, InterruptedException, JSONException {
        JsonNode old=
                new GenericTypeDocumentModel(TEST_TYPE,"1",json(), ADMIN).j();
        GenericTypeDocumentModel dm=
                new GenericTypeDocumentModel(TEST_TYPE,"1",json(), ADMIN);

        jsonAssertEquals(dm.merge(old).j(), makeNodeWithVersion(2));
    }
}

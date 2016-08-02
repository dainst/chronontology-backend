package org.dainst.chronontology.handler.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.dainst.chronontology.TestConstants;
import org.dainst.chronontology.util.JsonUtils;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.dainst.chronontology.JsonTestUtils.jsonAssertEquals;
import static org.dainst.chronontology.util.JsonUtils.json;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;


/**
 * @author Daniel de Oliveira
 */
public class DocumentTest {

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
        ArrayNode a = example.putArray(Document.MODIFIED);
        a.add(old.get(Document.MODIFIED).get(0));
        a.add(niew.get(Document.MODIFIED).get(0));
        return example;
    }

    private JsonNode makeNodeWithVersion(int version) {
        JsonNode example= json();
        ((ObjectNode)example).put(Document.VERSION,version);
        return example;
    }

    private Document exampleDoc() {
        return new Document("1",TestConstants.TEST_TYPE,json(), ADMIN);
    }

    /**
     * This is like a resource already stored succesfully.
     * @return
     */
    private JsonNode oldJson() {
        return json("{\"id\":\"1\",\"type\":\""+ TestConstants.TEST_TYPE+"\"}");
    }

    private JsonNode single(String k,String v) {
        return json("{\""+k+"\":\""+v+"\"}");
    }

    @Test
    public void setResourceId() {
        Document doc= exampleDoc();
        assertEquals(doc.j().get(Document.RESOURCE).get(Document.ID).toString(),"\"1\"");
        assertEquals(doc.getId(),"1");
    }

    @Test
    public void setResourceType() {
        Document doc= exampleDoc();
        assertEquals(doc.j().get(Document.RESOURCE).get(Document.TYPE).toString(),"\""+TestConstants.TEST_TYPE+"\"");
        assertEquals(doc.getType(),TestConstants.TEST_TYPE);
    }


    @Test
    public void createdDateStaysSame() throws IOException, InterruptedException {
        Document old= exampleDoc();
        Thread.sleep(10);
        Document dm= exampleDoc();

        jsonAssertEquals(
                dm.merge(old).j().get(Document.CREATED),
                old.j().get(Document.CREATED));
    }

    @Test
    public void modifiedDatesMerge() throws IOException, InterruptedException, JSONException {
        Document old= exampleDoc();
        Thread.sleep(10);
        Document dm= exampleDoc();

        JsonNode nodeWithDates = nodeWithModifiedDates(old.j(), dm.j());

        jsonAssertEquals(
                dm.merge(old).j(),
                nodeWithDates);
    }

    @Test
    public void mergeTakesIdAndTypeFromOldDoc() {
        Document oldOne= exampleDoc();
        Document newOne= new Document("2","other",json(), ADMIN);;

        newOne.merge(oldOne);
        assertEquals(newOne.getId(),"1");
        assertEquals(newOne.getType(),TestConstants.TEST_TYPE);
    }

    @Test
    public void filterUnsupported() {
        JsonNode n= JsonUtils.json();
        ((ObjectNode)n).put("a","a");   // unwanted
        ((ObjectNode)n).put(Document.ID,"1"); // unwanted
        ((ObjectNode)n).put(Document.RESOURCE,json());
        ((ObjectNode)n).put(Document.DATASET,"c");

        Document dm=
                new Document("1",TestConstants.TEST_TYPE,n, ADMIN);

        assertNotNull(dm.j().get(Document.RESOURCE));
        assertNotNull(dm.j().get(Document.DATASET));
        assertNull(dm.j().get("a"));
        assertNull(dm.j().get(Document.ID));
    }


    @Test
    public void setVersionOnCreate() throws IOException, InterruptedException, JSONException {

        jsonAssertEquals(
                new Document("1",TestConstants.TEST_TYPE,json(), ADMIN).j(),
                makeNodeWithVersion(1));
    }

    @Test
    public void setCreateUserOnCreate() throws IOException {
        jsonAssertEquals(exampleDoc().j()
                        .get(Document.CREATED),
                single("user",ADMIN));
    }

    @Test
    public void setModifiedUserOnCreate() throws IOException {
        jsonAssertEquals(
                new Document("1",TestConstants.TEST_TYPE,json(), ADMIN).j().
                        get(Document.MODIFIED).get(0),
                single("user",ADMIN));
    }

    @Test
    public void differentUserOnModify() throws IOException {
        Document old= new Document("1",TestConstants.TEST_TYPE,json(), ADMIN);
        Document dm=
                new Document("1",TestConstants.TEST_TYPE,json(), "ove");

        jsonAssertEquals(
                dm.merge(old).j().
                        get(Document.MODIFIED).get(1),
                single("user","ove"));
    }


    @Test
    public void countVersions() throws IOException, InterruptedException, JSONException {
        Document old=exampleDoc();
        Document dm=exampleDoc();

        jsonAssertEquals(dm.merge(old).j(), makeNodeWithVersion(2));
    }

    @Test
    public void createFromOld() {
        JsonNode n= json();
        ((ObjectNode)n).put(Document.RESOURCE,oldJson());
        ((ObjectNode)n).put(Document.CREATED,single("date","today"));

        Document dm= Document.from(n);
        assertEquals(dm.getId(),"1");
        assertEquals(dm.getType(),TestConstants.TEST_TYPE);
        assertEquals(dm.j().get(Document.CREATED),single("date","today"));
    }

    @Test
    public void createFromOldNoType() {
        JsonNode n = json();
        ((ObjectNode) n).put(Document.RESOURCE, single(Document.ID, "1"));

        try {
            Document.from(n);
            fail();
        } catch (Exception expected) {
        }
    }


    @Test
    public void createFromOldNull() {
        assertEquals(Document.from(null),null);
    }

    @Test
    public void getDataset() {
        JsonNode n= json();
        ((ObjectNode)n).put(Document.RESOURCE,oldJson());
        ((ObjectNode)n).put(Document.DATASET,"1");

        Document dm= Document.from(n);
        assertEquals(dm.getDataset(),"1");
    }

    @Test
    public void noDataset() {
        JsonNode n= json();
        ((ObjectNode)n).put(Document.RESOURCE,oldJson());

        Document dm= Document.from(n);
        assertEquals(dm.getDataset(),null);
    }
}

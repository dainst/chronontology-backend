package org.dainst.chronontology.handler.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.dainst.chronontology.handler.model.Document;
import org.dainst.chronontology.util.JsonUtils;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.dainst.chronontology.JsonTestUtils.jsonAssertEquals;
import static org.dainst.chronontology.util.JsonUtils.*;
import static org.testng.Assert.assertEquals;

/**
 * @author Daniel M. de Oliveira
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

    @Test
    public void createdDateStaysSame() throws IOException, InterruptedException {
        Document old=
                new Document("1",json(), ADMIN);
        Thread.sleep(10);
        Document dm=
                new Document("1",json(), ADMIN);

        jsonAssertEquals(
                dm.merge(old).j().get(Document.CREATED),
                old.j().get(Document.CREATED));
    }

    @Test
    public void modifiedDatesMerge() throws IOException, InterruptedException, JSONException {
        Document old=
                new Document("1",json(), ADMIN);
        Thread.sleep(10);
        Document dm=
                new Document("1",json(), ADMIN);

        JsonNode nodeWithDates = nodeWithModifiedDates(old.j(), dm.j());

        jsonAssertEquals(
                dm.merge(old).j(),
                nodeWithDates);
    }

    @Test
    public void filterUnsupported() {
        JsonNode n= JsonUtils.json();
        ((ObjectNode)n).put("a","a"); // unwanted
        ((ObjectNode)n).put(Document.RESOURCE,"b");
        ((ObjectNode)n).put(Document.DATASET,"c");

        JsonNode exp= JsonUtils.json();
        ((ObjectNode)exp).put(Document.RESOURCE,"b");
        ((ObjectNode)exp).put(Document.DATASET,"c");

        Document dm=
                new Document("1",n, ADMIN);

        assertEquals(dm.j().get(Document.RESOURCE).toString().replace("\"",""),"b");
        assertEquals(dm.j().get(Document.DATASET).toString().replace("\"",""),"c");
        assertEquals(dm.j().get("a"),null);
    }


    @Test
    public void setVersionOnCreate() throws IOException, InterruptedException, JSONException {

        jsonAssertEquals(
                new Document("1",json(), ADMIN).j(),
                makeNodeWithVersion(1));
    }

    @Test
    public void setCreateUserOnCreate() throws IOException {
        jsonAssertEquals(
                new Document("1",json(), ADMIN).j()
                        .get(Document.CREATED),
                json("{ \"user\" : \""+ADMIN+"\" }"));
    }

    @Test
    public void setModifiedUserOnCreate() throws IOException {
        jsonAssertEquals(
                new Document("1",json(), ADMIN).j().
                        get(Document.MODIFIED).get(0),
                json("{\"user\":\""+ADMIN+"\"}"));
    }

    @Test
    public void differentUserOnModify() throws IOException {
        Document old=
                new Document("1",json(), ADMIN);
        Document dm=
                new Document("1",json(), "ove");

        jsonAssertEquals(
                dm.merge(old).j().
                        get(Document.MODIFIED).get(1),
                json("{\"user\":\"ove\"}"));
    }


    @Test
    public void countVersions() throws IOException, InterruptedException, JSONException {
        Document old=
                new Document("1",json(), ADMIN);
        Document dm=
                new Document("1",json(), ADMIN);

        jsonAssertEquals(dm.merge(old).j(), makeNodeWithVersion(2));
    }

    @Test
    public void createFromOld() {
        JsonNode n= json();
        ((ObjectNode)n).put(Document.ID,"1");
        ((ObjectNode)n).put(Document.CREATED,json("{\"date\":\"today\"}"));

        Document dm= Document.from(n);
        assertEquals(dm.getId(),"1");
        assertEquals(dm.j().get(Document.CREATED),json("{\"date\":\"today\"}"));
    }

    @Test
    public void createFromOldNull() {
        assertEquals(Document.from(null),null);
    }

    @Test
    public void getDataset() {
        JsonNode n= json();
        ((ObjectNode)n).put(Document.ID,"1");
        ((ObjectNode)n).put(Document.DATASET,"1");

        Document dm= Document.from(n);
        assertEquals(dm.getDataset(),"1");
    }

    @Test
    public void noDataset() {
        JsonNode n= json();
        ((ObjectNode)n).put(Document.ID,"1");

        Document dm= Document.from(n);
        assertEquals(dm.getDataset(),null);
    }
}

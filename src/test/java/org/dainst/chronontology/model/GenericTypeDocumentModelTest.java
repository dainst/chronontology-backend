package org.dainst.chronontology.model;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;
import java.io.IOException;

import static org.testng.Assert.assertEquals;


/**
 * @author Daniel M. de Oliveira
 */
public class GenericTypeDocumentModelTest {

    @Test
    public void createdDateStaysSame() throws IOException, InterruptedException {
        JsonNode old=
                new GenericTypeDocumentModel("period","1",new ObjectMapper().readTree("{}")).j();
        String dateCreated= (String) old.get("created").toString();

        Thread.sleep(10);

        GenericTypeDocumentModel dm=
                new GenericTypeDocumentModel("period","1",new ObjectMapper().readTree("{}"));
        dm.mix(old);

        assertEquals(dm.j().get("created").toString(),dateCreated);
    }

    @Test
    public void modifiedDatesMerge() throws IOException, InterruptedException, JSONException {
        JsonNode old=
                new GenericTypeDocumentModel("period","1",new ObjectMapper().readTree("{}")).j();
        String dateCreatedOld= old.get("created").toString();

        Thread.sleep(10);

        GenericTypeDocumentModel dm=
                new GenericTypeDocumentModel("period","1",new ObjectMapper().readTree("{}"));
        String dateCreatedNew= dm.j().get("created").toString();

        JsonNode example= new ObjectMapper().readTree("{}");
        ArrayNode a = ((ObjectNode) example).putArray("modified");
        a.add(dateCreatedOld.replace("\"", ""));
        a.add(dateCreatedNew.replace("\"", ""));

        dm.mix(old);
        JSONAssert.assertEquals(example.toString(), dm.j().toString(),false);
    }

    @Test
    public void setVersionOnCreate() throws IOException, InterruptedException, JSONException {
        JsonNode example= new ObjectMapper().createObjectNode();
        ((ObjectNode)example).put("version",1);

        JsonNode j=
                new GenericTypeDocumentModel("period","1",new ObjectMapper().createObjectNode()).j();

        JSONAssert.assertEquals(example.toString(), j.toString(), false);
    }

    @Test
    public void countVersions() throws IOException, InterruptedException, JSONException {
        JsonNode example= new ObjectMapper().createObjectNode();
        ((ObjectNode)example).put("version",2);

        JsonNode old=
                new GenericTypeDocumentModel("period","1",new ObjectMapper().createObjectNode()).j();
        GenericTypeDocumentModel dm=
                new GenericTypeDocumentModel("period","1",new ObjectMapper().createObjectNode());
        dm.mix(old);

        JSONAssert.assertEquals(example.toString(), dm.j().toString(), false);
    }
}

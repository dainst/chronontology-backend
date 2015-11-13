package org.dainst.chronontology;

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
public class DocumentModelTest {

    @Test
    public void createdDateStaysSame() throws IOException, InterruptedException {
        JsonNode old=
                new DocumentModel("period",new ObjectMapper().readTree("{}"),"1").j();
        String dateCreated= (String) old.get("created").toString();

        Thread.sleep(10);

        DocumentModel dm=
                new DocumentModel("period",new ObjectMapper().readTree("{}"),"1");
        dm.mix(old);

        assertEquals(dm.j().get("created").toString(),dateCreated);
    }

    @Test
    public void modifiedDatesMerge() throws IOException, InterruptedException, JSONException {
        JsonNode old=
                new DocumentModel("period",new ObjectMapper().readTree("{}"),"1").j();
        String dateCreatedOld= old.get("created").toString();

        Thread.sleep(10);

        DocumentModel dm=
                new DocumentModel("period",new ObjectMapper().readTree("{}"),"1");
        String dateCreatedNew= dm.j().get("created").toString();

        JsonNode example= new ObjectMapper().readTree("{}");
        ArrayNode a = ((ObjectNode) example).putArray("modified");
        a.add(dateCreatedOld.replace("\"", ""));
        a.add(dateCreatedNew.replace("\"", ""));

        dm.mix(old);
        JSONAssert.assertEquals(example.toString(), dm.j().toString(),false);
    }
}

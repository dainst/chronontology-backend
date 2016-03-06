package org.dainst.chronontology.it;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.dainst.chronontology.handler.model.Document;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.dainst.chronontology.JsonTestUtils.assertResultsAreFound;
import static org.dainst.chronontology.JsonTestUtils.sampleDocument;
import static org.dainst.chronontology.it.ESClientTestUtil.refreshES;

import static org.testng.Assert.assertEquals;

/**
 * @author Daniel M. de Oliveira
 */
public class SearchIntegrationTest extends IntegrationTest {

    private List<String> postSampleData(String dataset,String... sampleData) {
        List<String> ids= new ArrayList<String>();
        for (String sample:sampleData) {
            ids.add(idOf(client.post(TYPE_ROUTE, sampleDocument(sample,null,dataset))));
        }
        refreshES();
        return ids;
    }

    @Test
    public void matchQueryTermWithUrlEncodedSlashes() throws IOException, InterruptedException {

        ESClientTestUtil.createEsTypeAndMapping();
        refreshES();

        List<String> ids= postSampleData(null,"/type/1","/type/2");
        ids.remove(1);

        assertResultsAreFound(
                client.get(TYPE_ROUTE + "?q="+Document.RESOURCE+":%22%2Ftype%2F1%22")
                ,ids);
    }

    @Test
    public void searchInAllFields() throws IOException, InterruptedException {

        List<String> ids= postSampleData(null,"abc","def");
        ids.remove(0);

        assertResultsAreFound(
                client.get(TYPE_ROUTE + "?q=def")
                ,ids);
    }

    @Test
    public void searchWithoutSizeRestriction() throws IOException, InterruptedException {

        List<String> ids= postSampleData(null,"b","b");

        assertResultsAreFound(
                client.get(TYPE_ROUTE + "?q="+ Document.RESOURCE+":b")
                ,ids);
    }

    @Test
    public void restrictedSizeSearchSizeLowerThanZero() throws IOException, InterruptedException {

        List<String> ids= postSampleData(null,"b","b");

        assertResultsAreFound(
                client.get(TYPE_ROUTE + "?q="+Document.RESOURCE+":b&size=-1")
                ,ids);
    }

    @Test
    public void restrictedSizeSearch() throws IOException, InterruptedException {

        List<String> ids= postSampleData(null,"b","b","a");
        ids.remove(0);
        ids.remove(0);

        assertResultsAreFound(
                client.get(TYPE_ROUTE + "?q="+Document.RESOURCE+":a&size=1")
                ,ids);
    }

    @Test
    public void restrictedSizeSearchSizeIsZero() throws IOException, InterruptedException {

        postSampleData(null,"b","b","a");

        assertResultsAreFound(
                client.get(TYPE_ROUTE + "?q=a:b&size=0")
                ,new ArrayList<String>());
    }



    @Test
    public void restrictedSizeSearchWithDatasets() throws IOException, InterruptedException {

        postSampleData("ds1","a","a","a");
        List<String> ids= postSampleData(null,"a","a","a");

        client.authenticate(null,null);

        assertResultsAreFound(client.get(TYPE_ROUTE + "?q="+Document.RESOURCE+":a&size=3"),ids);
    }

    @Test
    public void sizeAndOffsetSearchWithDatasets() throws IOException, InterruptedException {

        postSampleData("ds1","a","a","a");
        postSampleData(null,"a","a","a");

        client.authenticate(null,null);

        assertEquals(
            ((ArrayNode) client.get(TYPE_ROUTE + "?q="+Document.RESOURCE+":a&size=3&offset=1")
                    .get("results")).size()
            ,2);
        assertEquals(
            ((ArrayNode) client.get(TYPE_ROUTE + "?q="+Document.RESOURCE+":a&size=1&offset=1")
                    .get("results")).size()
            ,1);
        assertEquals(
            ((ArrayNode) client.get(TYPE_ROUTE + "?q="+Document.RESOURCE+":a&offset=2")
                    .get("results")).size()
            ,1);
    }
}

package org.dainst.chronontology.it;

import org.dainst.chronontology.handler.model.Document;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.dainst.chronontology.JsonTestUtils.assertResultsAreFound;
import static org.dainst.chronontology.JsonTestUtils.sampleDocument;
import static org.dainst.chronontology.it.ESClientTestUtil.refreshES;


// TODO factor out some tests to become unit tests
/**
 * @author Daniel M. de Oliveira
 */
public class SearchIntegrationTest extends IntegrationTest {

    private List<String> postSampleData(String... sampleData) {
        List<String> ids= new ArrayList<String>();
        for (String sample:sampleData) {
            ids.add(idOf(client.post(TYPE_ROUTE, sampleDocument(sample))));
        }
        refreshES();
        return ids;
    }

    @Test
    public void matchQueryTermWithUrlEncodedSlashes() throws IOException, InterruptedException {

        List<String> ids= postSampleData("/type/1","/type/2");
        ids.remove(1);

        assertResultsAreFound(
                client.get(TYPE_ROUTE + "?q="+Document.RESOURCE+":%22%2Ftype%2F1%22")
                ,ids);
    }

    @Test
    public void searchInAllFields() throws IOException, InterruptedException {

        List<String> ids= postSampleData("abc","def");
        ids.remove(0);

        assertResultsAreFound(
                client.get(TYPE_ROUTE + "?q=def")
                ,ids);
    }

    @Test
    public void searchWithoutSizeRestriction() throws IOException, InterruptedException {

        List<String> ids= postSampleData("b","b");

        assertResultsAreFound(
                client.get(TYPE_ROUTE + "?q="+ Document.RESOURCE+":b")
                ,ids);
    }

    @Test
    public void restrictedSizeSearchSizeLowerThanZero() throws IOException, InterruptedException {

        List<String> ids= postSampleData("b","b");

        assertResultsAreFound(
                client.get(TYPE_ROUTE + "?q="+Document.RESOURCE+":b&size=-1")
                ,ids);
    }

    @Test
    public void restrictedSizeSearch() throws IOException, InterruptedException {

        List<String> ids= postSampleData("b","b","a");
        ids.remove(0);
        ids.remove(0);

        assertResultsAreFound(
                client.get(TYPE_ROUTE + "?q="+Document.RESOURCE+":a&size=1")
                ,ids);
    }

    @Test
    public void restrictedSizeSearchSizeIsZero() throws IOException, InterruptedException {

        postSampleData("b","b","a");

        assertResultsAreFound(
                client.get(TYPE_ROUTE + "?q=a:b&size=0")
                ,new ArrayList<String>());
    }

    @Test
    public void restrictedSizeSearchWithDatasets() throws IOException, InterruptedException {

        // TODO make sure elasticsearch sort order returns these objects first
        client.post(TYPE_ROUTE, sampleDocument("a",null,"ds1"));
        client.post(TYPE_ROUTE, sampleDocument("a",null,"ds1"));
        client.post(TYPE_ROUTE, sampleDocument("a",null,"ds1"));
        refreshES();
        List<String> ids= postSampleData("a","a","a");

        client.authenticate(null,null);
        assertResultsAreFound(client.get(TYPE_ROUTE + "?q="+Document.RESOURCE+":a&size=3"),ids);
    }


}

package org.dainst.chronontology.handler;

import org.dainst.chronontology.JsonTestUtils;
import org.dainst.chronontology.handler.dispatch.Dispatcher;
import org.dainst.chronontology.handler.model.Results;
import org.dainst.chronontology.handler.model.RightsValidator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Arrays;

import static org.mockito.Mockito.*;

/**
 * @author Daniel M. de Oliveira
 */
public class SearchDocumentHandlerTest {

    private static final RightsValidator validator= mock(RightsValidator.class);
    private static final Dispatcher dispatcher= mock(Dispatcher.class);
    private static final SearchDocumentHandler handler= new SearchDocumentHandler(dispatcher,validator);

    private Request reqMock= mock(Request.class);
    private Response resMock= mock(Response.class);

    @BeforeMethod
    public void before() {
        Results dispatchResults= new Results("results");
        dispatchResults.add(JsonTestUtils.sampleDocument("a","1"));
        dispatchResults.add(JsonTestUtils.sampleDocument("a","2","dataset1"));
        dispatchResults.add(JsonTestUtils.sampleDocument("a","3"));
        dispatchResults.add(JsonTestUtils.sampleDocument("a","4","dataset1"));
        dispatchResults.add(JsonTestUtils.sampleDocument("a","5"));

        when(dispatcher.dispatchSearch(any(),any())).thenReturn(dispatchResults);
    }

    @Test
    public void returnCorrectResultsWithOmittingDocsWithDatasets() throws IOException {

        when(reqMock.queryString()).thenReturn("?size=2");

        JsonTestUtils.assertResultsAreFound(
                ((Results) handler.handle(reqMock,resMock)).j(),
                Arrays.asList("1","3"));
    }

    @Test
    public void returnCorrectResultsWithEmbeddedSizeParam() throws IOException {

        when(reqMock.queryString()).thenReturn("?width=10&size=2&length=9");

        JsonTestUtils.assertResultsAreFound(
                ((Results) handler.handle(reqMock,resMock)).j(),
                Arrays.asList("1","3"));
    }

    @Test
    public void returnCorrectResultsWithNullQueryString() throws IOException {

        when(reqMock.queryString()).thenReturn(null);

        JsonTestUtils.assertResultsAreFound(
                ((Results) handler.handle(reqMock,resMock)).j(),
                Arrays.asList("1","3","5"));
    }

    @Test
    public void returnCorrectResultsWithNegativeSizeParam() throws IOException {

        when(reqMock.queryString()).thenReturn("?size=-1");

        JsonTestUtils.assertResultsAreFound(
                ((Results) handler.handle(reqMock,resMock)).j(),
                Arrays.asList("1","3","5"));
    }

    @Test
    public void returnCorrectResultsWithoutNumberInSizeParam() throws IOException {

        when(reqMock.queryString()).thenReturn("?size=");

        JsonTestUtils.assertResultsAreFound(
                ((Results) handler.handle(reqMock,resMock)).j(),
                Arrays.asList("1","3","5"));
    }

    @Test
    public void stripOutQueryParam() throws IOException {

        when(reqMock.queryString()).thenReturn("?size=10");
        when(reqMock.pathInfo()).thenReturn("/path/");

        handler.handle(reqMock,resMock);
        verify(dispatcher, atLeastOnce()).dispatchSearch("/path/","");
    }

    @Test
    public void stripOutEmbeddedQueryParam() throws IOException {

        when(reqMock.queryString()).thenReturn("?width=10&size=2&length=9");
        when(reqMock.pathInfo()).thenReturn("/path/");

        handler.handle(reqMock,resMock);
        verify(dispatcher, atLeastOnce()).dispatchSearch("/path/","?width=10&length=9");
    }
}

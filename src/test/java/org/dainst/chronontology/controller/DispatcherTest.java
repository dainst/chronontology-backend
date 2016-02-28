package org.dainst.chronontology.controller;

import org.dainst.chronontology.Constants;
import org.dainst.chronontology.handler.CrudHandler;
import org.dainst.chronontology.handler.RightsValidator;
import org.dainst.chronontology.store.SearchableDatastore;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * NOTE that here we test both functionality of {@link Dispatcher}
 * as well as of {@link SimpleDispatcher}.
 *
 * @author Daniel M. de Oliveira
 */
public class DispatcherTest {

    private static final String ROUTE = "route";
    private SearchableDatastore mockDS= mock(SearchableDatastore.class);
    private Request reqMock= mock(Request.class);
    private Dispatcher dispatcher = new SimpleDispatcher(mockDS);
    private CrudHandler crudHandler= new CrudHandler(dispatcher,new RightsValidator());


    @BeforeMethod
    public void before() {
        when(reqMock.attribute(any())).thenReturn("user");
        when(reqMock.body()).thenReturn("{}");
    }

    @Test
    public void postOK() throws IOException {

        when(mockDS.put(any(),any(),any())).thenReturn(true);


        Response resMock= mock(Response.class);
        crudHandler.handlePost(ROUTE,reqMock,resMock);
        verify(resMock,atMost(0)).status(Constants.HTTP_INTERNAL_SERVER_ERROR);
        verify(resMock).status(Constants.HTTP_CREATED);
    }

    @Test
    public void putOK() throws IOException {

        when(mockDS.put(any(),any(),any())).thenReturn(true);

        Response resMock= mock(Response.class);
        crudHandler.handlePut(ROUTE,reqMock,resMock);
        verify(resMock,atMost(0)).status(Constants.HTTP_INTERNAL_SERVER_ERROR);
        verify(resMock).status(Constants.HTTP_CREATED);
    }

    @Test
    public void postNotOk() throws IOException {

        when(mockDS.put(any(),any(),any())).thenReturn(false);

        Response resMock= mock(Response.class);
        crudHandler.handlePost(ROUTE,reqMock,resMock);

        verify(resMock,atMost(0)).status(Constants.HTTP_CREATED);
        verify(resMock,atMost(0)).status(Constants.HTTP_OK);
        verify(resMock).status(Constants.HTTP_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void putNotOk() throws IOException {

        when(mockDS.put(any(),any(),any())).thenReturn(false);

        Response resMock= mock(Response.class);
        crudHandler.handlePut(ROUTE,reqMock,resMock);

        verify(resMock,atMost(0)).status(Constants.HTTP_CREATED);
        verify(resMock,atMost(0)).status(Constants.HTTP_OK);
        verify(resMock).status(Constants.HTTP_INTERNAL_SERVER_ERROR);
    }
}
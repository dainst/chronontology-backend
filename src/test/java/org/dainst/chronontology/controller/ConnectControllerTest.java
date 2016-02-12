package org.dainst.chronontology.controller;

import org.dainst.chronontology.Constants;
import org.dainst.chronontology.store.SearchableDatastore;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Daniel M. de Oliveira
 */
public class ConnectControllerTest {

    private static final String ROUTE = "route";
    private SearchableDatastore mockDS1= mock(SearchableDatastore.class);
    private SearchableDatastore mockDS2= mock(SearchableDatastore.class);
    private Request reqMock= mock(Request.class);

    @BeforeMethod
    public void before() {
        when(reqMock.attribute(any())).thenReturn("user");
        when(reqMock.body()).thenReturn("{}");
    }


    @Test
    public void ds1ReturnFalseOnPost() throws IOException {
        when(mockDS1.put(any(),any(),any())).thenReturn(false);
        when(mockDS2.put(any(),any(),any())).thenReturn(true);
        Controller controller= new ConnectController(mockDS1,mockDS2);

        Response resMock= mock(Response.class);
        controller.handlePost(ROUTE,reqMock,resMock);
        verify(resMock).status(Constants.HTTP_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void ds2ReturnFalseOnPost() throws IOException {
        when(mockDS1.put(any(),any(),any())).thenReturn(true);
        when(mockDS2.put(any(),any(),any())).thenReturn(false);
        Controller controller= new ConnectController(mockDS1,mockDS2);

        Response resMock= mock(Response.class);
        controller.handlePost(ROUTE,reqMock,resMock);
        verify(resMock).status(Constants.HTTP_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void okOnPost() throws IOException {
        when(mockDS1.put(any(),any(),any())).thenReturn(true);
        when(mockDS2.put(any(),any(),any())).thenReturn(true);
        Controller controller= new ConnectController(mockDS1,mockDS2);

        Response resMock= mock(Response.class);
        controller.handlePost(ROUTE,reqMock,resMock);
        verify(resMock).status(Constants.HTTP_CREATED);
    }

    @Test
    public void ds1ReturnFalseOnPut() throws IOException {
        when(mockDS1.put(any(),any(),any())).thenReturn(false);
        when(mockDS2.put(any(),any(),any())).thenReturn(true);
        Controller controller= new ConnectController(mockDS1,mockDS2);

        Response resMock= mock(Response.class);
        controller.handlePut(ROUTE,reqMock,resMock);
        verify(resMock).status(Constants.HTTP_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void ds2ReturnFalseOnPut() throws IOException {
        when(mockDS1.put(any(),any(),any())).thenReturn(true);
        when(mockDS2.put(any(),any(),any())).thenReturn(false);

        Controller controller= new ConnectController(mockDS1,mockDS2);

        Response resMock= mock(Response.class);
        controller.handlePut(ROUTE,reqMock,resMock);
        verify(resMock).status(Constants.HTTP_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void okOnPut() throws IOException {
        when(mockDS1.put(any(),any(),any())).thenReturn(true);
        when(mockDS2.put(any(),any(),any())).thenReturn(true);
        Controller controller= new ConnectController(mockDS1,mockDS2);

        Response resMock= mock(Response.class);
        controller.handlePut(ROUTE,reqMock,resMock);
        verify(resMock).status(Constants.HTTP_CREATED);
    }
}

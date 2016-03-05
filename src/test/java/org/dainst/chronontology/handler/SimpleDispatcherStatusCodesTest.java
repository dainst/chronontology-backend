package org.dainst.chronontology.handler;

import org.dainst.chronontology.Constants;
import org.dainst.chronontology.handler.dispatch.Dispatcher;
import org.dainst.chronontology.handler.dispatch.SimpleDispatcher;
import org.dainst.chronontology.handler.model.RightsValidator;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.verify;

/**
 * @author Daniel M. de Oliveira
 */
public class SimpleDispatcherStatusCodesTest extends StatusCodesTest {

    private Dispatcher dispatcher = new SimpleDispatcher(mockDS1);
    private PostDocumentHandler postDocumentHandler = new PostDocumentHandler(dispatcher,new RightsValidator());
    private PutDocumentHandler putDocumentHandler = new PutDocumentHandler(dispatcher,new RightsValidator());

    @Test
    public void postOK() throws IOException {
        t(true,postDocumentHandler,Constants.HTTP_CREATED);
        verify(resMock,atMost(0)).status(Constants.HTTP_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void putOK() throws IOException {
        t(true,putDocumentHandler,Constants.HTTP_CREATED);
        verify(resMock,atMost(0)).status(Constants.HTTP_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void postNotOk() throws IOException {
        t(false,postDocumentHandler,Constants.HTTP_INTERNAL_SERVER_ERROR);
        verify(resMock,atMost(0)).status(Constants.HTTP_CREATED);
        verify(resMock,atMost(0)).status(Constants.HTTP_OK);
    }

    @Test
    public void putNotOk() throws IOException {
        t(false,putDocumentHandler,Constants.HTTP_INTERNAL_SERVER_ERROR);
        verify(resMock,atMost(0)).status(Constants.HTTP_CREATED);
        verify(resMock,atMost(0)).status(Constants.HTTP_OK);
    }
}

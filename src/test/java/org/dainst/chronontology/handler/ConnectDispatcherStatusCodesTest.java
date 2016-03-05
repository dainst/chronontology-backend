package org.dainst.chronontology.handler;

import org.dainst.chronontology.Constants;
import org.dainst.chronontology.handler.dispatch.ConnectDispatcher;
import org.dainst.chronontology.handler.dispatch.Dispatcher;
import org.dainst.chronontology.handler.model.RightsValidator;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * @author Daniel M. de Oliveira
 */
public class ConnectDispatcherStatusCodesTest extends StatusCodesTest {


    private final Dispatcher dispatcher = new ConnectDispatcher(mockDS1,mockDS2);
    private final PutDocumentHandler putDocumentHandler = new PutDocumentHandler(dispatcher,new RightsValidator());
    private final PostDocumentHandler postDocumentHandler = new PostDocumentHandler(dispatcher,new RightsValidator());

    @Test
    public void ds1ReturnFalseOnPost() throws IOException {
        t(false,true,postDocumentHandler,Constants.HTTP_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void ds2ReturnFalseOnPost() throws IOException {
        t(true,false,postDocumentHandler,Constants.HTTP_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void okOnPost() throws IOException {
        t(true,true,postDocumentHandler,Constants.HTTP_CREATED);
    }

    @Test
    public void ds1ReturnFalseOnPut() throws IOException {
        t(false,true,putDocumentHandler,Constants.HTTP_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void ds2ReturnFalseOnPut() throws IOException {
        t(true,false,putDocumentHandler,Constants.HTTP_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void okOnPut() throws IOException {
        t(true,true,putDocumentHandler,Constants.HTTP_CREATED);
    }
}

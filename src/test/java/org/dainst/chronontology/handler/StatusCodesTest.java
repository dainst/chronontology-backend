package org.dainst.chronontology.handler;

import org.dainst.chronontology.store.SearchableDatastore;
import org.testng.annotations.BeforeMethod;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Daniel M. de Oliveira
 */
public class StatusCodesTest {

    protected static final String ROUTE = "/route/";

    protected Request reqMock;
    protected Response resMock;

    protected final SearchableDatastore mockDS1= mock(SearchableDatastore.class);
    protected final SearchableDatastore mockDS2= mock(SearchableDatastore.class);

    @BeforeMethod
    public void before() {
        reqMock= mock(Request.class);
        resMock= mock(Response.class);
        when(reqMock.attribute(any())).thenReturn("user");
        when(reqMock.body()).thenReturn("{}");
        when(reqMock.pathInfo()).thenReturn(ROUTE);
        when(reqMock.params(any())).thenReturn("1");
    }

    private void prepareDSAnsers(final boolean ds1Answer,final Boolean ds2answer) {
        when(mockDS1.put(any(),any(),any())).thenReturn(ds1Answer);
        if (ds2answer!=null)
            when(mockDS2.put(any(),any(),any())).thenReturn(ds2answer);
    }

    protected void t(final boolean ds1Anwer, final Boolean ds2Answer, Handler handler, int status)
            throws IOException {

        prepareDSAnsers(ds1Anwer,ds2Answer);
        handler.handle(reqMock,resMock);
        verify(resMock).status(status);
    }

    protected void t(final boolean ds1Anwer, Handler handler, int status)
            throws IOException {

        t(ds1Anwer,null,handler,status);
    }

}

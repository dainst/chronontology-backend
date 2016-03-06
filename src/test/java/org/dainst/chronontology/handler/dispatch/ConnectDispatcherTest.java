package org.dainst.chronontology.handler.dispatch;

import org.dainst.chronontology.store.ElasticsearchDatastore;
import org.dainst.chronontology.store.FilesystemDatastore;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @author Daniel M. de Oliveira
 */
public class ConnectDispatcherTest {

    private FilesystemDatastore mockDS1;
    private ElasticsearchDatastore mockDS2;
    private ConnectDispatcher disp;

    @BeforeMethod
    public void before() {
        mockDS1= mock(FilesystemDatastore.class);
        mockDS2= mock(ElasticsearchDatastore.class);
        disp= new ConnectDispatcher(mockDS1,mockDS2);
    }

    @Test
    public void direct() {
        disp.dispatchGet("type","key",true,null);
        verify(mockDS1,atLeast(1)).get("type","key");
        verify(mockDS2,atMost(0)).get("type","key");
    }

    @Test
    public void notDirect() {
        disp.dispatchGet("type","key",false,null);
        verify(mockDS2,atLeast(1)).get("type","key");
        verify(mockDS1,atMost(0)).get("type","key");
    }

    @Test
    public void getSpecificVersion() {
        disp.dispatchGet("type","key",false,1);
        verify(mockDS1,atLeast(1)).get("type","key",1);
        verify(mockDS2,atMost(0)).get("type","key");
    }

}

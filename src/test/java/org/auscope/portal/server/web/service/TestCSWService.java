package org.auscope.portal.server.web.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.jmock.Mockery;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;
import org.auscope.portal.csw.CSWThreadExecutor;
import org.auscope.portal.csw.CSWMethodMakerGetDataRecords;
import org.auscope.portal.server.util.Util;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;

import java.util.Arrays;
import java.io.PrintWriter;

/**
 * User: Mathew Wyatt
 * Date: 20/08/2009
 * Time: 12:05:48 PM
 */
public class TestCSWService {

    /**
     * JMock context
     */
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    /**
     * Main object we are testing
     */
    private CSWService cswService;

    /**
     * Mock httpService caller
     */
    private HttpServiceCaller httpServiceCaller = context.mock(HttpServiceCaller.class);

    /**
     * Mock thread executor
     */
    private CSWThreadExecutor threadExecutor = context.mock(CSWThreadExecutor.class);

     /**
     * Mock property configurer
     */
    private PortalPropertyPlaceholderConfigurer propertyConfigurer = context.mock(PortalPropertyPlaceholderConfigurer.class);

    /**
     * Mock Util
     * @throws Exception
     */
     private Util util = new Util();//context.mock(Util.class);

    @Before
    public void setup() throws Exception {
       /* context.checking(new Expectations() {{
            //constructor gets a host property
            oneOf(propertyConfigurer).resolvePlaceholder(with(any(String.class)));

            //check that the executor was called
            oneOf(threadExecutor).execute(with(any(Runnable.class)));
        }});*/
        
        this.cswService = new CSWService(threadExecutor, httpServiceCaller, util);
        this.cswService.setServiceUrl("http://localhost");
    }

    /**
     * Test that the thread is executed
     * @throws Exception
     */
    @Test
    public void testUpdateCSWRecordsInBackground() throws Exception {
        context.checking(new Expectations() {{
            oneOf(threadExecutor).execute(with(any(Runnable.class)));
        }});

        this.cswService.updateRecordsInBackground();
    }

    /**
     * Test that the function is able to actually load CSW records
     * @throws Exception
     */
    @Test
    public void testUpdateCSWRecords() throws Exception {
        final String docString = org.auscope.portal.Util.loadXML("src/test/resources/cswRecordResponse.xml");

        context.checking(new Expectations() {{
            oneOf(httpServiceCaller).getHttpClient();
            oneOf(httpServiceCaller).callMethod(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));will(returnValue(docString));
        }});

        this.cswService.updateCSWRecords();

        //in the response we loaded from the text file it contains 53 records
        Assert.assertEquals(53, this.cswService.getDataRecords().length);
    }

    /**
     * Test we return WMS records only
     * @throws Exception
     */
    @Test
    public void testGetWMSRecords() throws Exception {
        //make sure the data records are populated
        testUpdateCSWRecords();

        //in the response we loaded from the text file it contains 6 WMS records
        Assert.assertEquals(6, this.cswService.getWMSRecords().length);
    }

    /**
     * Test we return WFS records only
     * @throws Exception
     */
    @Test
    public void testGetWFSRecords() throws Exception {
        //make sure the data records are populated
        testUpdateCSWRecords();

        //in the response we loaded from the text file it contains 41 WFS records
        Assert.assertEquals(41, this.cswService.getWFSRecords().length);
    }

    /**
     * Test we get WFS recrods based on a feature typeName
     * @throws Exception
     */
    @Test
    public void testGetWFSRecordsForTypename() throws Exception {
        //make sure the data records are populated
        testUpdateCSWRecords();

        //in the response we loaded from the text file it contains 2 er:Mine records
        Assert.assertEquals(2, this.cswService.getWFSRecordsForTypename("er:Mine").length);
    }

}

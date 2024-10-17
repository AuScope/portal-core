package org.auscope.portal.core.services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.test.BasicThreadExecutor;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.core.util.ResourceUtil;
import org.jmock.Expectations;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit tests for CSWCacheService
 *
 * @author Josh Vote
 */
public class TestCSWCacheService extends PortalTestClass {
    //determines the size of the test + congestion
    static final int CONCURRENT_THREADS_TO_RUN = 3;

    //These determine the correct numbers for a single read of the test file
    static final int RECORD_COUNT_TOTAL = 15;
    static final int RECORD_MATCH_TOTAL = 30;
    static final int RECORD_COUNT_WMS = 2;
    static final int RECORD_COUNT_WFS = 11;
    static final int RECORD_COUNT_WCS = 2;
    static final int RECORD_COUNT_WMS_WCS = 1;

    private CSWCacheService cswCacheService;
    private HttpServiceCaller httpServiceCaller = context.mock(HttpServiceCaller.class);
    private BasicThreadExecutor threadExecutor;
    
    private ElasticsearchService mockElasticsearchService = context.mock(ElasticsearchService.class);

    private static final String serviceUrlFormatString = "http://cswservice.%1$s.url/";

    /**
     * Initialises each of our unit tests with a new CSWFilterService
     */
    @Before
    public void setUp() {

        this.threadExecutor = new BasicThreadExecutor();

        //Create our service list
        ArrayList<CSWServiceItem> serviceUrlList = new ArrayList<>(CONCURRENT_THREADS_TO_RUN);
        for (int i = 0; i < CONCURRENT_THREADS_TO_RUN; i++) {
            serviceUrlList.add(new CSWServiceItem(String.format("id-%1$s", i + 1), String.format(
                    serviceUrlFormatString, i + 1)));
        }
        
        List<CSWRecord> cswRecordList = new ArrayList<>();
        cswRecordList.add(context.mock(CSWRecord.class, "mockRecord1"));
        cswRecordList.add(context.mock(CSWRecord.class, "mockRecord2"));
        cswRecordList.add(context.mock(CSWRecord.class, "mockRecord3"));
        
        context.checking(new Expectations() {
            {
            	allowing(mockElasticsearchService).getAllCSWRecords();
            	will(returnValue(cswRecordList));
            }
        });
        
        this.cswCacheService = new CSWCacheService(threadExecutor, httpServiceCaller, serviceUrlList, mockElasticsearchService);
    }

    @After
    public void tearDown() {
        this.threadExecutor = null;
        this.cswCacheService = null;
        File f1 = new File(FileIOUtil.getUserDirURL() + "id-1.ser");
        File f2 = new File(FileIOUtil.getUserDirURL() + "id-2.ser");
        File f3 = new File(FileIOUtil.getUserDirURL() + "id-3.ser");

		if (f1.exists()) {
			f1.delete();
		}
		if (f2.exists()) {
			f2.delete();
		}
		if (f3.exists()) {
			f3.delete();
		}
    }

    /**
     * Success if only a single update is able to run at any given time (Subsequent updates are terminated)
     * @throws IOException
     */
    @Test
    @Ignore
    public void testSingleUpdate() throws IOException {
        final long delay = 1000;
        final String cswResponse = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/csw/cswRecordResponse_NoMoreRecords.xml");
        context.checking(new Expectations() {
            {
            	//ignoring(mockKnownLayerService);
            	
                for (int i = 0; i < CONCURRENT_THREADS_TO_RUN; i++) {
                    oneOf(httpServiceCaller).getMethodResponseAsStream(
                            with(aHttpMethodBase(null, String.format(serviceUrlFormatString, i + 1), null)));
                    will(delayReturnValue(delay, new ByteArrayInputStream(cswResponse.getBytes())));
                }

            }
        });

        //Only one of these should trigger an update (the other should return immediately
        cswCacheService.updateCache();
        cswCacheService.updateCache();

        try {
            threadExecutor.getExecutorService().shutdown();
            threadExecutor.getExecutorService().awaitTermination(180, TimeUnit.SECONDS);
        } catch (Exception ex) {
            threadExecutor.getExecutorService().shutdownNow();
            Assert.fail("Exception whilst waiting for update to finish " + ex.getMessage());
        }
    }
}

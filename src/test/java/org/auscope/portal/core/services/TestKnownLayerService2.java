package org.auscope.portal.core.services;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.CSWOnlineResourceImpl;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.stackdriver.ServiceStatusResponse;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.view.ViewCSWRecordFactory;
import org.auscope.portal.core.view.ViewKnownLayerFactory;
import org.auscope.portal.core.view.knownlayer.KnownLayer;
import org.auscope.portal.core.view.knownlayer.KnownLayerAndRecords;
import org.auscope.portal.core.view.knownlayer.KnownLayerGrouping;
import org.auscope.portal.core.view.knownlayer.KnownLayerSelector;
import org.jmock.Expectations;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ModelMap;

/**
 * Unit tests for KnownLayerService stack driver integration
 * 
 */
public class TestKnownLayerService2 extends PortalTestClass {

    private GoogleCloudMonitoringCachedService mockStackDriverService = context.mock(GoogleCloudMonitoringCachedService.class);

    private KnownLayerService knownLayerService;

    /**
     * Build our mock layer list
     * @throws MalformedURLException 
     * @throws PortalServiceException 
     */
    @Before
    public void setup() throws MalformedURLException, PortalServiceException {
        CSWOnlineResourceImpl or1 = new CSWOnlineResourceImpl(new URL("http://host.name.1"), null, "Host Name 1", null);
        CSWOnlineResourceImpl or2 = new CSWOnlineResourceImpl(new URL("http://host.name.2"), null, "Host Name 2", null);
        CSWOnlineResourceImpl or3 = new CSWOnlineResourceImpl(new URL("http://host.name.3"), null, "Host Name 3", null);
        CSWOnlineResourceImpl or4 = new CSWOnlineResourceImpl(new URL("http://host.name.4"), null, "Host Name 4", null);


        CSWRecord mockBelongingRecord = new CSWRecord();

        mockBelongingRecord.setOnlineResources(new AbstractCSWOnlineResource[] {or1, or2, or3, or4});

        KnownLayer kl1 = new KnownLayer("kl1", new KnownLayerSelector() {
            @Override
            public RelationType isRelatedRecord(CSWRecord record) {
                return RelationType.NotRelated;
            }
        });
        KnownLayer kl2 = new KnownLayer("kl2", new KnownLayerSelector() {
            @Override
            public RelationType isRelatedRecord(CSWRecord record) {
                return RelationType.NotRelated;
            }
        });
        KnownLayer kl3 = new KnownLayer("kl3", new KnownLayerSelector() {
            @Override
            public RelationType isRelatedRecord(CSWRecord record) {
                return RelationType.NotRelated;
            }
        });
        KnownLayer kl4 = new KnownLayer("kl4", new KnownLayerSelector() {
            @Override
            public RelationType isRelatedRecord(CSWRecord record) {
                return RelationType.NotRelated;
            }
        });
        kl1.setStackdriverServiceGroup("ERML");
        kl2.setStackdriverServiceGroup("NVCL");
        kl4.setStackdriverServiceGroup("Tenements");

        List<KnownLayerAndRecords> knownLayers = Arrays.asList(
                new KnownLayerAndRecords(kl1, Arrays.asList(new CSWRecord[]{mockBelongingRecord}), new ArrayList<CSWRecord>()),
                new KnownLayerAndRecords(kl2, Arrays.asList(new CSWRecord[]{mockBelongingRecord}), new ArrayList<CSWRecord>()),
                new KnownLayerAndRecords(kl3, Arrays.asList(new CSWRecord[]{mockBelongingRecord}), new ArrayList<CSWRecord>()),
                new KnownLayerAndRecords(kl4, Arrays.asList(new CSWRecord[]{mockBelongingRecord}), new ArrayList<CSWRecord>()));
        
        KnownLayerGrouping knownLayerGroupingMock = context.mock(KnownLayerGrouping.class);
        
        final HashMap<String, List<ServiceStatusResponse>> servGroup1Response = new HashMap<String, List<ServiceStatusResponse>>();
        final HashMap<String, List<ServiceStatusResponse>> servGroup2Response = new HashMap<String, List<ServiceStatusResponse>>();

        servGroup1Response.put("host.name.1", Arrays.asList(new ServiceStatusResponse(true, "getfeatureminoccview"),
                new ServiceStatusResponse(true, "wfsgetcaps")));
        servGroup1Response.put("host.name.2", Arrays.asList(new ServiceStatusResponse(true, "getfeatureminoccview")));

        servGroup2Response.put("host.name.2", Arrays.asList(new ServiceStatusResponse(false, "getfeatureboreholeview"),
                new ServiceStatusResponse(false, "wfsgetcaps")));
        servGroup2Response.put("host.name.3", Arrays.asList(new ServiceStatusResponse(true, "getfeatureboreholeview"),
                new ServiceStatusResponse(false, "wfsgetcaps")));
        servGroup2Response.put("host.name.4", Arrays.asList(new ServiceStatusResponse(false, "getfeatureboreholeview")));

        CSWCacheService mockCacheService = context.mock(CSWCacheService.class);

        context.checking(new Expectations() {{
            oneOf(mockStackDriverService).getStatuses("ERML");will(returnValue(servGroup1Response));
            oneOf(mockStackDriverService).getStatuses("NVCL");will(returnValue(servGroup2Response));
            oneOf(mockStackDriverService).getStatuses("Tenements");will(throwException(new PortalServiceException("tenements error")));
            oneOf(knownLayerGroupingMock).getKnownLayers();will(returnValue(knownLayers));
        }});


        knownLayerService = new KnownLayerService(Arrays.asList(kl1,kl2,kl3,kl4), mockCacheService, 
                new ViewKnownLayerFactory(), new ViewCSWRecordFactory()) {
                    @Override
                    public KnownLayerGrouping groupKnownLayerRecords() {
                        return knownLayerGroupingMock;
                    }
        };
        knownLayerService.setCachedService(mockStackDriverService);
    }

    @After
    public void tearDown() {
        knownLayerService = null;
    }

    /**
     * Tests that TestBaseCSWController properly encodes stackdriver responses
     * @throws Exception
     */
    @Test
    public void testStackdriverErrorHandling() throws Exception {

        knownLayerService.updateKnownLayersCache();
        List<ModelMap> data = knownLayerService.getKnownLayersCache();
        Assert.assertEquals(4, data.size());

        //servGroup1
        Assert.assertFalse(data.get(0).containsKey("stackdriverFailingHosts"));
        //servGroup2
        Assert.assertTrue(data.get(1).containsKey("stackdriverFailingHosts"));
        @SuppressWarnings("unchecked")
        List<String> failingHosts = (List<String>) data.get(1).get("stackdriverFailingHosts");
        Assert.assertEquals(3, failingHosts.size());
        Assert.assertEquals("host.name.2", failingHosts.get(0));
        Assert.assertEquals("host.name.3", failingHosts.get(1));
        Assert.assertEquals("host.name.4", failingHosts.get(2));
        //servGroup3
        Assert.assertFalse(data.get(2).containsKey("stackdriverFailingHosts"));
        //servGroup4
        Assert.assertFalse(data.get(3).containsKey("stackdriverFailingHosts"));
    }

}

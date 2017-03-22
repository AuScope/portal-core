package org.auscope.portal.core.server.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.auscope.portal.core.services.Nagios4CachedService;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.nagios.ServiceStatusResponse;
import org.auscope.portal.core.services.responses.nagios.ServiceStatusResponse.Status;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.view.ViewCSWRecordFactory;
import org.auscope.portal.core.view.ViewKnownLayerFactory;
import org.auscope.portal.core.view.knownlayer.KnownLayer;
import org.auscope.portal.core.view.knownlayer.KnownLayerAndRecords;
import org.auscope.portal.core.view.knownlayer.KnownLayerSelector;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

/**
 * Unit tests for BaseCSWController
 *
 * @author Josh Vote
 *
 */
public class TestBaseCSWController extends PortalTestClass {

    private class BaseCSWControllerImpl extends BaseCSWController {
        public BaseCSWControllerImpl(ViewCSWRecordFactory viewCSWRecordFactory,
                ViewKnownLayerFactory viewKnownLayerFactory) {
            super(viewCSWRecordFactory, viewKnownLayerFactory);
        }
    }

    private BaseCSWControllerImpl baseController = new BaseCSWControllerImpl(new ViewCSWRecordFactory(), new ViewKnownLayerFactory());
    private Nagios4CachedService mockNagiosService = context.mock(Nagios4CachedService.class);

    /**
     * Tests that TestBaseCSWController properly encodes nagios responses
     * @throws Exception
     */
    @Test
    public void testNagiosErrorHandling() throws Exception {
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
        kl1.setNagiosHostGroup("hg1");
        kl2.setNagiosHostGroup("hg2");
        kl4.setNagiosHostGroup("hg4");
        List<KnownLayerAndRecords> knownLayers = Arrays.asList(
                new KnownLayerAndRecords(kl1, new ArrayList<CSWRecord>(), new ArrayList<CSWRecord>()),
                new KnownLayerAndRecords(kl2, new ArrayList<CSWRecord>(), new ArrayList<CSWRecord>()),
                new KnownLayerAndRecords(kl3, new ArrayList<CSWRecord>(), new ArrayList<CSWRecord>()),
                new KnownLayerAndRecords(kl4, new ArrayList<CSWRecord>(), new ArrayList<CSWRecord>()));

        final HashMap<String, List<ServiceStatusResponse>> hg1Response = new HashMap<String, List<ServiceStatusResponse>>();
        final HashMap<String, List<ServiceStatusResponse>> hg2Response = new HashMap<String, List<ServiceStatusResponse>>();

        hg1Response.put("host.name.1", Arrays.asList(new ServiceStatusResponse(Status.ok, "hg1.serv1"), new ServiceStatusResponse(Status.ok, "hg1.serv2")));
        hg1Response.put("host.name.2", Arrays.asList(new ServiceStatusResponse(Status.ok, "hg1.serv3")));

        hg2Response.put("host.name.3", Arrays.asList(new ServiceStatusResponse(Status.ok, "hg2.serv1"), new ServiceStatusResponse(Status.warning, "hg2.serv2")));
        hg2Response.put("host.name.4", Arrays.asList(new ServiceStatusResponse(Status.critical, "hg2.serv3"), new ServiceStatusResponse(Status.critical, "hg2.serv4")));

        context.checking(new Expectations() {{
            oneOf(mockNagiosService).getStatuses("hg1");will(returnValue(hg1Response));
            oneOf(mockNagiosService).getStatuses("hg2");will(returnValue(hg2Response));
            oneOf(mockNagiosService).getStatuses("hg4");will(throwException(new PortalServiceException("hg4 error")));
        }});

        ModelAndView mav = baseController.generateKnownLayerResponse(knownLayers, mockNagiosService);
        List<ModelMap> data = (List<ModelMap>) mav.getModelMap().get("data");
        Assert.assertEquals(4, data.size());

        Assert.assertFalse(data.get(0).containsKey("nagiosFailingHosts"));
        Assert.assertTrue(data.get(1).containsKey("nagiosFailingHosts"));
        List<String> failingHosts = (List<String>) data.get(1).get("nagiosFailingHosts");
        Assert.assertEquals(2, failingHosts.size());
        Assert.assertEquals("host.name.3", failingHosts.get(0));
        Assert.assertEquals("host.name.4", failingHosts.get(1));
        Assert.assertFalse(data.get(2).containsKey("nagiosFailingHosts"));
        Assert.assertFalse(data.get(3).containsKey("nagiosFailingHosts"));
    }


}

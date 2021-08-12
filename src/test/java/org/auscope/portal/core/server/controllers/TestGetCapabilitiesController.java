package org.auscope.portal.core.server.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.auscope.portal.core.services.WMSService;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesRecord;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesRecord_1_1_1;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesRecord_1_3_0;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.ResourceUtil;
import org.auscope.portal.core.view.ViewCSWRecordFactory;
import org.auscope.portal.core.view.ViewGetCapabilitiesFactory;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

public class TestGetCapabilitiesController extends PortalTestClass {

    private ViewCSWRecordFactory viewCswFactory = context.mock(ViewCSWRecordFactory.class);
    private ViewGetCapabilitiesFactory viewCapabilityFactory = context.mock(ViewGetCapabilitiesFactory.class);
    private WMSService service;
    private WMSController controller;

    @Before
    public void setUp() {
        service = context.mock(WMSService.class);

        controller = new WMSController(service, viewCswFactory, viewCapabilityFactory, null);
    }

    @Test
    public void testGetCustomLayers() throws Exception {
        //GetCapabilititesControllerWMSResponse.xml
        final String serviceUrl = "http://example.com";
        InputStream is = ResourceUtil
                .loadResourceAsStream("org/auscope/portal/core/test/responses/wms/GetCapabilitiesControllerWMSResponse_1_1_1.xml");
        try {
            final GetCapabilitiesRecord record = new GetCapabilitiesRecord_1_1_1(is);

            context.checking(new Expectations() {
                {
                    oneOf(service).getWmsCapabilities(serviceUrl, null);
                    will(returnValue(record));

                    exactly(21).of(viewCswFactory).toView(with(any(CSWRecord.class)));
                    will(returnValue(new ModelMap()));

                    exactly(1).of(viewCapabilityFactory).toView(with(any(GetCapabilitiesRecord.class)));
                    will(returnValue(new ModelMap()));
                }
            });

            Assert.assertNotNull(is);
            ModelAndView modelAndView = controller.getCustomLayers(serviceUrl,"Y");
            Assert.assertNotNull(modelAndView);
            Assert.assertTrue((Boolean) modelAndView.getModel().get("success"));

            ModelMap mMap = (ModelMap) modelAndView.getModel().get("data");
            List ls = (List) mMap.get("cswRecords");
            Assert.assertEquals(21, ls.size());
            ls = (List) mMap.get("capabilityRecords");
            Assert.assertEquals(1, ls.size());

        } finally {
            try {
                is.close();
            } catch (IOException e) {
                //Not important if the stream can't be closed in unit test
                e.printStackTrace();
            }
        }
    }

    @Test
    public void test_1_3_0_GetCustomLayers() throws Exception {
        //GetCapabilititesControllerWMSResponse.xml
        final String serviceUrl = "http://example.com";
        InputStream is = ResourceUtil
                .loadResourceAsStream("org/auscope/portal/core/test/responses/wms/GetCapabilitiesControllerWMSResponse_1_3_0.xml");
        try {
            final GetCapabilitiesRecord record = new GetCapabilitiesRecord_1_3_0(is);

            context.checking(new Expectations() {
                {
                    oneOf(service).getWmsCapabilities(serviceUrl, null);
                    will(returnValue(record));

                    exactly(1).of(viewCswFactory).toView(with(any(CSWRecord.class)));
                    will(returnValue(new ModelMap()));

                    exactly(1).of(viewCapabilityFactory).toView(with(any(GetCapabilitiesRecord.class)));
                    will(returnValue(new ModelMap()));
                }
            });

            Assert.assertNotNull(is);
            ModelAndView modelAndView = controller.getCustomLayers(serviceUrl,"Y");
            Assert.assertNotNull(modelAndView);
            Assert.assertTrue((Boolean) modelAndView.getModel().get("success"));

            ModelMap mMap = (ModelMap) modelAndView.getModel().get("data");
            List ls = (List) mMap.get("cswRecords");
            Assert.assertEquals(1, ls.size());
            ls = (List) mMap.get("capabilityRecords");
            Assert.assertEquals(1, ls.size());

        } finally {
            try {
                is.close();
            } catch (IOException e) {
                //Not important if the stream can't be closed in unit test
                e.printStackTrace();
            }
        }
    }
}

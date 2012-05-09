package org.auscope.portal.server.web.controllers;

import java.util.ArrayList;
import java.util.List;

import org.auscope.portal.PortalTestClass;
import org.auscope.portal.server.domain.geodesy.GeodesyObservation;
import org.auscope.portal.server.web.service.GeodesyService;
import org.auscope.portal.server.web.service.PortalServiceException;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

public class TestGeodesyController extends PortalTestClass {
    private GeodesyService mockGeodesyService = context.mock(GeodesyService.class);
    private GeodesyController controller;

    @Before
    public void init() {
        controller = new GeodesyController(mockGeodesyService);
    }

    /**
     * Tests a successful request
     */
    @Test
    public void testSuccessRequest() throws Exception {
        final String serviceUrl = "http://example.com/wfs";
        final String stationId = "statioNid";
        final String startDate = "1986-10-09";
        final String endDate = "1990-12-13";

        final List<GeodesyObservation> result = new ArrayList<GeodesyObservation>();

        context.checking(new Expectations() {{
            oneOf(mockGeodesyService).getObservationsForStation(serviceUrl, stationId, startDate, endDate);will(returnValue(result));
        }});

        ModelAndView mav = controller.getGeodesyObservations(stationId, startDate, endDate, serviceUrl);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModelMap().get("success"));
        Assert.assertSame(result,  mav.getModelMap().get("data"));
    }

    /**
     * Tests a successful request
     */
    @Test
    public void testFailRequest() throws Exception {
        final String serviceUrl = "http://example.com/wfs";
        final String stationId = "statioNid";
        final String startDate = "1986-10-09";
        final String endDate = "1990-12-13";

        final List<GeodesyObservation> result = new ArrayList<GeodesyObservation>();

        context.checking(new Expectations() {{
            oneOf(mockGeodesyService).getObservationsForStation(serviceUrl, stationId, startDate, endDate);will(throwException(new PortalServiceException(null)));
        }});

        ModelAndView mav = controller.getGeodesyObservations(stationId, startDate, endDate, serviceUrl);
        Assert.assertNotNull(mav);
        Assert.assertFalse((Boolean) mav.getModelMap().get("success"));
    }
}

package org.auscope.portal.server.web.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.auscope.portal.PortalTestClass;
import org.auscope.portal.server.domain.admin.AdminDiagnosticResponse;
import org.auscope.portal.server.domain.admin.EndpointAndSelector;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.web.service.AdminService;
import org.auscope.portal.server.web.service.CSWServiceItem;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * Unit tests for AdminController
 * @author Josh Vote
 *
 */
public class TestAdminController extends PortalTestClass {

    private AdminService mockService;
    private CSWServiceItem mockServiceItem;
    private ArrayList cswServiceList;
    private PortalPropertyPlaceholderConfigurer mockProperties;
    private AdminController controller;

    @Before
    public void setup() {
        mockService = context.mock(AdminService.class);
        mockServiceItem = context.mock(CSWServiceItem.class);
        mockProperties = context.mock(PortalPropertyPlaceholderConfigurer.class);
        cswServiceList = new ArrayList();
        cswServiceList.add(mockServiceItem);

        controller = new AdminController(cswServiceList, mockProperties, mockService);
    }

    /**
     * Tests the vocab diagnostic test is initialised succesfully
     * @throws Exception
     */
    @Test
    public void testVocab() throws Exception {
        final String vocabUrl = "http://fake.vocab/url";
        final AdminDiagnosticResponse response = new AdminDiagnosticResponse();

        context.checking(new Expectations() {{
            oneOf(mockProperties).resolvePlaceholder("HOST.vocabService.url");
            will(returnValue(vocabUrl));

            oneOf(mockService).vocabConnectivity(vocabUrl);
            will(returnValue(response));
        }});

        ModelAndView mav = controller.testVocabulary();
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
    }

    /**
     * Tests that duplicate WFS + typename combinations are removed before calling the admin service
     * @throws Exception
     */
    @Test
    public void testDuplicateWFSServices() throws Exception {
        final String[] serviceUrls = new String[] {"a", "b", "a", "b", "c", "c"};
        final String[] typeNames = new String[] {"1", "1", "2", "1", "2", "2"};
        final String bboxJson = "{\"crs\":\"EPSG:4326\",\"eastBoundLongitude\":160,\"westBoundLongitude\":110,\"southBoundLatitude\":-47,\"northBoundLatitude\":-3}";
        final List<EndpointAndSelector> expected = new ArrayList<EndpointAndSelector>();
        final AdminDiagnosticResponse response = new AdminDiagnosticResponse();


        //We lose #3 and #5 to duplicates
        expected.addAll(Arrays.asList(
            new EndpointAndSelector(serviceUrls[0], typeNames[0]),
            new EndpointAndSelector(serviceUrls[1], typeNames[1]),
            new EndpointAndSelector(serviceUrls[2], typeNames[2]),
            new EndpointAndSelector(serviceUrls[4], typeNames[4])));


        context.checking(new Expectations() {{
            oneOf(mockService).wfsConnectivity(with(equal(expected)), with(any(FilterBoundingBox.class)));will(returnValue(response));
        }});

        ModelAndView mav = controller.testWFS(serviceUrls, typeNames, bboxJson);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
    }

    /**
     * Tests that duplicate WMS + layer name combinations are removed before calling the admin service
     * @throws Exception
     */
    @Test
    public void testDuplicateWMSServices() throws Exception {
        final String[] serviceUrls = new String[] {"a", "b", "a", "b", "c", "c"};
        final String[] layerNames = new String[] {"1", "1", "2", "1", "2", "2"};
        final String bboxJson = "{\"crs\":\"EPSG:4326\",\"eastBoundLongitude\":160,\"westBoundLongitude\":110,\"southBoundLatitude\":-47,\"northBoundLatitude\":-3}";
        final List<EndpointAndSelector> expected = new ArrayList<EndpointAndSelector>();
        final AdminDiagnosticResponse response = new AdminDiagnosticResponse();


        //We lose #3 and #5 to duplicates
        expected.addAll(Arrays.asList(
            new EndpointAndSelector(serviceUrls[0], layerNames[0]),
            new EndpointAndSelector(serviceUrls[1], layerNames[1]),
            new EndpointAndSelector(serviceUrls[2], layerNames[2]),
            new EndpointAndSelector(serviceUrls[4], layerNames[4])));


        context.checking(new Expectations() {{
            oneOf(mockService).wmsConnectivity(with(equal(expected)), with(any(FilterBoundingBox.class)));will(returnValue(response));
        }});

        ModelAndView mav = controller.testWMS(serviceUrls, layerNames, bboxJson);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
    }
}

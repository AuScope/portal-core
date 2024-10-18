package org.auscope.portal.core.server.controllers;

import java.net.URI;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.services.WFSService;
import org.auscope.portal.core.services.responses.wfs.WFSResponse;
import org.auscope.portal.core.services.responses.wfs.WFSTransformedResponse;
import org.auscope.portal.core.test.ByteBufferedServletOutputStream;
import org.auscope.portal.core.test.PortalTestClass;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

/**
 * Unit tests for WFSController
 *
 * @author Matthew Wyatt
 * @author Josh Vote
 */
public class TestWfsController extends PortalTestClass {

    /**
     * The controller to test
     */
    private WFSController wfsController;

    private HttpRequestBase mockMethod = context.mock(HttpRequestBase.class);

    private WFSService mockWfsService = context.mock(WFSService.class);

    private HttpServletResponse mockResponse = context.mock(HttpServletResponse.class);

    private HttpServletRequest mockRequest = context.mock(HttpServletRequest.class);

    @Before
    public void setUp() {
        wfsController = new WFSController(mockWfsService);
    }

    /**
     * Test that all classes are invoked correctly and return valid JSON
     */
    @Test
    public void testGetAllFeatures() throws Exception {
        final String gmlBlob = "gmlBlob";
        final String wfsUrl = "http://service/wfs";
        final String featureType = "type:name";
        final int maxFeatures = 1234;
        final String srs = null; //dont specify this
        final String bboxJsonString = null;

        context.checking(new Expectations() {
            {
                oneOf(mockWfsService).getWfsResponse(with(equal(wfsUrl)), with(equal(featureType)),
                        with(any(String.class)), with(equal(maxFeatures)), with(equal(srs)));
                will(returnValue(new WFSResponse(gmlBlob, mockMethod)));

                allowing(mockMethod).getURI();
                will(returnValue(new URI("http://service.wfs/wfs")));
            }
        });

        ModelAndView modelAndView = wfsController.requestAllFeatures(wfsUrl, featureType, bboxJsonString, maxFeatures);
        ModelMap dataObj = (ModelMap) modelAndView.getModel().get("data");
        Assert.assertTrue((Boolean) modelAndView.getModel().get("success"));
        Assert.assertNotNull(dataObj);
        Assert.assertEquals(gmlBlob, dataObj.get("gml"));
    }

    @Test
    public void testGetAllFeaturesInBbox() throws Exception {
        final String gmlBlob = "gmlBlob";
        final String wfsUrl = "http://service/wfs";
        final String featureType = "type:name";
        final int maxFeatures = 1234;
        final String srs = null; //dont specify this
        final String bboxJsonString = "{\"bboxSrs\":\"http://www.opengis.net/gml/srs/epsg.xml%234326\",\"lowerCornerPoints\":[-5,-6],\"upperCornerPoints\":[7,8]}";

        context.checking(new Expectations() {
            {
                oneOf(mockWfsService).getWfsResponse(with(equal(wfsUrl)), with(equal(featureType)),
                        with(any(String.class)), with(equal(maxFeatures)), with(equal(srs)));
                will(returnValue(new WFSResponse(gmlBlob, mockMethod)));

                allowing(mockMethod).getURI();
                will(returnValue(new URI("http://service.wfs/wfs")));
            }
        });

        ModelAndView modelAndView = wfsController.requestAllFeatures(wfsUrl, featureType, bboxJsonString, maxFeatures);
        ModelMap dataObj = (ModelMap) modelAndView.getModel().get("data");
        Assert.assertTrue((Boolean) modelAndView.getModel().get("success"));
        Assert.assertNotNull(dataObj);
        Assert.assertEquals(gmlBlob, dataObj.get("gml"));
    }

    @Test
    public void testRequestFeature() throws Exception {
        final String gmlBlob = "gmlBlob";
        final String wfsUrl = "http://service/wfs";
        final String featureType = "type:name";
        final String featureId = "feature-id";

        context.checking(new Expectations() {
            {
                oneOf(mockWfsService).getWfsResponse(wfsUrl, featureType, featureId);
                will(returnValue(new WFSResponse(gmlBlob, mockMethod)));

                allowing(mockMethod).getURI();
                will(returnValue(new URI("http://service.wfs/wfs")));
            }
        });

        ModelAndView modelAndView = wfsController.requestFeature(wfsUrl, featureType, featureId);
        ModelMap dataObj = (ModelMap) modelAndView.getModel().get("data");
        Assert.assertTrue((Boolean) modelAndView.getModel().get("success"));
        Assert.assertNotNull(dataObj);
        Assert.assertEquals(gmlBlob, dataObj.get("gml"));
    }

    /**
     * Test wfsFeaturePopup will all optional parameters specified
     *
     * @throws Exception
     */
    @Test
    public void testWFSFeaturePopup() throws Exception {
        final String serviceUrl = "http://example.com/wfs";
        final String typeName = "wfs:typeName";
        final String featureId = "idString";
        final String convertedData = "gmlToKMLResult";
        final String wfsResponse = "wfsResponseString";
        final String baseUrl = "http://portal.org/api";
        final ByteBufferedServletOutputStream outputStream = new ByteBufferedServletOutputStream(
                convertedData.getBytes().length);

        context.checking(new Expectations() {
            {
                allowing(mockRequest).getRequestURL();
                will(returnValue(new StringBuffer("http://portal.org/api/wfsFeaturePopup.do")));
                allowing(mockResponse).setContentType(with(any(String.class)));

                oneOf(mockWfsService).getWfsResponseAsHtml(serviceUrl, typeName, featureId, baseUrl);
                will(returnValue(new WFSTransformedResponse(wfsResponse, convertedData, mockMethod)));

                oneOf(mockResponse).getOutputStream();
                will(returnValue(outputStream));
            }
        });

        wfsController.wfsFeaturePopup(mockRequest, mockResponse, serviceUrl, typeName, featureId);

        Assert.assertArrayEquals(convertedData.getBytes(), outputStream.toByteArray());
    }

    /**
     * Tests wfsFeaturePopup with only a URL
     *
     * @throws Exception
     */
    @Test
    public void testWFSFeaturePopupUrlOnly() throws Exception {
        final String serviceUrl = "http://example.com/wfs";
        final String typeName = null;
        final String featureId = null;
        final String convertedData = "gmlToKMLResult";
        final String wfsResponse = "wfsResponseString";
        final String baseUrl = "http://portal.org/api";
        final ByteBufferedServletOutputStream outputStream = new ByteBufferedServletOutputStream(
                convertedData.getBytes().length);

        context.checking(new Expectations() {
            {
                allowing(mockResponse).setContentType(with(any(String.class)));

                allowing(mockRequest).getRequestURL();
                will(returnValue(new StringBuffer("http://portal.org/api/wfsFeaturePopup.do")));

                oneOf(mockWfsService).getWfsResponseAsHtml(serviceUrl, baseUrl);
                will(returnValue(new WFSTransformedResponse(wfsResponse, convertedData, mockMethod)));

                oneOf(mockResponse).getOutputStream();
                will(returnValue(outputStream));
            }
        });

        wfsController.wfsFeaturePopup(mockRequest, mockResponse, serviceUrl, typeName, featureId);

        Assert.assertArrayEquals(convertedData.getBytes(), outputStream.toByteArray());
    }
}

package org.auscope.portal.core.server.controllers;

import java.net.URI;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.controllers.WFSController;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.methodmakers.filter.SimplePropertyFilter;
import org.auscope.portal.core.services.responses.wfs.WFSCountResponse;
import org.auscope.portal.core.services.responses.wfs.WFSResponse;
import org.auscope.portal.core.services.responses.wfs.WFSTransformedResponse;
import org.auscope.portal.core.test.ByteBufferedServletOutputStream;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.services.WFSService;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

/**
 * Unit tests for GSMLController
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

    @Test
    public void testRequestFeatureByProperty() throws Exception {
        final String gmlBlob = "gmlBlob";
        final String wfsUrl = "http://service/wfs";
        final String featureType = "type:name";
        final String featureProperty = "feature/property";
        final String propertyValue = "comparison value";

        final String filterString = new SimplePropertyFilter(featureProperty, propertyValue)
                .getFilterStringAllRecords();

        context.checking(new Expectations() {
            {
                oneOf(mockWfsService).getWfsResponse(wfsUrl, featureType, filterString, null, null);
                will(returnValue(new WFSResponse(gmlBlob, mockMethod)));

                allowing(mockMethod).getURI();
                will(returnValue(new URI("http://service.wfs/wfs")));
            }
        });

        ModelAndView modelAndView = wfsController.requestFeatureByProperty(wfsUrl, featureType, featureProperty,
                propertyValue);
        ModelMap dataObj = (ModelMap) modelAndView.getModel().get("data");
        Assert.assertTrue((Boolean) modelAndView.getModel().get("success"));
        Assert.assertNotNull(dataObj);
        Assert.assertEquals(gmlBlob, dataObj.get("gml"));
    }

    /**
     * Tests get feature count works as expected
     *
     * @throws Exception
     */
    @Test
    public void testGetFeatureCount() throws Exception {
        final String wfsUrl = "http://service/wfs";
        final String featureType = "type:name";
        final String bboxJsonString = "{\"bboxSrs\":\"http://www.opengis.net/gml/srs/epsg.xml%234326\",\"lowerCornerPoints\":[-5,-6],\"upperCornerPoints\":[7,8]}";
        final int maxFeatures = 12315;
        final int featureCount = 21;

        context.checking(new Expectations() {
            {
                oneOf(mockWfsService).getWfsFeatureCount(with(equal(wfsUrl)), with(equal(featureType)),
                        with(any(String.class)), with(equal(maxFeatures)), with(equal((String) null)));
                will(returnValue(new WFSCountResponse(featureCount)));
            }
        });
        ModelAndView modelAndView = wfsController.requestFeatureCount(wfsUrl, featureType, bboxJsonString, maxFeatures);
        Integer dataObj = (Integer) modelAndView.getModel().get("data");
        Assert.assertTrue((Boolean) modelAndView.getModel().get("success"));
        Assert.assertNotNull(dataObj);
        Assert.assertEquals(new Integer(featureCount), dataObj);
    }

    /**
     * Tests get feature count works as expected
     *
     * @throws Exception
     */
    @Test
    public void testGetFeatureCountException() throws Exception {
        final String wfsUrl = "http://service/wfs";
        final String featureType = "type:name";
        final String bboxJsonString = "{\"bboxSrs\":\"http://www.opengis.net/gml/srs/epsg.xml%234326\",\"lowerCornerPoints\":[-5,-6],\"upperCornerPoints\":[7,8]}";
        final int maxFeatures = 12315;

        context.checking(new Expectations() {
            {
                oneOf(mockWfsService).getWfsFeatureCount(with(equal(wfsUrl)), with(equal(featureType)),
                        with(any(String.class)), with(equal(maxFeatures)), with(equal((String) null)));
                will(throwException(new PortalServiceException("")));
            }
        });
        ModelAndView modelAndView = wfsController.requestFeatureCount(wfsUrl, featureType, bboxJsonString, maxFeatures);
        Assert.assertFalse((Boolean) modelAndView.getModel().get("success"));
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
        final ByteBufferedServletOutputStream outputStream = new ByteBufferedServletOutputStream(
                convertedData.getBytes().length);

        context.checking(new Expectations() {
            {
                allowing(mockMethod).getURI();
                will(returnValue(new URI(serviceUrl)));

                allowing(mockResponse).setContentType(with(any(String.class)));

                oneOf(mockWfsService).getWfsResponseAsHtml(serviceUrl, typeName, featureId);
                will(returnValue(new WFSTransformedResponse(wfsResponse, convertedData, mockMethod)));

                oneOf(mockResponse).getOutputStream();
                will(returnValue(outputStream));
            }
        });

        wfsController.wfsFeaturePopup(mockResponse, serviceUrl, typeName, featureId);

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
        final ByteBufferedServletOutputStream outputStream = new ByteBufferedServletOutputStream(
                convertedData.getBytes().length);

        context.checking(new Expectations() {
            {
                allowing(mockResponse).setContentType(with(any(String.class)));

                oneOf(mockWfsService).getWfsResponseAsHtml(serviceUrl);
                will(returnValue(new WFSTransformedResponse(wfsResponse, convertedData, mockMethod)));

                oneOf(mockResponse).getOutputStream();
                will(returnValue(outputStream));
            }
        });

        wfsController.wfsFeaturePopup(mockResponse, serviceUrl, typeName, featureId);

        Assert.assertArrayEquals(convertedData.getBytes(), outputStream.toByteArray());
    }
}

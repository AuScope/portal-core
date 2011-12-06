package org.auscope.portal.server.web.controllers;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.URI;
import org.auscope.portal.PortalTestClass;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.domain.filter.IFilter;
import org.auscope.portal.server.domain.wfs.WFSCountResponse;
import org.auscope.portal.server.domain.wfs.WFSKMLResponse;
import org.auscope.portal.server.web.service.WFSService;
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
public class TestGSMLController extends PortalTestClass {

    /**
     * The controller to test
     */
    private GSMLController gsmlController;


    private HttpMethodBase mockMethod = context.mock(HttpMethodBase.class);

    private WFSService mockWfsService = context.mock(WFSService.class);
    private IFilter mockFilter = context.mock(IFilter.class);

    @Before
    public void setUp() {
        gsmlController = new GSMLController(mockWfsService, mockFilter);
    }

    /**
     * Test that all classes are invoked correctly and return valid JSON
     */
    @Test
    public void testGetAllFeatures() throws Exception {
        final String gmlBlob = "gmlBlob";
        final String kmlBlob = "kmlBlob";
        final String filterString = "filterStr";
        final String wfsUrl = "http://service/wfs";
        final String featureType = "type:name";
        final int maxFeatures = 1234;
        final String srs = null; //dont specify this
        final String bboxJsonString = null;

        context.checking(new Expectations() {{
            oneOf(mockWfsService).getWfsResponseAsKml(wfsUrl, featureType, filterString, maxFeatures, srs);will(returnValue(new WFSKMLResponse(gmlBlob, kmlBlob, mockMethod)));

            oneOf(mockFilter).getFilterStringAllRecords(); will(returnValue(filterString));

            allowing(mockMethod).getURI();will(returnValue(new URI("http://service.wfs/wfs", false)));
        }});

        ModelAndView modelAndView = gsmlController.requestAllFeatures(wfsUrl, featureType, bboxJsonString, maxFeatures);
        ModelMap dataObj = (ModelMap) modelAndView.getModel().get("data");
        Assert.assertTrue((Boolean) modelAndView.getModel().get("success"));
        Assert.assertNotNull(dataObj);
        Assert.assertEquals(gmlBlob, dataObj.get("gml"));
        Assert.assertEquals(kmlBlob, dataObj.get("kml"));
    }

    @Test
    public void testGetAllFeaturesInBbox() throws Exception {
        final String gmlBlob = "gmlBlob";
        final String kmlBlob = "kmlBlob";
        final String filterString = "filterStr";
        final String wfsUrl = "http://service/wfs";
        final String featureType = "type:name";
        final int maxFeatures = 1234;
        final String srs = null; //dont specify this
        final String bboxJsonString = "{\"bboxSrs\":\"http://www.opengis.net/gml/srs/epsg.xml%234326\",\"lowerCornerPoints\":[-5,-6],\"upperCornerPoints\":[7,8]}";

        context.checking(new Expectations() {{
            oneOf(mockWfsService).getWfsResponseAsKml(wfsUrl, featureType, filterString, maxFeatures, srs);will(returnValue(new WFSKMLResponse(gmlBlob, kmlBlob, mockMethod)));

            oneOf(mockFilter).getFilterStringBoundingBox(with(any(FilterBoundingBox.class))); will(returnValue(filterString));

            allowing(mockMethod).getURI();will(returnValue(new URI("http://service.wfs/wfs", false)));
        }});

        ModelAndView modelAndView = gsmlController.requestAllFeatures(wfsUrl, featureType, bboxJsonString, maxFeatures);
        ModelMap dataObj = (ModelMap) modelAndView.getModel().get("data");
        Assert.assertTrue((Boolean) modelAndView.getModel().get("success"));
        Assert.assertNotNull(dataObj);
        Assert.assertEquals(gmlBlob, dataObj.get("gml"));
        Assert.assertEquals(kmlBlob, dataObj.get("kml"));
    }

    @Test
    public void testRequestFeature() throws Exception {
        final String gmlBlob = "gmlBlob";
        final String kmlBlob = "kmlBlob";
        final String wfsUrl = "http://service/wfs";
        final String featureType = "type:name";
        final String featureId = "feature-id";

        context.checking(new Expectations() {{
            oneOf(mockWfsService).getWfsResponseAsKml(wfsUrl, featureType, featureId);will(returnValue(new WFSKMLResponse(gmlBlob, kmlBlob, mockMethod)));

            allowing(mockMethod).getURI();will(returnValue(new URI("http://service.wfs/wfs", false)));
        }});

        ModelAndView modelAndView = gsmlController.requestFeature(wfsUrl, featureType, featureId);
        ModelMap dataObj = (ModelMap) modelAndView.getModel().get("data");
        Assert.assertTrue((Boolean) modelAndView.getModel().get("success"));
        Assert.assertNotNull(dataObj);
        Assert.assertEquals(gmlBlob, dataObj.get("gml"));
        Assert.assertEquals(kmlBlob, dataObj.get("kml"));
    }

    /**
     * Tests get feature count works as expected
     * @throws Exception
     */
    @Test
    public void testGetFeatureCount() throws Exception {
        final String wfsUrl = "http://service/wfs";
        final String featureType = "type:name";
        final String filterString = "fake-filter-string";
        final String bboxJsonString = "{\"bboxSrs\":\"http://www.opengis.net/gml/srs/epsg.xml%234326\",\"lowerCornerPoints\":[-5,-6],\"upperCornerPoints\":[7,8]}";
        final int maxFeatures = 12315;
        final int featureCount = 21;

        context.checking(new Expectations() {{
            oneOf(mockFilter).getFilterStringBoundingBox(with(any(FilterBoundingBox.class))); will(returnValue(filterString));
            oneOf(mockWfsService).getWfsFeatureCount(wfsUrl, featureType, filterString, maxFeatures);will(returnValue(new WFSCountResponse(featureCount)));
        }});
        ModelAndView modelAndView = gsmlController.requestFeatureCount(wfsUrl, featureType, bboxJsonString, maxFeatures);
        Integer dataObj = (Integer) modelAndView.getModel().get("data");
        Assert.assertTrue((Boolean) modelAndView.getModel().get("success"));
        Assert.assertNotNull(dataObj);
        Assert.assertEquals(new Integer(featureCount), dataObj);
    }

    /**
     * Tests get feature count works as expected
     * @throws Exception
     */
    @Test
    public void testGetFeatureCountException() throws Exception {
        final String wfsUrl = "http://service/wfs";
        final String featureType = "type:name";
        final String filterString = "fake-filter-string";
        final String bboxJsonString = "{\"bboxSrs\":\"http://www.opengis.net/gml/srs/epsg.xml%234326\",\"lowerCornerPoints\":[-5,-6],\"upperCornerPoints\":[7,8]}";
        final int maxFeatures = 12315;
        final int featureCount = 21;

        context.checking(new Expectations() {{
            oneOf(mockFilter).getFilterStringBoundingBox(with(any(FilterBoundingBox.class))); will(returnValue(filterString));

            oneOf(mockWfsService).getWfsFeatureCount(wfsUrl, featureType, filterString, maxFeatures);will(returnValue(new WFSCountResponse(featureCount)));
        }});
        ModelAndView modelAndView = gsmlController.requestFeatureCount(wfsUrl, featureType, bboxJsonString, maxFeatures);
        Integer dataObj = (Integer) modelAndView.getModel().get("data");
        Assert.assertTrue((Boolean) modelAndView.getModel().get("success"));
        Assert.assertNotNull(dataObj);
        Assert.assertEquals(new Integer(featureCount), dataObj);
    }
}

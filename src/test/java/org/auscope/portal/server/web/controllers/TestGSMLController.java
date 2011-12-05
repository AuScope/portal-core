package org.auscope.portal.server.web.controllers;

import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.URI;
import org.auscope.portal.gsml.GSMLResponseHandler;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.domain.filter.IFilter;
import org.auscope.portal.server.domain.wfs.WFSKMLResponse;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.web.service.WFSService;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
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
public class TestGSMLController {

    /**
     * JMock context
     */
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    /**
     * The controller to test
     */
    private GSMLController gsmlController;

    /**
     * Mock request
     */
    private HttpServletRequest mockHttpRequest = context.mock(HttpServletRequest.class);

    /**
     * Mock session
     */
    private HttpSession mockHttpSession = context.mock(HttpSession.class);


    private HttpMethodBase mockMethod = context.mock(HttpMethodBase.class);

    /**
     * Mock session
     */
    private ServletContext mockServletContext = context.mock(ServletContext.class);

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

            oneOf(mockHttpRequest).getSession();will(returnValue(mockHttpSession));
            oneOf(mockHttpSession).getServletContext();will(returnValue(mockServletContext));
            oneOf(mockServletContext).getResourceAsStream(with(any(String.class))); will(returnValue(null));

            oneOf(mockFilter).getFilterStringAllRecords(); will(returnValue(filterString));

            allowing(mockMethod).getURI();will(returnValue(new URI("http://service.wfs/wfs", false)));
        }});

        ModelAndView modelAndView = gsmlController.requestAllFeatures(wfsUrl, featureType, bboxJsonString, maxFeatures, mockHttpRequest);
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

            oneOf(mockHttpRequest).getSession();will(returnValue(mockHttpSession));
            oneOf(mockHttpSession).getServletContext();will(returnValue(mockServletContext));
            oneOf(mockServletContext).getResourceAsStream(with(any(String.class))); will(returnValue(null));

            oneOf(mockFilter).getFilterStringBoundingBox(with(any(FilterBoundingBox.class))); will(returnValue(filterString));

            allowing(mockMethod).getURI();will(returnValue(new URI("http://service.wfs/wfs", false)));
        }});

        ModelAndView modelAndView = gsmlController.requestAllFeatures(wfsUrl, featureType, bboxJsonString, maxFeatures, mockHttpRequest);
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
        final String filterString = "filterStr";
        final String wfsUrl = "http://service/wfs";
        final String featureType = "type:name";
        final String featureId = "feature-id";
        final int maxFeatures = 1234;
        final String srs = null; //dont specify this

        context.checking(new Expectations() {{
            oneOf(mockWfsService).getWfsResponseAsKml(wfsUrl, featureType, featureId);will(returnValue(new WFSKMLResponse(gmlBlob, kmlBlob, mockMethod)));

            oneOf(mockHttpRequest).getSession();will(returnValue(mockHttpSession));
            oneOf(mockHttpSession).getServletContext();will(returnValue(mockServletContext));
            oneOf(mockServletContext).getResourceAsStream(with(any(String.class))); will(returnValue(null));

            allowing(mockMethod).getURI();will(returnValue(new URI("http://service.wfs/wfs", false)));
        }});

        ModelAndView modelAndView = gsmlController.requestFeature(wfsUrl, featureType, featureId, mockHttpRequest);
        ModelMap dataObj = (ModelMap) modelAndView.getModel().get("data");
        Assert.assertTrue((Boolean) modelAndView.getModel().get("success"));
        Assert.assertNotNull(dataObj);
        Assert.assertEquals(gmlBlob, dataObj.get("gml"));
        Assert.assertEquals(kmlBlob, dataObj.get("kml"));
    }
}

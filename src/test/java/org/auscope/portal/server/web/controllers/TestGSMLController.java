package org.auscope.portal.server.web.controllers;

import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.gsml.GSMLResponseHandler;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.domain.filter.IFilter;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
     * Mock httpService caller
     */
    private HttpServiceCaller httpServiceCaller = context.mock(HttpServiceCaller.class);

    /**
     * Mock gml to kml converter
     */
    private GmlToKml gmlToKml = context.mock(GmlToKml.class);

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

    /**
     * Mock session
     */
    private ServletContext mockServletContext = context.mock(ServletContext.class);

    private WFSGetFeatureMethodMaker wfsGetFeatureMethodMaker = context.mock(WFSGetFeatureMethodMaker.class);

    private IFilter mockFilter = context.mock(IFilter.class);

    @Before
    public void setUp() {
        gsmlController = new GSMLController(httpServiceCaller, gmlToKml, wfsGetFeatureMethodMaker, mockFilter);
    }

    /**
     * Test that all classes are invoked correctly and return valid JSON
     */
    @Test
    public void testGetAllFeatures() throws Exception {
        final String kmlBlob = "kmlBlob";
        final String filterString = "filterStr";

        context.checking(new Expectations() {{
            oneOf(httpServiceCaller).getHttpClient();
            oneOf(httpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));

            oneOf(gmlToKml).convert(with(any(String.class)), with(any(InputStream.class)),with(any(String.class)));will(returnValue(kmlBlob));

            oneOf(wfsGetFeatureMethodMaker).makeMethod(with(any(String.class)), with(any(String.class)), with(any(String.class)), with(any(Integer.class)), with(any(String.class)));

            oneOf(mockHttpRequest).getSession();will(returnValue(mockHttpSession));
            oneOf(mockHttpSession).getServletContext();will(returnValue(mockServletContext));
            oneOf(mockServletContext).getResourceAsStream(with(any(String.class))); will(returnValue(null));

            oneOf(mockFilter).getFilterStringAllRecords(); will(returnValue(filterString));
        }});

        ModelAndView modelAndView = gsmlController.requestAllFeatures("fake", "fake", null, 0, mockHttpRequest);

        //check that the kml blob has been put ont he model
        modelAndView.getModel().get("data").equals(kmlBlob);
        modelAndView.getModel().get("success").equals(true);
    }

    @Test
    public void testGetAllFeaturesInBbox() throws Exception {
        final String kmlBlob = "kmlBlob";
        final String filterString = "filterStr";
        final String bboxToParse = "{\"bboxSrs\":\"http://www.opengis.net/gml/srs/epsg.xml%234326\",\"lowerCornerPoints\":[-5,-6],\"upperCornerPoints\":[7,8]}";


        context.checking(new Expectations() {{
            oneOf(httpServiceCaller).getHttpClient();
            oneOf(httpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));

            oneOf(gmlToKml).convert(with(any(String.class)), with(any(InputStream.class)),with(any(String.class)));will(returnValue(kmlBlob));

            oneOf(wfsGetFeatureMethodMaker).makeMethod(with(any(String.class)), with(any(String.class)), with(any(String.class)), with(any(Integer.class)), with(any(String.class)));

            oneOf(mockHttpRequest).getSession();will(returnValue(mockHttpSession));
            oneOf(mockHttpSession).getServletContext();will(returnValue(mockServletContext));
            oneOf(mockServletContext).getResourceAsStream(with(any(String.class))); will(returnValue(null));

            oneOf(mockFilter).getFilterStringBoundingBox(with(any(FilterBoundingBox.class))); will(returnValue(filterString));
        }});

        ModelAndView modelAndView = gsmlController.requestAllFeatures("fake", "fake", bboxToParse, 0, mockHttpRequest);

        //check that the kml blob has been put ont he model
        modelAndView.getModel().get("data").equals(kmlBlob);
        modelAndView.getModel().get("success").equals(true);
    }

    @Test
    public void testRequestFeature() throws Exception {
        final String kmlBlob = "kmlBlob";
        final String serviceUrl = "http://example.com";
        final String featureType = "featureType";
        final String featureId = "featureId";
        final HttpMethodBase mockMethod = context.mock(HttpMethodBase.class);

        context.checking(new Expectations() {{
            oneOf(httpServiceCaller).getHttpClient();
            oneOf(httpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));

            oneOf(gmlToKml).convert(with(any(String.class)), with(any(InputStream.class)), with(any(String.class)));will(returnValue(kmlBlob));

            oneOf(wfsGetFeatureMethodMaker).makeMethod(serviceUrl, featureType, featureId);will(returnValue(mockMethod));

            oneOf(mockHttpRequest).getSession();will(returnValue(mockHttpSession));
            oneOf(mockHttpSession).getServletContext();will(returnValue(mockServletContext));
            oneOf(mockServletContext).getResourceAsStream(with(any(String.class))); will(returnValue(null));
        }});

        ModelAndView modelAndView = gsmlController.requestFeature(serviceUrl,featureType, featureId,mockHttpRequest);

        //check that the kml blob has been put ont he model
        Assert.assertEquals(kmlBlob, ((Map)modelAndView.getModel().get("data")).get("kml"));
        Assert.assertTrue(modelAndView.getModel().get("success").equals(true));
    }
}

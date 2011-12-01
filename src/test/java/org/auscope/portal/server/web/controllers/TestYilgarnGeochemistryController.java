package org.auscope.portal.server.web.controllers;


import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.gsml.YilgarnLocatedSpecimenRecord;
import org.auscope.portal.gsml.YilgarnObservationRecord;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.web.service.YilgarnGeochemistryService;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

/**
 * The Class TestYilgarnLocSpecimenController.
 */
public class TestYilgarnGeochemistryController {

    /** The context. */
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    /** The http service caller. */
    private HttpServiceCaller mockHttpServiceCaller = context.mock(HttpServiceCaller.class);

    /** The mock http request. */
    private HttpServletRequest mockHttpRequest = context.mock(HttpServletRequest.class);

    /** The mock http session. */
    private HttpSession mockHttpSession = context.mock(HttpSession.class);

    /** The mock servlet context. */
    private ServletContext mockServletContext = context.mock(ServletContext.class);

    /** The mock servlet context. */
    private YilgarnGeochemistryService mockGeochemService = context.mock(YilgarnGeochemistryService.class);

    /** The mock http response. */
    private HttpServletResponse mockHttpResponse = context.mock(HttpServletResponse.class);

    /** The mock method maker. */
    private WFSGetFeatureMethodMaker mockMethodMaker = context.mock(WFSGetFeatureMethodMaker.class);

    /** The mock method maker. */
    private GmlToKml mockGmlToKml = context.mock(GmlToKml.class);

    /** The yilgarn loc specimen controller. */
    private YilgarnGeochemistryController controller;

    @Before
    public void setUp() {
        controller = new YilgarnGeochemistryController(mockHttpServiceCaller, mockGmlToKml, mockMethodMaker, mockGeochemService);
    }

    /**
     * Test located specimen feature can do a basic transform
     *
     * @throws Exception the exception
     */
    @Test
    public void testLocatedSpecimenFeature_SimpleTransform() throws Exception {

        final String serviceUrl = "http://fake.com/wfs";
        final String featureId = "feature_id";
        final String materialClass = "matclass";
        final YilgarnObservationRecord[] mockObservations = new YilgarnObservationRecord[] {};
        final YilgarnLocatedSpecimenRecord mockLocSpecRecord = context.mock(YilgarnLocatedSpecimenRecord.class);

        context.checking(new Expectations() {{
            oneOf(mockGeochemService).getLocatedSpecimens(serviceUrl, featureId);will(returnValue(mockLocSpecRecord));

            allowing(mockLocSpecRecord).getMaterialClass();will(returnValue(materialClass));
            allowing(mockLocSpecRecord).getRelatedObservations();will(returnValue(mockObservations));
        }});

        ModelAndView modelAndView = controller.doLocatedSpecimenFeature(serviceUrl, featureId);
        Assert.assertNotNull(modelAndView);
        Map<String, Object> model = modelAndView.getModel();
        Assert.assertEquals(true, model.get("success"));

        ModelMap data = (ModelMap) model.get("data");
        Assert.assertNotNull(data);
        Assert.assertSame(mockObservations, data.get("records"));
    }

    /**
     * Test located specimen feature. only returns observations with unique names
     *
     * @throws Exception the exception
     */
    @Test
    public void testLocatedSpecimenFeature_UniqueObservations() throws Exception {

        final String serviceUrl = "http://fake.com/wfs";
        final String featureId = "feature_id";
        final String materialClass = "matclass";
        final YilgarnObservationRecord[] mockObservations = new YilgarnObservationRecord[] {
                context.mock(YilgarnObservationRecord.class, "obs0"),
                context.mock(YilgarnObservationRecord.class, "obs1"),
                context.mock(YilgarnObservationRecord.class, "obs2"),
                context.mock(YilgarnObservationRecord.class, "obs3"),
                context.mock(YilgarnObservationRecord.class, "obs4")};
        final YilgarnLocatedSpecimenRecord mockLocSpecRecord = context.mock(YilgarnLocatedSpecimenRecord.class);

        context.checking(new Expectations() {{
            oneOf(mockGeochemService).getLocatedSpecimens(serviceUrl, featureId);will(returnValue(mockLocSpecRecord));

            allowing(mockLocSpecRecord).getMaterialClass();will(returnValue(materialClass));
            allowing(mockLocSpecRecord).getRelatedObservations();will(returnValue(mockObservations));

            allowing(mockObservations[0]).getAnalyteName();will(returnValue("nacl"));
            allowing(mockObservations[1]).getAnalyteName();will(returnValue("au"));
            allowing(mockObservations[2]).getAnalyteName();will(returnValue("au"));
            allowing(mockObservations[3]).getAnalyteName();will(returnValue("nacl"));
            allowing(mockObservations[4]).getAnalyteName();will(returnValue("ag"));
        }});

        ModelAndView modelAndView = controller.doLocatedSpecimenFeature(serviceUrl, featureId);
        Assert.assertNotNull(modelAndView);
        Map<String, Object> model = modelAndView.getModel();
        Assert.assertEquals(true, model.get("success"));

        ModelMap data = (ModelMap) model.get("data");
        Assert.assertNotNull(data);
        Assert.assertSame(mockObservations, data.get("records"));
        Assert.assertNotNull(data.get("uniqueSpecName"));
        List<String> uniqueNames = Arrays.asList((String[]) data.get("uniqueSpecName"));

        Assert.assertEquals(3, uniqueNames.size());
        Assert.assertTrue(uniqueNames.contains("nacl"));
        Assert.assertTrue(uniqueNames.contains("au"));
        Assert.assertTrue(uniqueNames.contains("ag"));
    }

    /**
     * Test located specimen feature can handle exceptions from the underlying service
     *
     * @throws Exception the exception
     */
    @Test
    public void testLocatedSpecimenFeature_ServiceException() throws Exception {

        final String serviceUrl = "http://fake.com/wfs";
        final String featureId = "feature_id";
        final String materialClass = "matclass";
        final YilgarnObservationRecord[] mockObservations = new YilgarnObservationRecord[] {};
        final YilgarnLocatedSpecimenRecord mockLocSpecRecord = context.mock(YilgarnLocatedSpecimenRecord.class);

        context.checking(new Expectations() {{
            oneOf(mockGeochemService).getLocatedSpecimens(serviceUrl, featureId);will(throwException(new ConnectException()));

            allowing(mockLocSpecRecord).getMaterialClass();will(returnValue(materialClass));
            allowing(mockLocSpecRecord).getRelatedObservations();will(returnValue(mockObservations));
        }});

        ModelAndView modelAndView = controller.doLocatedSpecimenFeature(serviceUrl, featureId);
        Map<String, Object> model = modelAndView.getModel();
        Assert.assertNotNull(modelAndView);
        Assert.assertFalse((Boolean) model.get("success"));
    }

    /**
     * Test located specimen feature can handle the underlying service returning null (indicating no results)
     *
     * @throws Exception the exception
     */
    @Test
    public void testLocatedSpecimenFeature_NoResults() throws Exception {

        final String serviceUrl = "http://fake.com/wfs";
        final String featureId = "feature_id_thatdne";
        final String materialClass = "matclass";
        final YilgarnObservationRecord[] mockObservations = new YilgarnObservationRecord[] {};
        final YilgarnLocatedSpecimenRecord mockLocSpecRecord = context.mock(YilgarnLocatedSpecimenRecord.class);

        context.checking(new Expectations() {{
            oneOf(mockGeochemService).getLocatedSpecimens(serviceUrl, featureId);will(returnValue(null));

            allowing(mockLocSpecRecord).getMaterialClass();will(returnValue(materialClass));
            allowing(mockLocSpecRecord).getRelatedObservations();will(returnValue(mockObservations));
        }});

        ModelAndView modelAndView = controller.doLocatedSpecimenFeature(serviceUrl, featureId);
        Map<String, Object> model = modelAndView.getModel();
        Assert.assertNotNull(modelAndView);
        Assert.assertFalse((Boolean) model.get("success"));
    }

    /**
     * Test doing geochemistry filter and getting all values
     */
    @Test
    public void testYilgarnGeochemistryFilter() throws Exception{
        final String kmlBlob = "kmlBlob";
        final String expectedGML = "<gml/>";
        final StringWriter actualJSONResponse = new StringWriter();

        context.checking(new Expectations() {{
            oneOf(mockHttpServiceCaller).getHttpClient();
            oneOf(mockHttpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class))); will(returnValue(expectedGML));

            oneOf(mockHttpResponse).setContentType(with(any(String.class)));
            oneOf(mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));

            oneOf(mockGmlToKml).convert(with(any(String.class)), with(any(InputStream.class)),with(any(String.class)));will(returnValue(kmlBlob));
            oneOf(mockMethodMaker).makeMethod(with(any(String.class)), with(any(String.class)), with(any(String.class)), with(any(Integer.class)));
            oneOf(mockHttpRequest).getSession();will(returnValue(mockHttpSession));
            oneOf(mockHttpSession).getServletContext();will(returnValue(mockServletContext));
            oneOf(mockServletContext).getResourceAsStream(with(any(String.class))); will(returnValue(null));
        }});
        ModelAndView modelAndView = controller.doYilgarnGeochemistryFilter("fake", "fake", null, 0,mockHttpRequest);
        Assert.assertNotNull(modelAndView);
        Map<String, Object> model = modelAndView.getModel();
        Assert.assertEquals(true, model.get("success"));

        Assert.assertNotNull(model.get("data"));
    }


}



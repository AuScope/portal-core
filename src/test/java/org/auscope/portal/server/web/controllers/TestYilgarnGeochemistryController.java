package org.auscope.portal.server.web.controllers;


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

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.URI;
import org.auscope.portal.gsml.YilgarnLocatedSpecimenRecord;
import org.auscope.portal.gsml.YilgarnObservationRecord;
import org.auscope.portal.server.domain.wfs.WFSKMLResponse;
import org.auscope.portal.server.web.service.WFSService;
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

    /** The mock Web Feature Service request Service*/
    private WFSService mockWfsService = context.mock(WFSService.class);

    /** The mock method*/
    private HttpMethodBase mockMethod = context.mock(HttpMethodBase.class);

    /** The yilgarn loc specimen controller. */
    private YilgarnGeochemistryController controller;

    @Before
    public void setUp() {
        controller = new YilgarnGeochemistryController(mockWfsService, mockGeochemService);
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
        final String serviceUrl = "http://service/wfs";
        final String geologicName = "filter info";
        final int maxFeatures = 0;
        final String bbox = null;
        final String expectedGML = "<gml/>";
        final StringWriter actualJSONResponse = new StringWriter();

        context.checking(new Expectations() {{

            oneOf(mockHttpResponse).setContentType(with(any(String.class)));
            oneOf(mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));

            oneOf(mockWfsService).getWfsResponseAsKml(with(equal(serviceUrl)), with(equal("gsml:GeologicUnit")), with(any(String.class)), with(equal(maxFeatures)), with(equal((String)null)));will(returnValue(new WFSKMLResponse(expectedGML, kmlBlob, mockMethod)));

            oneOf(mockHttpRequest).getSession();will(returnValue(mockHttpSession));
            oneOf(mockHttpSession).getServletContext();will(returnValue(mockServletContext));
            oneOf(mockServletContext).getResourceAsStream(with(any(String.class))); will(returnValue(null));

            allowing(mockMethod).getURI();will(returnValue(new URI(serviceUrl, true)));
        }});
        ModelAndView modelAndView = controller.doYilgarnGeochemistryFilter(serviceUrl, geologicName, bbox, maxFeatures,mockHttpRequest);
        Assert.assertNotNull(modelAndView);
        Map<String, Object> model = modelAndView.getModel();
        Assert.assertEquals(true, model.get("success"));
        Assert.assertNotNull(model.get("data"));
    }


}



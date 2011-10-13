package org.auscope.portal.server.web.controllers;

import java.io.ByteArrayInputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.server.domain.vocab.Concept;
import org.auscope.portal.server.domain.vocab.ConceptFactory;
import org.auscope.portal.server.domain.vocab.NamedIndividual;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.web.SISSVocMethodMaker;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Node;


/**
 * User: Michael Stegherr
 * Date: Sep 14, 2009
 * Time: 11:28:47 AM
 */
public class TestVocabController {

    /**
     * JMock context
     */
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    /**
     * Main object we are testing
     */
    private VocabController vocabController;

    /**
     * Mock httpService caller
     */
    private HttpServiceCaller httpServiceCaller = context.mock(HttpServiceCaller.class);

    /**
     * Mock property configurer
     */
    private PortalPropertyPlaceholderConfigurer propertyConfigurer = context.mock(PortalPropertyPlaceholderConfigurer.class);

    /**
     * Mock http request
     */
    private HttpServletRequest mockHttpRequest = context.mock(HttpServletRequest.class);

    /**
     * Mock http response
     */
    private HttpServletResponse mockHttpResponse = context.mock(HttpServletResponse.class);

    private SISSVocMethodMaker mockSissVocMethodMaker = context.mock(SISSVocMethodMaker.class);

    private ConceptFactory mockConceptFactory = context.mock(ConceptFactory.class);

    @Before
    public void setup() throws Exception {
        this.vocabController = new VocabController(
                this.httpServiceCaller, mockConceptFactory, this.propertyConfigurer, mockSissVocMethodMaker);
    }

    /**
     * Tests the getScalarQuery method is correctly forming a JSON response from an XML chunk received from a remote URL.
     * @throws Exception
     */
    @Test
    public void testGetScalarQuery() throws Exception {
        final String serviceUrl = "http://service.example.com/query";
        final String docString = org.auscope.portal.Util.loadXML("src/test/resources/GetVocabQuery_Success.xml");
        final ByteArrayInputStream docStringStream = new ByteArrayInputStream(docString.getBytes());
        final String expectedDefinition =  "Mineral index for TSA singleton match or primary mixture component";
        final String expectedScopeNote =  expectedDefinition;
        final String expectedLabel = "TSA_S_Mineral1";
        final String repositoryName = "testRepository";
        final String labelName = "testLabel";
        final HttpMethodBase mockHttpMethod = context.mock(HttpMethodBase.class);
        final HttpClient mockHttpClient = context.mock(HttpClient.class);
        final Concept[] parsedConcepts = new Concept[] {
                context.mock(Concept.class)
        };

        context.checking(new Expectations() {{
            allowing(propertyConfigurer).resolvePlaceholder("HOST.vocabService.url");will(returnValue(serviceUrl));

            //One call to make a method
            oneOf(mockSissVocMethodMaker).getConceptByLabelMethod(serviceUrl, repositoryName, labelName);will(returnValue(mockHttpMethod));

            //One call to remote service
            oneOf(httpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(httpServiceCaller).getMethodResponseAsStream(mockHttpMethod,mockHttpClient);will(returnValue(docStringStream));

            //One parsing step
            oneOf(mockConceptFactory).parseFromRDF(with(any(Node.class)));will(returnValue(parsedConcepts));

            //configuration for mock response concepts
            allowing(parsedConcepts[0]).getDefinition();will(returnValue(expectedDefinition));
            allowing(parsedConcepts[0]).getPreferredLabel();will(returnValue(expectedLabel));
        }});

        ModelAndView mav = this.vocabController.getScalarQuery(repositoryName, labelName);
        ModelMap data = (ModelMap) mav.getModel().get("data");

        Assert.assertTrue((Boolean)mav.getModel().get("success"));
        Assert.assertEquals(expectedScopeNote, data.get("scopeNote"));
        Assert.assertEquals(expectedLabel, data.get("label"));
    }

    /**
     * Unit test for getAllCSWThemes method under normal operation
     * @throws Exception
     */
    @Test
    public void testGetAllCSWThemes() throws Exception {
        final String serviceUrl = "http://service.example.com/query";
        final String docString = org.auscope.portal.Util.loadXML("src/test/resources/SISSVocResponse.xml");
        final ByteArrayInputStream docStringStream = new ByteArrayInputStream(docString.getBytes());
        final String repositoryName = "ga-darwin";
        final String labelName = "*";
        final HttpMethodBase mockHttpMethod = context.mock(HttpMethodBase.class);
        final HttpClient mockHttpClient = context.mock(HttpClient.class);

        //Building a set of mock objects to emulate this complex tree is too much of a pain
        Concept concept1 = new Concept("urn:concept:1");
        Concept concept2 = new Concept("urn:concept:2");
        Concept concept3 = new Concept("urn:concept:3");
        NamedIndividual ni1 = new NamedIndividual("urn:ni:1");
        NamedIndividual ni2 = new NamedIndividual("urn:ni:2");

        concept1.setNarrower(new Concept[] {concept2, concept3, ni2});
        concept1.setLabel("LabelConcept1");
        concept1.setPreferredLabel("PrefLabelConcept1");

        concept2.setBroader(new Concept[] {concept1});
        concept2.setRelated(new Concept[] {concept3});
        concept2.setLabel("LabelConcept2");
        concept2.setPreferredLabel("PrefLabelConcept2");
        concept2.setDefinition("DefinitionConcept2");

        concept3.setBroader(new Concept[] {concept1});
        concept3.setRelated(new Concept[] {concept2});
        concept3.setNarrower(new Concept[] {ni1});
        concept3.setLabel("LabelConcept3");
        concept3.setPreferredLabel("PrefLabelConcept3");

        ni1.setBroader(new Concept[] {concept3});
        ni1.setLabel("LabelNamedIndividual1");
        ni1.setPreferredLabel("PrefLabelNamedIndividual1");

        ni2.setBroader(new Concept[] {concept1});
        ni2.setLabel("LabelNamedIndividual2");
        ni2.setPreferredLabel("PrefLabelNamedIndividual2");

        final Concept[] parsedConcepts = new Concept[] {
                concept1
        };

        context.checking(new Expectations() {{
            allowing(propertyConfigurer).resolvePlaceholder("HOST.vocabService.url");will(returnValue(serviceUrl));

            //One call to make a method
            oneOf(mockSissVocMethodMaker).getConceptByLabelMethod(serviceUrl, repositoryName, labelName);will(returnValue(mockHttpMethod));

            //One call to remote service
            oneOf(httpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(httpServiceCaller).getMethodResponseAsStream(mockHttpMethod,mockHttpClient);will(returnValue(docStringStream));

            //One parsing step
            oneOf(mockConceptFactory).parseFromRDF(with(any(Node.class)));will(returnValue(parsedConcepts));
        }});

        ModelAndView mav = this.vocabController.getAllCSWThemes();
        Assert.assertTrue((Boolean)mav.getModel().get("success"));

        List<ModelMap> data = (List<ModelMap>) mav.getModel().get("data");
        Assert.assertNotNull(data);
        Assert.assertEquals(5, data.size());

        Assert.assertEquals("urn:concept:1", data.get(0).get("urn"));
        Assert.assertEquals("PrefLabelConcept1", data.get(0).get("label"));
        Assert.assertEquals(new Integer(0), (Integer) data.get(0).get("indent"));

        Assert.assertEquals("urn:concept:2", data.get(1).get("urn"));
        Assert.assertEquals("PrefLabelConcept2", data.get(1).get("label"));
        Assert.assertEquals(new Integer(1), (Integer) data.get(1).get("indent"));

        Assert.assertEquals("urn:concept:3", data.get(2).get("urn"));
        Assert.assertEquals("PrefLabelConcept3", data.get(2).get("label"));
        Assert.assertEquals(new Integer(1), (Integer) data.get(2).get("indent"));

        Assert.assertEquals("urn:ni:1", data.get(3).get("urn"));
        Assert.assertEquals("PrefLabelNamedIndividual1", data.get(3).get("label"));
        Assert.assertEquals(new Integer(2), (Integer) data.get(3).get("indent"));

        Assert.assertEquals("urn:ni:2", data.get(4).get("urn"));
        Assert.assertEquals("PrefLabelNamedIndividual2", data.get(4).get("label"));
        Assert.assertEquals(new Integer(1), (Integer) data.get(4).get("indent"));
    }

    /**
     * Unit test for getAllCSWThemes method for when vocab service calls fail
     * @throws Exception
     */
    @Test
    public void testGetAllCSWThemesVocabFailure() throws Exception {
        final String serviceUrl = "http://service.example.com/query";
        final String repositoryName = "ga-darwin";
        final String labelName = "*";
        final HttpMethodBase mockHttpMethod = context.mock(HttpMethodBase.class);
        final HttpClient mockHttpClient = context.mock(HttpClient.class);


        context.checking(new Expectations() {{
            allowing(propertyConfigurer).resolvePlaceholder("HOST.vocabService.url");will(returnValue(serviceUrl));

            //One call to make a method
            oneOf(mockSissVocMethodMaker).getConceptByLabelMethod(serviceUrl, repositoryName, labelName);will(returnValue(mockHttpMethod));

            //One call to remote service
            oneOf(httpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(httpServiceCaller).getMethodResponseAsStream(mockHttpMethod,mockHttpClient);will(throwException(new Exception()));
        }});

        ModelAndView mav = this.vocabController.getAllCSWThemes();
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Unit test for getAllCSWThemes method for when parsing vocab XML fails
     * @throws Exception
     */
    @Test
    public void testGetAllCSWThemesParsingError() throws Exception {
        final String serviceUrl = "http://service.example.com/query";
        final String docString = "this is not an XML string";
        final ByteArrayInputStream docStringStream = new ByteArrayInputStream(docString.getBytes());
        final String repositoryName = "ga-darwin";
        final String labelName = "*";
        final HttpMethodBase mockHttpMethod = context.mock(HttpMethodBase.class);
        final HttpClient mockHttpClient = context.mock(HttpClient.class);


        context.checking(new Expectations() {{
            allowing(propertyConfigurer).resolvePlaceholder("HOST.vocabService.url");will(returnValue(serviceUrl));

            //One call to make a method
            oneOf(mockSissVocMethodMaker).getConceptByLabelMethod(serviceUrl, repositoryName, labelName);will(returnValue(mockHttpMethod));

            //One call to remote service
            oneOf(httpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(httpServiceCaller).getMethodResponseAsStream(mockHttpMethod,mockHttpClient);will(returnValue(docStringStream));

            oneOf(mockConceptFactory).parseFromRDF(with(any(Node.class)));will(throwException(new RuntimeException()));
        }});

        ModelAndView mav = this.vocabController.getAllCSWThemes();
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }
}

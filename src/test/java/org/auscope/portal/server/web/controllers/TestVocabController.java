package org.auscope.portal.server.web.controllers;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.web.SISSVocMethodMaker;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.vocabs.VocabularyServiceResponseHandler;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;


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

    @Before
    public void setup() throws Exception {
        VocabularyServiceResponseHandler responseHandler =
            new VocabularyServiceResponseHandler();

        this.vocabController = new VocabController(
                this.httpServiceCaller, responseHandler, this.propertyConfigurer, mockSissVocMethodMaker);
    }

    /**
     * Tests the getScalarQuery method is correctly forming a JSON response from an XML chunk received from a remote URL.
     * @throws Exception
     */
    @Test
    public void testGetScalarQuery() throws Exception {
        final String serviceUrl = "http://service.example.com/query";
        final String docString = org.auscope.portal.Util.loadXML("src/test/resources/GetVocabQuery_Success.xml");
        final String expectedScopeNote =  "Mineral index for TSA singleton match or primary mixture component";
        final String expectedLabel = "TSA_S_Mineral1";
        final String repositoryName = "testRepository";
        final String labelName = "testLabel";

        context.checking(new Expectations() {{
            allowing(propertyConfigurer).resolvePlaceholder("HOST.vocabService.url");will(returnValue(serviceUrl));
            oneOf(mockSissVocMethodMaker).getCommodityMethod(serviceUrl, "commodity_vocab", "urn:cgi:classifierScheme:GA:commodity");

            oneOf(httpServiceCaller).getMethodResponseAsString(with(any(GetMethod.class)),with(any(HttpClient.class)));will(returnValue(docString));
            oneOf(httpServiceCaller).getHttpClient();

            oneOf(mockSissVocMethodMaker).getConceptByLabelMethod(serviceUrl, repositoryName, labelName);
        }});

        ModelAndView mav = this.vocabController.getScalarQuery(repositoryName, labelName);

        ModelMap data = (ModelMap) mav.getModel().get("data");

        Assert.assertTrue((Boolean)mav.getModel().get("success"));
        Assert.assertEquals(expectedScopeNote, data.get("scopeNote"));
        Assert.assertEquals(expectedLabel, data.get("label"));

    }
}

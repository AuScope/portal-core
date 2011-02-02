package org.auscope.portal.server.web.controllers;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.vocabs.VocabularyServiceResponseHandler;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
    
    @Before
    public void setup() throws Exception {
        final String serviceUrl = "http://service.example.com/query";
//        GetMethod getMethod;

        context.checking(new Expectations() {{
            //constructor gets a host property
        	allowing(propertyConfigurer).resolvePlaceholder(with(any(String.class)));will(returnValue(serviceUrl));
            
            //TODO check, that queryString has been built correctly!
            
            //check that the executor was called
//            oneOf(threadExecutor).execute(with(any(Runnable.class)));
        }});

        VocabularyServiceResponseHandler responseHandler =
            new VocabularyServiceResponseHandler();
        
        this.vocabController = new VocabController(
                this.httpServiceCaller, responseHandler, this.propertyConfigurer);
    }

    /**
     * Tests the getScalarQuery method is correctly forming a JSON response from an XML chunk received from a remote URL.
     * @throws Exception
     */
    @Test
    public void testGetScalarQuery() throws Exception {
    	final String docString = org.auscope.portal.Util.loadXML("src/test/resources/GetVocabQuery_Success.xml");
        final String returnedString = docString.replace("\"", "\\\"").replace("\t","\\t").replace("</", "<\\/");
        final String expectedScopeNote =  "\"scopeNote\":\"Mineral index for TSA singleton match or primary mixture component\"";
        final String expectedSuccess = "\"success\":true";
        final String expectedLabel = "\"label\":\"TSA_S_Mineral1\"";
        final String expectedData = "\"data\":\"" + returnedString + "\"";
        final StringWriter actualJSONResponse = new StringWriter();
        final String repositoryName = "testRepository";
        final String labelName = "testLabel";

        context.checking(new Expectations() {{
            oneOf(httpServiceCaller).getMethodResponseAsString(with(any(GetMethod.class)),with(any(HttpClient.class)));will(returnValue(docString));
            oneOf(httpServiceCaller).getHttpClient();
            
            
            //check that the correct response is getting output
            oneOf(mockHttpResponse).setContentType(with(any(String.class)));
            oneOf(mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});
        
        ModelAndView mav = this.vocabController.getScalarQuery(repositoryName, labelName);
        mav.getView().render(mav.getModel(), mockHttpRequest, mockHttpResponse);
        
        
        String response = actualJSONResponse.getBuffer().toString();
        Assert.assertTrue(response.contains(expectedScopeNote));
        Assert.assertTrue(response.contains(expectedSuccess));
        Assert.assertTrue(response.contains(expectedLabel));
        Assert.assertTrue(response.contains(expectedData));
        
    }
}

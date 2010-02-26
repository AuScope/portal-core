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
import org.junit.*;
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
     * Test that the method is calling a service and creating a JSON response out of the
     * commodity concepts
     *  
     * @throws Exception
     */
    @Test
    public void testGetCommodities() throws Exception {
        final String docString = org.auscope.portal.Util.loadXML("src/test/resources/vocabularyServiceResponse.xml");
        final String expectedJSONResponse = "[[\"urn:cgi:classifier:PIRSA:commodity:QP\",\"Quarry Products\"],[\"urn:cgi:classifier:PIRSA:commodity:RHYO\",\"Rhyolite\"],[\"urn:cgi:classifier:PIRSA:commodity:RHYRB\",\"Rhyolite - road base\"],[\"urn:cgi:classifier:PIRSA:commodity:SCHFL\",\"Schist - filling\"],[\"urn:cgi:classifier:PIRSA:commodity:SCHRB\",\"Schist - road base\"],[\"urn:cgi:classifier:PIRSA:commodity:SCHT\",\"Schist\"],[\"urn:cgi:classifier:PIRSA:commodity:SLSCA\",\"Siltstone - concrete aggregate\"],[\"urn:cgi:classifier:PIRSA:commodity:SLSFL\",\"Siltstone - filling\"],[\"urn:cgi:classifier:PIRSA:commodity:SLSRB\",\"Siltstone - road base\"],[\"urn:cgi:classifier:PIRSA:commodity:SLSRS\",\"Siltstone - road seal aggregate\"],[\"urn:cgi:classifier:PIRSA:commodity:SLSSP\",\"Siltstone - spalls\"],[\"urn:cgi:classifier:PIRSA:commodity:SLSSS\",\"Siltstone - specification sand\"],[\"urn:cgi:classifier:PIRSA:commodity:SLST\",\"Siltstone\"],[\"urn:cgi:classifier:GSV:commodity:Agg\",\"Aggregate - undifferentiated\"],[\"urn:cgi:classifier:GSWA:commodity:Agg\",\"Aggregate - undifferentiated\"],[\"urn:cgi:classifier:PIRSA:commodity:AGGR\",\"Aggregate - undifferentiated\"],[\"urn:cgi:classifier:PIRSA:commodity:AMPRB\",\"Amphibolite - road base\"],[\"urn:cgi:classifier:PIRSA:commodity:GBRRB\",\"Gabbro - road base\"],[\"urn:cgi:classifier:PIRSA:commodity:GNSCA\",\"Gneiss - concrete aggregate\"],[\"urn:cgi:classifier:PIRSA:commodity:GNSCD\",\"Gneiss - crusher dust\"],[\"urn:cgi:classifier:PIRSA:commodity:GNSFL\",\"Gneiss - filling\"],[\"urn:cgi:classifier:PIRSA:commodity:GNSNA\",\"Gneiss - general purpose aggregate\"],[\"urn:cgi:classifier:PIRSA:commodity:GNSRB\",\"Gneiss - road base\"],[\"urn:cgi:classifier:PIRSA:commodity:GNSRS\",\"Gneiss - road seal aggregate\"],[\"urn:cgi:classifier:PIRSA:commodity:GNSS\",\"Gneiss\"],[\"urn:cgi:classifier:PIRSA:commodity:GNSSP\",\"Gneiss - spalls\"],[\"urn:cgi:classifier:PIRSA:commodity:GNSSS\",\"Gneiss - specification sand\"]]";
        final StringWriter actualJSONResponse = new StringWriter();

        context.checking(new Expectations() {{
            oneOf(httpServiceCaller).getMethodResponseAsString(with(any(GetMethod.class)),with(any(HttpClient.class)));will(returnValue(docString));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});

        ModelAndView mav = this.vocabController.getCommodities();
        
        //check that our JSON response has been nicely populated
        //calling the renderer will write the JSON to our mocks
        mav.getView().render(mav.getModel(), mockHttpRequest, mockHttpResponse);

        //System.out.println(expectedJSONResponse);
        //System.out.println(actualJSONResponse.getBuffer().toString());

        //check that the actual is the expected
        if(expectedJSONResponse.equals(actualJSONResponse.getBuffer().toString()))
            Assert.assertTrue(true);
        else
            Assert.assertFalse(true);        
    }
    
    /**
     * Tests the getScalarQuery method is correctly forming a JSON response from an XML chunk received from a remote URL.
     * @throws Exception
     */
    @Test
    public void testGetScalarQuery() throws Exception {
        final String docString = org.auscope.portal.Util.loadXML("src/test/resources/GetVocabQuery_Success.xml");
        final String returnedString = docString.replace("\"", "\\\"").replace("\t","\\t").replace("</", "<\\/");
        final String expectedJSONResponse = "{\"scopeNote\":\"Mineral index for TSA singleton match or primary mixture component\",\"success\":true,\"label\":\"TSA_S_Mineral1\",\"data\":\"" + returnedString + "\"}";
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
        
        System.out.println(expectedJSONResponse);
        System.out.println(actualJSONResponse.getBuffer().toString());
        
        Assert.assertEquals(expectedJSONResponse, actualJSONResponse.getBuffer().toString());
    }
}

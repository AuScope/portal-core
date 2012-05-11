package org.auscope.portal.server.web.service;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.PortalTestClass;
import org.auscope.portal.server.domain.vocab.Concept;
import org.auscope.portal.server.domain.vocab.ConceptFactory;
import org.auscope.portal.server.web.SISSVocMethodMaker;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;

public class TestSISSVocService extends PortalTestClass {

    private HttpMethodBase mockMethod = context.mock(HttpMethodBase.class);
    private HttpClient mockHttpClient = context.mock(HttpClient.class);
    private HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class);
    private ConceptFactory mockConceptFactory = context.mock(ConceptFactory.class);
    private SISSVocMethodMaker mockMethodMaker = context.mock(SISSVocMethodMaker.class);
    private SISSVocService service;

    @Before
    public void init() {
        service = new SISSVocService(mockServiceCaller, mockConceptFactory, mockMethodMaker);
    }

    /**
     * Tests the correct calls are made and the response is correctly parsed
     * @throws Exception
     */
    @Test
    public void testGetConceptByLabel() throws Exception {
        final String serviceUrl = "http://example.org/opendap";
        final String repository = "repository";
        final String label = "label";

        final InputStream responseStream = getClass().getResourceAsStream("/SISSVocResponse.xml");
        final Concept[] expectedResult = new Concept[] {context.mock(Concept.class)};

        context.checking(new Expectations() {{
            oneOf(mockMethodMaker).getConceptByLabelMethod(serviceUrl, repository, label);will(returnValue(mockMethod));
            oneOf(mockServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod, mockHttpClient);will(returnValue(responseStream));

            oneOf(mockConceptFactory).parseFromRDF(with(any(Node.class)));will(returnValue(expectedResult));
        }});

        Assert.assertSame(expectedResult, service.getConceptByLabel(serviceUrl, repository, label));
    }

    /**
     * Tests the correct calls are made and response errors are correctly parsed
     * @throws Exception
     */
    @Test(expected=PortalServiceException.class)
    public void testGetConceptByLabelException() throws Exception {
        final String serviceUrl = "http://example.org/opendap";
        final String repository = "repository";
        final String label = "label";

        context.checking(new Expectations() {{
            oneOf(mockMethodMaker).getConceptByLabelMethod(serviceUrl, repository, label);will(returnValue(mockMethod));
            oneOf(mockServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod, mockHttpClient);will(throwException(new IOException()));
        }});

        service.getConceptByLabel(serviceUrl, repository, label);
    }

    /**
     * Tests the correct calls are made and the response is correctly parsed
     * @throws Exception
     */
    @Test
    public void testGetCommodityConcepts() throws Exception {
        final String serviceUrl = "http://example.org/opendap";
        final String repository = "repository";
        final String commodityParent = "parent";

        final InputStream responseStream = getClass().getResourceAsStream("/sparqlCommoditiesResponse.xml");

        context.checking(new Expectations() {{
            oneOf(mockMethodMaker).getCommodityMethod(serviceUrl, repository, commodityParent);will(returnValue(mockMethod));
            oneOf(mockServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod, mockHttpClient);will(returnValue(responseStream));
        }});

        Concept[] result = service.getCommodityConcepts(serviceUrl, repository, commodityParent);
        Assert.assertNotNull(result);
        Assert.assertEquals(3, result.length);

        Assert.assertEquals("Silver", result[0].getLabel());
        Assert.assertEquals("urn:cgi:classifier:GA:commodity:Ag", result[0].getUrn());
        Assert.assertEquals("Agate", result[1].getLabel());
        Assert.assertEquals("urn:cgi:classifier:GA:commodity:Aga", result[1].getUrn());
        Assert.assertEquals("Moss agate", result[2].getLabel());
        Assert.assertEquals("urn:cgi:classifier:GA:commodity:Agam", result[2].getUrn());
    }

    /**
     * Tests the correct calls are made and response errors are correctly parsed
     * @throws Exception
     */
    @Test(expected=PortalServiceException.class)
    public void testGetCommodityConceptsException() throws Exception {
        final String serviceUrl = "http://example.org/opendap";
        final String repository = "repository";
        final String parent = "paretn";

        context.checking(new Expectations() {{
            oneOf(mockMethodMaker).getCommodityMethod(serviceUrl, repository, parent);will(returnValue(mockMethod));
            oneOf(mockServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod, mockHttpClient);will(throwException(new IOException()));
        }});

        service.getCommodityConcepts(serviceUrl, repository, parent);
    }
}

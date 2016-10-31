package org.auscope.portal.core.services;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpClientInputStream;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.sissvoc.SISSVoc2MethodMaker;
import org.auscope.portal.core.services.responses.vocab.Concept;
import org.auscope.portal.core.services.responses.vocab.ConceptFactory;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;

public class TestSISSVoc2Service extends PortalTestClass {

    private HttpRequestBase mockMethod = context.mock(HttpRequestBase.class);
    private HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class);
    private ConceptFactory mockConceptFactory = context.mock(ConceptFactory.class);
    private SISSVoc2MethodMaker mockMethodMaker = context.mock(SISSVoc2MethodMaker.class);
    private SISSVoc2Service service;

    @Before
    public void init() {
        service = new SISSVoc2Service(mockServiceCaller, mockConceptFactory, mockMethodMaker);
    }

    /**
     * Tests the correct calls are made and the response is correctly parsed
     * @throws PortalServiceException 
     * @throws IOException 
     * @throws URISyntaxException 
     */
    @Test
    public void testGetConceptByLabel() throws PortalServiceException, URISyntaxException, IOException {
        final String serviceUrl = "http://example.org/opendap";
        final String repository = "repository";
        final String label = "label";

        try (final HttpClientInputStream responseStream = new HttpClientInputStream(ResourceUtil
                .loadResourceAsStream("org/auscope/portal/core/test/responses/sissvoc/SISSVocResponse.xml"), null)) {
            final Concept[] expectedResult = new Concept[] { context.mock(Concept.class) };

            context.checking(new Expectations() {
                {
                    oneOf(mockMethodMaker).getConceptByLabelMethod(serviceUrl, repository, label);
                    will(returnValue(mockMethod));
                    oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                    will(returnValue(responseStream));
                    oneOf(mockConceptFactory).parseFromRDF(with(any(Node.class)));
                    will(returnValue(expectedResult));
                    oneOf(mockMethod).releaseConnection();
                }
            });

            Assert.assertSame(expectedResult, service.getConceptByLabel(serviceUrl, repository, label));
        }
    }

    /**
     * Tests the correct calls are made and response errors are correctly parsed
     * @throws PortalServiceException 
     * @throws IOException 
     * @throws URISyntaxException 
     */
    @Test(expected = PortalServiceException.class)
    public void testGetConceptByLabelException() throws PortalServiceException, URISyntaxException, IOException {
        final String serviceUrl = "http://example.org/opendap";
        final String repository = "repository";
        final String label = "label";

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).getConceptByLabelMethod(serviceUrl, repository, label);
                will(returnValue(mockMethod));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(throwException(new IOException()));
                oneOf(mockMethod).releaseConnection();
            }
        });

        service.getConceptByLabel(serviceUrl, repository, label);
    }

    /**
     * Tests the correct calls are made and the response is correctly parsed
     * @throws PortalServiceException 
     * @throws IOException 
     * @throws URISyntaxException 
     */
    @Test
    public void testGetCommodityConcepts() throws PortalServiceException, IOException, URISyntaxException {
        final String serviceUrl = "http://example.org/opendap";
        final String repository = "repository";
        final String commodityParent = "parent";

        try (final HttpClientInputStream responseStream = new HttpClientInputStream(ResourceUtil
                .loadResourceAsStream("org/auscope/portal/core/test/responses/sissvoc/sparqlCommoditiesResponse.xml"),null)) {

            context.checking(new Expectations() {
                {
                    oneOf(mockMethodMaker).getCommodityMethod(serviceUrl, repository, commodityParent);
                    will(returnValue(mockMethod));
                    oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                    will(returnValue(responseStream));
                    oneOf(mockMethod).releaseConnection();
                }
            });

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
    }

    /**
     * Tests the correct calls are made and response errors are correctly parsed
     * @throws IOException 
     * @throws URISyntaxException 
     * @throws PortalServiceException 
     */
    @Test(expected = PortalServiceException.class)
    public void testGetCommodityConceptsException() throws URISyntaxException, IOException, PortalServiceException {
        final String serviceUrl = "http://example.org/opendap";
        final String repository = "repository";
        final String parent = "paretn";

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).getCommodityMethod(serviceUrl, repository, parent);
                will(returnValue(mockMethod));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(throwException(new IOException()));
                oneOf(mockMethod).releaseConnection();
            }
        });

        service.getCommodityConcepts(serviceUrl, repository, parent);
    }
}

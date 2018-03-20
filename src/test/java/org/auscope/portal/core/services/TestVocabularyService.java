package org.auscope.portal.core.services;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpClientInputStream;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.VocabularyMethodMaker;
import org.auscope.portal.core.services.methodmakers.VocabularyMethodMaker.Format;
import org.auscope.portal.core.services.methodmakers.VocabularyMethodMaker.View;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.Assert.*;

public class TestVocabularyService extends PortalTestClass {

    private HttpRequestBase mockMethod1 = context.mock(HttpRequestBase.class, "mockMethod1");
    private HttpRequestBase mockMethod2 = context.mock(HttpRequestBase.class, "mockMethod2");
    private HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class);
    private VocabularyMethodMaker mockMethodMaker = context.mock(VocabularyMethodMaker.class);

    private String serviceUrl = "http://example.org:8080/sissvoc/path";
    private String schemeUrl = "http://example.org/classifier/repository/vocabulary-scheme";

    private VocabularyService vocabularyService;

    @Before
    public void setUp() throws Exception {
        vocabularyService = new VocabularyService(mockServiceCaller,mockMethodMaker,serviceUrl);
        vocabularyService.setPageSize(50);
    }

    private static boolean containsResourceUri(List<Resource> list, String uri) {
        for (Resource res : list) {
            if (res.getURI().equals(uri)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testGetAllConcepts() throws PortalServiceException, URISyntaxException, IOException {
        try (final InputStream rs1 = new HttpClientInputStream(ResourceUtil.loadResourceAsStream(
                "org/auscope/portal/core/test/responses/vocabulary/commodityConcepts_MoreData.xml"), null);
             final InputStream rs2 = new HttpClientInputStream(
                     ResourceUtil.loadResourceAsStream(
                             "org/auscope/portal/core/test/responses/vocabulary/commodityConcepts_NoMoreData.xml"),
                     null)) {

            context.checking(new Expectations() {
                {

                    oneOf(mockMethodMaker).getAllConcepts(serviceUrl, Format.Rdf, vocabularyService.getPageSize(), 0);
                    will(returnValue(mockMethod1));
                    oneOf(mockMethodMaker).getAllConcepts(serviceUrl, Format.Rdf, vocabularyService.getPageSize(), 1);
                    will(returnValue(mockMethod2));

                    oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod1);
                    will(returnValue(rs1));
                    oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod2);
                    will(returnValue(rs2));

                    oneOf(mockMethod1).releaseConnection();
                    oneOf(mockMethod2).releaseConnection();
                }
            });

            Model model = vocabularyService.getAllConcepts();
            Assert.assertNotNull(model);
            List<Resource> resources = Lists.newArrayList(model.listSubjects());
            Assert.assertEquals(2, resources.size());
            Assert.assertTrue(
                    containsResourceUri(resources, "http://resource.geosciml.org/classifier/cgi/commodity-code/gold"));
            Assert.assertTrue(
                    containsResourceUri(resources, "http://resource.geosciml.org/classifier/cgi/commodity-code/uranium"));
            Assert.assertFalse(
                    containsResourceUri(resources, "http://resource.geosciml.org/classifier/cgi/commodity-code/not-a-commodity"));
        }
    }

    /**
     * Tests that when iterating a repository, a single failure is reported
     * correctly
     *
     * @throws URISyntaxException
     * @throws PortalServiceException
     * @throws IOException
     */
    @Test(expected = PortalServiceException.class)
    public void testGetAllDescriptionsCommsError() throws PortalServiceException, URISyntaxException, IOException {
        // final String repository = "repository";

        try (final HttpClientInputStream rs1 = new HttpClientInputStream(ResourceUtil.loadResourceAsStream(
                "org/auscope/portal/core/test/responses/vocabulary/commodityConcepts_MoreData.xml"), null)) {

            context.checking(new Expectations() {
                {
                    oneOf(mockMethodMaker).getAllConcepts(serviceUrl, Format.Rdf, vocabularyService.getPageSize(), 0);
                    will(returnValue(mockMethod1));
                    oneOf(mockMethodMaker).getAllConcepts(serviceUrl, Format.Rdf, vocabularyService.getPageSize(), 1);
                    will(returnValue(mockMethod2));

                    oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod1);
                    will(returnValue(rs1));
                    oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod2);
                    will(throwException(new ConnectException("error")));

                    oneOf(mockMethod1).releaseConnection();
                    oneOf(mockMethod2).releaseConnection();
                }
            });

            vocabularyService.getAllConcepts();
        }
    }

    /**
     * Tests that iterating a repository using a schemeUrl works as expected
     *
     * @throws URISyntaxException
     * @throws PortalServiceException
     * @throws IOException
     */
    @Test
    public void testGetAllConceptsInScheme() throws IOException, URISyntaxException, PortalServiceException {
        final InputStream rs1 = new HttpClientInputStream(ResourceUtil.loadResourceAsStream(
                "org/auscope/portal/core/test/responses/vocabulary/timescaleConcepts_MoreData.xml"), null);
        final InputStream rs2 = new HttpClientInputStream(ResourceUtil.loadResourceAsStream(
                "org/auscope/portal/core/test/responses/vocabulary/timescaleConcepts_NoMoreData.xml"), null);

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).getAllConceptsInScheme(serviceUrl, schemeUrl, Format.Rdf, View.all,
                        vocabularyService.getPageSize(), 0);
                will(returnValue(mockMethod1));
                oneOf(mockMethodMaker).getAllConceptsInScheme(serviceUrl, schemeUrl, Format.Rdf, View.all,
                        vocabularyService.getPageSize(), 1);
                will(returnValue(mockMethod2));

                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod1);
                will(returnValue(rs1));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod2);
                will(returnValue(rs2));

                oneOf(mockMethod1).releaseConnection();
                oneOf(mockMethod2).releaseConnection();
            }
        });

        Model model = vocabularyService.getAllConceptsInScheme(schemeUrl, View.all);
        Assert.assertNotNull(model);
        List<Resource> resources = Lists.newArrayList(model.listSubjects());
        Assert.assertEquals(23, resources.size());
        Assert.assertTrue(
                containsResourceUri(resources, "http://resource.geosciml.org/classifier/ics/ischart/Orosirian"));
        Assert.assertTrue(
                containsResourceUri(resources, "http://resource.geosciml.org/classifier/ics/ischart/Rhyacian"));
        Assert.assertFalse(
                containsResourceUri(resources, "http://resource.geosciml.org/classifier/ics/ischart/Nonsensian"));

    }

    @Test(expected = PortalServiceException.class)
    public void testGetAllConceptsInScheme_CommsError() throws IOException, URISyntaxException, PortalServiceException {
        final HttpClientInputStream rs1 = new HttpClientInputStream(ResourceUtil.loadResourceAsStream(
                "org/auscope/portal/core/test/responses/vocabulary/timescaleConcepts_MoreData.xml"), null);

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).getAllConceptsInScheme(serviceUrl, schemeUrl, Format.Rdf, View.all,
                        vocabularyService.getPageSize(), 0);
                will(returnValue(mockMethod1));
                oneOf(mockMethodMaker).getAllConceptsInScheme(serviceUrl, schemeUrl, Format.Rdf, View.all,
                        vocabularyService.getPageSize(), 1);
                will(returnValue(mockMethod2));

                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod1);
                will(returnValue(rs1));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod2);
                will(throwException(new ConnectException("error")));

                oneOf(mockMethod1).releaseConnection();
                oneOf(mockMethod2).releaseConnection();

            }
        });
        vocabularyService.getAllConceptsInScheme(schemeUrl, View.all);
    }

    /**
     * Tests that single resources are extracted correctly
     *
     * @throws PortalServiceException
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testGetResourceByUri() throws PortalServiceException, URISyntaxException, IOException {
        final String uri = "http://resource.geosciml.org/classifier/cgi/mineral-occurrence-type/deposit";

        try (final HttpClientInputStream rs1 = new HttpClientInputStream(ResourceUtil.loadResourceAsStream(
                "org/auscope/portal/core/test/responses/vocabulary/occurrenceType_ResourceRDF.xml"), null)) {

            context.checking(new Expectations() {
                {
                    oneOf(mockMethodMaker).getResourceByUri(serviceUrl, uri, Format.Rdf);
                    will(returnValue(mockMethod1));

                    oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod1);
                    will(returnValue(rs1));

                    oneOf(mockMethod1).releaseConnection();
                }
            });

            Resource res = vocabularyService.getResourceByUri(uri);
            Assert.assertNotNull(res);

            Property skosDefn = res.getModel().createProperty("http://www.w3.org/2004/02/skos/core#", "definition");
            List<Statement> matchingStatements = Lists.newArrayList(res.listProperties(skosDefn));

            boolean foundEnglishDef = false;
            for (Statement statement : matchingStatements) {
                if (statement.getObject().asLiteral().getLanguage().equals("en")) {
                    foundEnglishDef = true;
                    Assert.assertEquals("A mass of naturally occurring material in the Earth that contains an anomalous concentration of some mineral or rock type that has some potential for human utilization, without regard to mode of origin. Typically is a single, connected, genetically related body of material.",
                            statement.getObject().asLiteral().getString());
                }
            }
            Assert.assertTrue("No English skos definition found!", foundEnglishDef);
        }
    }

    /**
     * Tests that getting a resource with a comms error fails gracefully
     *
     * @throws IOException
     * @throws URISyntaxException
     * @throws PortalServiceException
     */
    @Test(expected = PortalServiceException.class)
    public void testGetConceptByUri_CommsError() throws URISyntaxException, IOException, PortalServiceException {
        final String uri = "http://resource.geosciml.org/classifier/cgi/mineral-occurrence-type/deposit";

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).getResourceByUri(serviceUrl, uri, Format.Rdf);
                will(returnValue(mockMethod1));

                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod1);
                will(throwException(new ConnectException("err")));

                oneOf(mockMethod1).releaseConnection();
            }
        });

        vocabularyService.getResourceByUri(uri);
    }
}
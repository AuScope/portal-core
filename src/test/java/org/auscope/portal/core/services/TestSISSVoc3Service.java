package org.auscope.portal.core.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpClientInputStream;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.sissvoc.SISSVoc3MethodMaker;
import org.auscope.portal.core.services.methodmakers.sissvoc.SISSVoc3MethodMaker.Format;
import org.auscope.portal.core.services.methodmakers.sissvoc.SISSVoc3MethodMaker.View;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class TestSISSVoc3Service extends PortalTestClass {
    private HttpRequestBase mockMethod = context.mock(HttpRequestBase.class, "mockMethod");
    private HttpRequestBase mockMethod2 = context.mock(HttpRequestBase.class, "mockMethod2");
    private HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class);
    private SISSVoc3MethodMaker mockMethodMaker = context.mock(SISSVoc3MethodMaker.class);
    private String baseUrl = "http://example.org:8080/sissvoc/path";
    private String schemeUrl = "http://example.org/classifier/repository/vocabulary-scheme";
    private String repository = "repository";

    private SISSVoc3Service service;

    @Before
    public void setup() {
        service = new SISSVoc3Service(mockServiceCaller, mockMethodMaker, baseUrl, repository);
        service.setPageSize(50);
    }

    private static boolean containsResourceUri(List<Resource> list, String uri) {
        for (Resource res : list) {
            if (res.getURI().equals(uri)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests that iterating a repository works as expected
     * 
     * @throws URISyntaxException
     * @throws PortalServiceException
     * @throws IOException
     */
    @Test
    public void testGetAllConcepts() throws PortalServiceException, URISyntaxException, IOException {
        try (final HttpClientInputStream rs1 = new HttpClientInputStream(ResourceUtil.loadResourceAsStream(
                "org/auscope/portal/core/test/responses/sissvoc/SISSVoc3_ConceptsRDF_MoreData.xml"), null);
                final InputStream rs2 = new HttpClientInputStream(
                        ResourceUtil.loadResourceAsStream(
                                "org/auscope/portal/core/test/responses/sissvoc/SISSVoc3_ConceptsRDF_NoMoreData.xml"),
                        null)) {

            context.checking(new Expectations() {
                {

                    oneOf(mockMethodMaker).getAllConcepts(baseUrl, repository, Format.Rdf, service.getPageSize(), 0);
                    will(returnValue(mockMethod));
                    oneOf(mockMethodMaker).getAllConcepts(baseUrl, repository, Format.Rdf, service.getPageSize(), 1);
                    will(returnValue(mockMethod2));

                    oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                    will(returnValue(rs1));
                    oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod2);
                    will(returnValue(rs2));

                    oneOf(mockMethod).releaseConnection();
                    oneOf(mockMethod2).releaseConnection();
                }
            });

            Model model = service.getAllConcepts();
            Assert.assertNotNull(model);
            List<Resource> resources = Lists.newArrayList(model.listSubjects());
            Assert.assertEquals(7, resources.size());
            Assert.assertTrue(
                    containsResourceUri(resources, "http://resource.auscope.org/classifier/AuScope/commodity/Energy"));
            Assert.assertTrue(
                    containsResourceUri(resources, "http://resource.auscope.org/classifier/PIRSA/commodity/U3O8"));
            Assert.assertFalse(
                    containsResourceUri(resources, "http://resource.auscope.org/classifier/GA/Non-Existent-Resource/"));
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
                "org/auscope/portal/core/test/responses/sissvoc/SISSVoc3_ConceptsRDF_MoreData.xml"), null)) {

            context.checking(new Expectations() {
                {
                    oneOf(mockMethodMaker).getAllConcepts(baseUrl, repository, Format.Rdf, service.getPageSize(), 0);
                    will(returnValue(mockMethod));
                    oneOf(mockMethodMaker).getAllConcepts(baseUrl, repository, Format.Rdf, service.getPageSize(), 1);
                    will(returnValue(mockMethod2));

                    oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                    will(returnValue(rs1));
                    oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod2);
                    will(throwException(new ConnectException("error")));

                    oneOf(mockMethod).releaseConnection();
                    oneOf(mockMethod2).releaseConnection();
                }
            });

            service.getAllConcepts();
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
                "org/auscope/portal/core/test/responses/sissvoc/SISSVoc3_ConceptsRDF_MoreData.xml"), null);
        final InputStream rs2 = new HttpClientInputStream(ResourceUtil.loadResourceAsStream(
                "org/auscope/portal/core/test/responses/sissvoc/SISSVoc3_ConceptsRDF_NoMoreData.xml"), null);

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).getAllConceptsInScheme(baseUrl, repository, schemeUrl, Format.Rdf, View.all,
                        service.getPageSize(), 0);
                will(returnValue(mockMethod));
                oneOf(mockMethodMaker).getAllConceptsInScheme(baseUrl, repository, schemeUrl, Format.Rdf, View.all,
                        service.getPageSize(), 1);
                will(returnValue(mockMethod2));

                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(returnValue(rs1));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod2);
                will(returnValue(rs2));

                oneOf(mockMethod).releaseConnection();
                oneOf(mockMethod2).releaseConnection();
            }
        });

        Model model = service.getAllConceptsInScheme(schemeUrl, View.all);
        Assert.assertNotNull(model);
        List<Resource> resources = Lists.newArrayList(model.listSubjects());
        Assert.assertEquals(7, resources.size());
        Assert.assertTrue(
                containsResourceUri(resources, "http://resource.auscope.org/classifier/AuScope/commodity/Energy"));
        Assert.assertTrue(
                containsResourceUri(resources, "http://resource.auscope.org/classifier/PIRSA/commodity/U3O8"));
        Assert.assertFalse(
                containsResourceUri(resources, "http://resource.auscope.org/classifier/GA/Non-Existent-Resource/"));

    }

    @Test(expected = PortalServiceException.class)
    public void testGetAllConceptsInScheme_CommsError() throws IOException, URISyntaxException, PortalServiceException {
        final HttpClientInputStream rs1 = new HttpClientInputStream(ResourceUtil.loadResourceAsStream(
                "org/auscope/portal/core/test/responses/sissvoc/SISSVoc3_ConceptsRDF_MoreData.xml"), null);

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).getAllConceptsInScheme(baseUrl, repository, schemeUrl, Format.Rdf, View.all,
                        service.getPageSize(), 0);
                will(returnValue(mockMethod));
                oneOf(mockMethodMaker).getAllConceptsInScheme(baseUrl, repository, schemeUrl, Format.Rdf, View.all,
                        service.getPageSize(), 1);
                will(returnValue(mockMethod2));

                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(returnValue(rs1));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod2);
                will(throwException(new ConnectException("error")));

                oneOf(mockMethod).releaseConnection();
                oneOf(mockMethod2).releaseConnection();

            }
        });
        service.getAllConceptsInScheme(schemeUrl, View.all);
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
        final String uri = "http://resource.auscope.org/classifier/GA/commodity/Au";

        try (final HttpClientInputStream rs1 = new HttpClientInputStream(ResourceUtil.loadResourceAsStream(
                "org/auscope/portal/core/test/responses/sissvoc/SISSVoc3_ResourceRDF.xml"), null)) {

            context.checking(new Expectations() {
                {
                    oneOf(mockMethodMaker).getResourceByUri(baseUrl, repository, uri, Format.Rdf);
                    will(returnValue(mockMethod));

                    oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                    will(returnValue(rs1));

                    oneOf(mockMethod).releaseConnection();
                }
            });

            Resource res = service.getResourceByUri(uri);
            Assert.assertNotNull(res);

            Property skosDefn = res.getModel().createProperty("http://www.w3.org/2004/02/skos/core#", "definition");
            List<Statement> matchingStatements = Lists.newArrayList(res.listProperties(skosDefn));

            boolean foundEnglishDef = false;
            for (Statement statement : matchingStatements) {
                if (statement.getObject().asLiteral().getLanguage().equals("en")) {
                    foundEnglishDef = true;
                    Assert.assertEquals("Gold is a highly sought-after precious metal in jewelry.",
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
        final String uri = "http://resource.auscope.org/classifier/GA/commodity/Au";

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).getResourceByUri(baseUrl, repository, uri, Format.Rdf);
                will(returnValue(mockMethod));

                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(throwException(new ConnectException("err")));

                oneOf(mockMethod).releaseConnection();
            }
        });

        service.getResourceByUri(uri);
    }
}

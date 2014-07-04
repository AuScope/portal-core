package org.auscope.portal.core.services;

import java.io.InputStream;
import java.net.ConnectException;
import java.util.List;


import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.sissvoc.SISSVoc3MethodMaker;
import org.auscope.portal.core.services.methodmakers.sissvoc.SISSVoc3MethodMaker.Format;
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
    private String repository = "repository";


    private SISSVoc3Service service;

    @Before
    public void setup() {
        service = new SISSVoc3Service(mockServiceCaller, mockMethodMaker, baseUrl, repository);
        service.setPageSize(50);
    }

    private boolean containsResourceUri(List<Resource> list, String uri) {
        for (Resource res : list) {
            if (res.getURI().equals(uri)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests that iterating a repository works as expected
     * @throws Exception
     */
    @Test
    public void testGetAllConcepts() throws Exception {
        final InputStream rs1 = ResourceUtil.loadResourceAsStream("org/auscope/portal/core/test/responses/sissvoc/SISSVoc3_ConceptsRDF_MoreData.xml");
        final InputStream rs2 = ResourceUtil.loadResourceAsStream("org/auscope/portal/core/test/responses/sissvoc/SISSVoc3_ConceptsRDF_NoMoreData.xml");

        context.checking(new Expectations() {{
            oneOf(mockMethodMaker).getAllConcepts(baseUrl, repository, Format.Rdf, service.getPageSize(), 0);will(returnValue(mockMethod));
            oneOf(mockMethodMaker).getAllConcepts(baseUrl, repository, Format.Rdf, service.getPageSize(), 1);will(returnValue(mockMethod2));

            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);will(returnValue(rs1));
            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod2);will(returnValue(rs2));

            oneOf(mockMethod).releaseConnection();
            oneOf(mockMethod2).releaseConnection();
        }});

        Model model = service.getAllConcepts();
        Assert.assertNotNull(model);
        List<Resource> resources = Lists.newArrayList(model.listSubjects());
        Assert.assertEquals(7, resources.size());
        Assert.assertTrue(containsResourceUri(resources, "http://resource.auscope.org/classifier/AuScope/commodity/Energy"));
        Assert.assertTrue(containsResourceUri(resources, "http://resource.auscope.org/classifier/PIRSA/commodity/U3O8"));
        Assert.assertFalse(containsResourceUri(resources, "http://resource.auscope.org/classifier/GA/Non-Existent-Resource/"));
    }

    /**
     * Tests that when iterating a repository, a single failure is reported correctly
     * @throws Exception
     */
    @Test(expected=PortalServiceException.class)
    public void testGetAllDescriptionsCommsError() throws Exception {
        final String repository = "repository";

        final InputStream rs1 = ResourceUtil.loadResourceAsStream("org/auscope/portal/core/test/responses/sissvoc/SISSVoc3_ConceptsRDF_MoreData.xml");

        context.checking(new Expectations() {{
            oneOf(mockMethodMaker).getAllConcepts(baseUrl, repository, Format.Rdf, service.getPageSize(), 0);will(returnValue(mockMethod));
            oneOf(mockMethodMaker).getAllConcepts(baseUrl, repository, Format.Rdf, service.getPageSize(), 1);will(returnValue(mockMethod2));

            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);will(returnValue(rs1));
            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod2);will(throwException(new ConnectException("error")));

            oneOf(mockMethod).releaseConnection();
            oneOf(mockMethod2).releaseConnection();
        }});

        service.getAllConcepts();
    }

    /**
     * Tests that single resources are extracted correctly
     * @throws Exception
     */
    @Test
    public void testGetResourceByUri() throws Exception {
        final String uri = "http://resource.auscope.org/classifier/GA/commodity/Au";

        final InputStream rs1 = ResourceUtil.loadResourceAsStream("org/auscope/portal/core/test/responses/sissvoc/SISSVoc3_ResourceRDF.xml");

        context.checking(new Expectations() {{
            oneOf(mockMethodMaker).getResourceByUri(baseUrl, repository, uri, Format.Rdf);will(returnValue(mockMethod));

            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);will(returnValue(rs1));

            oneOf(mockMethod).releaseConnection();
        }});

        Resource res = service.getResourceByUri(uri);
        Assert.assertNotNull(res);

        Property skosDefn = res.getModel().createProperty( "http://www.w3.org/2004/02/skos/core#", "definition");
        List<Statement> matchingStatements = Lists.newArrayList(res.listProperties(skosDefn));

        boolean foundEnglishDef = false;
        for (Statement statement : matchingStatements) {
            if (statement.getObject().asLiteral().getLanguage().equals("en")) {
                foundEnglishDef = true;
                Assert.assertEquals("Gold is a highly sought-after precious metal in jewelry.", statement.getObject().asLiteral().getString());
            }
        }
        Assert.assertTrue("No English skos definition found!", foundEnglishDef);
    }

    /**
     * Tests that getting a resource with a comms error fails gracefully
     * @throws Exception
     */
    @Test(expected=PortalServiceException.class)
    public void testGetConceptByUri_CommsError() throws Exception {
        final String uri = "http://resource.auscope.org/classifier/GA/commodity/Au";

        context.checking(new Expectations() {{
            oneOf(mockMethodMaker).getResourceByUri(baseUrl, repository, uri, Format.Rdf);will(returnValue(mockMethod));

            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);will(throwException(new ConnectException("err")));

            oneOf(mockMethod).releaseConnection();
        }});

        service.getResourceByUri(uri);
    }
}

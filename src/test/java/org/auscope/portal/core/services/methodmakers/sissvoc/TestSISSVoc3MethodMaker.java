package org.auscope.portal.core.services.methodmakers.sissvoc;

import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.services.methodmakers.sissvoc.SISSVoc3MethodMaker.Format;
import org.auscope.portal.core.services.methodmakers.sissvoc.SISSVoc3MethodMaker.View;
import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestSISSVoc3MethodMaker extends PortalTestClass {
    SISSVoc3MethodMaker mm;

    @Before
    public void setup() {
        mm = new SISSVoc3MethodMaker();
    }

    /**
     * Tests that the optional params for various methods don't generate
     * exceptions
     * 
     * @throws URISyntaxException
     */
    @Test
    public void testOptionalParamErrors() throws URISyntaxException {
        final String url = "http://example.org/vocab";
        final String repository = "repo";
        final String schemeUrl = "http://example.org/classifier/repository/vocabulary-scheme";

        Assert.assertNotNull(mm.getAllConcepts(url, repository, null, null, null));
        Assert.assertNotNull(mm.getAllConceptsInScheme(url, repository, schemeUrl, null, null, null, null));
        Assert.assertNotNull(mm.getConceptsWithLabel(url, repository, "label", null, null, null));
        Assert.assertNotNull(mm.getResourceByUri(url, repository, "uri", null));
        Assert.assertNotNull(mm.getBroaderConcepts(url, repository, "uri", null, null, null));
        Assert.assertNotNull(mm.getNarrowerConcepts(url, repository, "uri", null, null, null));
    }

    /**
     * Ensures getAllConcepts encodes the method correctly
     * 
     * @throws URISyntaxException
     */
    @Test
    public void testGetAllConcepts() throws URISyntaxException {
        final String url = "http://sissvoc.example.org./";
        final String repository = "repo";

        final Format format = Format.Html;
        final Integer pageSize = 23;
        final Integer page = 2;

        HttpRequestBase method = mm.getAllConcepts(url, repository, format, pageSize, page);
        String queryString = method.getURI().getQuery();
        String path = method.getURI().getPath();

        Assert.assertEquals("/repo/concept.html", path);
        Assert.assertTrue(queryString.contains(String.format("_page=%1$s", page)));
        Assert.assertTrue(queryString.contains(String.format("_pageSize=%1$s", pageSize)));
    }

    /**
     * Ensures getAllConceptsInScheme encodes the method correctly
     * 
     * @throws URISyntaxException
     */
    @Test
    public void testGetAllConceptsInScheme() throws URISyntaxException {
        final String url = "http://sissvoc.example.org./";
        final String repository = "repo";
        final String schemeUrl = "http://example.org/classifier/repository/vocabulary-scheme";

        final Format format = Format.Html;
        final View view = View.basic;
        final Integer pageSize = 23;
        final Integer page = 2;

        HttpRequestBase method = mm.getAllConceptsInScheme(url, repository, schemeUrl, format, view, pageSize, page);

        String queryString = method.getURI().getQuery();
        String path = method.getURI().getPath();

        Assert.assertEquals("/repo/concept.html", path);
        Assert.assertTrue(queryString.contains(String.format("_page=%1$s", page)));
        Assert.assertTrue(queryString.contains(String.format("_pageSize=%1$s", pageSize)));
        Assert.assertTrue(queryString.contains(String.format("_view=%1$s", view.name())));
    }

    /**
     * Ensures getAllConcepts encodes the method correctly
     * 
     * @throws URISyntaxException
     */
    @Test
    public void testGetConceptsWithLabel() throws URISyntaxException {
        final String url = "http://sissvoc.example.org./";
        final String repository = "repo";
        final String label = "label";
        final Format format = Format.Ttl;
        final Integer pageSize = 23;
        final Integer page = 2;

        HttpRequestBase method = mm.getConceptsWithLabel(url, repository, label, format, pageSize, page);
        String queryString = method.getURI().getQuery();
        String path = method.getURI().getPath();

        Assert.assertEquals("/repo/concept.ttl", path);
        Assert.assertTrue(queryString.contains(String.format("_page=%1$s", page)));
        Assert.assertTrue(queryString.contains(String.format("_pageSize=%1$s", pageSize)));
        Assert.assertTrue(queryString.contains(String.format("anylabel=%1$s", label)));
    }

    /**
     * Ensures getResourceByUri encodes the method correctly
     * 
     * @throws URISyntaxException
     */
    @Test
    public void testGetConceptsByUri() throws URISyntaxException {
        final String url = "http://sissvoc.example.org./";
        final String repository = "repo";
        final String uri = "uri";
        final Format format = Format.Rdf;

        HttpRequestBase method = mm.getResourceByUri(url, repository, uri, format);
        String queryString = method.getURI().getQuery();
        String path = method.getURI().getPath();

        Assert.assertEquals("/repo/resource.rdf", path);
        Assert.assertTrue(queryString.contains(String.format("uri=%1$s", uri)));
    }

    /**
     * Ensures getBroaderConcepts encodes the method correctly
     * 
     * @throws URISyntaxException
     */
    @Test
    public void testGetBroaderConcepts() throws URISyntaxException {
        final String url = "http://sissvoc.example.org./";
        final String repository = "repo";
        final String baseUri = "base-uri";
        final Format format = Format.Json;
        final Integer pageSize = 23;
        final Integer page = 2;

        HttpRequestBase method = mm.getBroaderConcepts(url, repository, baseUri, format, pageSize, page);
        String queryString = method.getURI().getQuery();
        String path = method.getURI().getPath();

        Assert.assertEquals("/repo/concept/broader.json", path);
        Assert.assertTrue(queryString.contains(String.format("_page=%1$s", page)));
        Assert.assertTrue(queryString.contains(String.format("_pageSize=%1$s", pageSize)));
        Assert.assertTrue(queryString.contains(String.format("uri=%1$s", baseUri)));
    }

    /**
     * Ensures testGetNarrowerConcepts encodes the method correctly
     * 
     * @throws URISyntaxException
     */
    @Test
    public void testGetNarrowerConcepts() throws URISyntaxException {
        final String url = "http://sissvoc.example.org./";
        final String repository = "repo";
        final String baseUri = "base-uri";
        final Format format = Format.Json;
        final Integer pageSize = 23;
        final Integer page = 2;

        HttpRequestBase method = mm.getNarrowerConcepts(url, repository, baseUri, format, pageSize, page);
        String queryString = method.getURI().getQuery();
        String path = method.getURI().getPath();

        Assert.assertEquals("/repo/concept/narrower.json", path);
        Assert.assertTrue(queryString.contains(String.format("_page=%1$s", page)));
        Assert.assertTrue(queryString.contains(String.format("_pageSize=%1$s", pageSize)));
        Assert.assertTrue(queryString.contains(String.format("uri=%1$s", baseUri)));
    }

}

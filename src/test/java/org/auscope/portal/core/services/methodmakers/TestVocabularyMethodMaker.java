package org.auscope.portal.core.services.methodmakers;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class TestVocabularyMethodMaker extends PortalTestClass {
    VocabularyMethodMaker vocabularyMethodMaker;

    @Before
    public void setUp() throws Exception {
        vocabularyMethodMaker = new VocabularyMethodMaker();
    }


    @Test
    public void testOptionalParamErrors() throws URISyntaxException {
        final String url = "http://example.org/vocab";
        final String schemeUrl = "http://example.org/classifier/repository/vocabulary-scheme";

        Assert.assertNotNull(vocabularyMethodMaker.getAllConcepts(url,null, null, null));
        Assert.assertNotNull(vocabularyMethodMaker.getAllConceptsInScheme(url, schemeUrl, null, null, null, null));
        Assert.assertNotNull(vocabularyMethodMaker.getConceptsWithLabel(url, "label", null, null, null));
        Assert.assertNotNull(vocabularyMethodMaker.getResourceByUri(url, "uri", null));
        Assert.assertNotNull(vocabularyMethodMaker.getBroaderConcepts(url,  "uri", null, null, null));
        Assert.assertNotNull(vocabularyMethodMaker.getNarrowerConcepts(url,  "uri", null, null, null));
    }

    /**
     * Ensures getAllConcepts encodes the method correctly
     *
     * @throws URISyntaxException
     */
    @Test
    public void testGetAllConcepts() throws URISyntaxException {
        final String url = "http://sissvoc.example.org./";

        final VocabularyMethodMaker.Format format = VocabularyMethodMaker.Format.Html;
        final Integer pageSize = 23;
        final Integer page = 2;

        HttpRequestBase method = vocabularyMethodMaker.getAllConcepts(url, format, pageSize, page);
        String queryString = method.getURI().getQuery();
        String path = method.getURI().getPath();

        Assert.assertEquals("/concept.html", path);
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
        final String schemeUrl = "http://example.org/classifier/repository/vocabulary-scheme";

        final VocabularyMethodMaker.Format format = VocabularyMethodMaker.Format.Html;
        final VocabularyMethodMaker.View view = VocabularyMethodMaker.View.basic;
        final Integer pageSize = 23;
        final Integer page = 2;

        HttpRequestBase method = vocabularyMethodMaker.getAllConceptsInScheme(url, schemeUrl, format, view, pageSize, page);

        String queryString = method.getURI().getQuery();
        String path = method.getURI().getPath();

        Assert.assertEquals("/concept.html", path);
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
        final String label = "label";
        final VocabularyMethodMaker.Format format = VocabularyMethodMaker.Format.Ttl;
        final Integer pageSize = 23;
        final Integer page = 2;

        HttpRequestBase method = vocabularyMethodMaker.getConceptsWithLabel(url, label, format, pageSize, page);
        String queryString = method.getURI().getQuery();
        String path = method.getURI().getPath();

        Assert.assertEquals("/concept.ttl", path);
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
        final String uri = "uri";
        final VocabularyMethodMaker.Format format = VocabularyMethodMaker.Format.Rdf;

        HttpRequestBase method = vocabularyMethodMaker.getResourceByUri(url, uri, format);
        String queryString = method.getURI().getQuery();
        String path = method.getURI().getPath();

        Assert.assertEquals("/resource.rdf", path);
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
        final String baseUri = "base-uri";
        final VocabularyMethodMaker.Format format = VocabularyMethodMaker.Format.Json;
        final Integer pageSize = 23;
        final Integer page = 2;

        HttpRequestBase method = vocabularyMethodMaker.getBroaderConcepts(url, baseUri, format, pageSize, page);
        String queryString = method.getURI().getQuery();
        String path = method.getURI().getPath();

        Assert.assertEquals("/concept/broader.json", path);
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
        final String baseUri = "base-uri";
        final VocabularyMethodMaker.Format format = VocabularyMethodMaker.Format.Json;
        final Integer pageSize = 23;
        final Integer page = 2;

        HttpRequestBase method = vocabularyMethodMaker.getNarrowerConcepts(url, baseUri, format, pageSize, page);
        String queryString = method.getURI().getQuery();
        String path = method.getURI().getPath();

        Assert.assertEquals("/concept/narrower.json", path);
        Assert.assertTrue(queryString.contains(String.format("_page=%1$s", page)));
        Assert.assertTrue(queryString.contains(String.format("_pageSize=%1$s", pageSize)));
        Assert.assertTrue(queryString.contains(String.format("uri=%1$s", baseUri)));
    }
}
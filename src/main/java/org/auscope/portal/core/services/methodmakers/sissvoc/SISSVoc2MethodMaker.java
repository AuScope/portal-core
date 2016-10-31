package org.auscope.portal.core.services.methodmakers.sissvoc;

import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.auscope.portal.core.services.methodmakers.AbstractMethodMaker;

/**
 * A class for generating HTTP methods that can communicate with a SISSVoc version 2.0 service
 * 
 * @author Josh Vote
 *
 */
public class SISSVoc2MethodMaker extends AbstractMethodMaker {

    /**
     * Generates a method for requesting information about available repositories
     * 
     * @param vocabUrl
     *            The SISSVoc endpoint
     * @return
     */
    public HttpRequestBase getRepositoryInfoMethod(String vocabUrl) {
        return new HttpGet(urlPathConcat(vocabUrl, "RepositoryInfo"));
    }

    /**
     * Generates a method for getting vocabulary concept information based on a particular label
     * 
     * @param vocabUrl
     *            The SISSVoc endpoint
     * @param repository
     *            The repository to query
     * @param label
     *            The label to search with
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase getConceptByLabelMethod(String vocabUrl, String repository, String label)
            throws URISyntaxException {
        HttpGet method = new HttpGet(urlPathConcat(vocabUrl, "getConceptByLabel"));
        URIBuilder builder = new URIBuilder(method.getURI());
        builder.setParameters(parseQuery(repository + "/" + label));        
        method.setURI(builder.build());
        return method;
    }

    private static List <NameValuePair> parseQuery(String query) {
        if (query != null && query.length() > 0) {
            return URLEncodedUtils.parse(query, Consts.UTF_8);
        }
        return null;
    }

    /**
     * Generates a method for getting vocabulary concept information based on a defined URI
     * 
     * @param vocabUrl
     *            The SISSVoc endpoint
     * @param repository
     *            The repository to query
     * @param uri
     *            The URI of the vocabulary concept
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase getConceptByUriMethod(String vocabUrl, String repository, String uri)
            throws URISyntaxException {
        HttpGet method = new HttpGet(urlPathConcat(vocabUrl, "getConceptByURI"));
        URIBuilder builder = new URIBuilder(method.getURI());
        builder.setParameters(parseQuery(repository + "/" + uri));        
        method.setURI(builder.build());
        return method;
    }

    /**
     * Generates a method for getting vocabulary concept information about commodities (ERML specific SISSVoc function).
     *
     * @param vocabUrl
     *            The SISSVoc endpoint
     * @param repository
     *            The repository to query
     * @param commodity
     *            The URI of the commodity to query
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase getCommodityMethod(String vocabUrl, String repository, String commodity)
            throws URISyntaxException {
        HttpGet method = new HttpGet(urlPathConcat(vocabUrl, "getCommodity"));
        URIBuilder builder = new URIBuilder(method.getURI());
        builder.setParameters(parseQuery(repository + "/" + commodity));
        method.setURI(builder.build());
        return method;
    }
}

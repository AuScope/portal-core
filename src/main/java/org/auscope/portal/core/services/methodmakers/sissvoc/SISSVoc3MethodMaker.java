package org.auscope.portal.core.services.methodmakers.sissvoc;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.auscope.portal.core.services.methodmakers.AbstractMethodMaker;

/**
 * A class for generating HTTP methods to communicate with a SISSVoc version 3
 * service
 *
 * @author Josh Vote
 *
 */
public class SISSVoc3MethodMaker extends AbstractMethodMaker {
    /**
     * The response format from the SISSVoc service
     */
    public enum Format {
        /**
         * Request the response styled using HTML
         */
        Html,
        /**
         * Request the response styled using RDF XML
         */
        Rdf,
        /**
         * Request the response styled using JSON
         */
        Json,
        /**
         * Request the response styled using Turtle
         * http://www.w3.org/TeamSubmission/turtle/
         */
        Ttl
    }

    public enum View {
        basic,

        concept,

        description,

        all
    }

    /**
     * Utility method for building a SISSVoc GetMethod
     *
     * @param sissVocUrl
     *            The base URL
     * @param repository
     *            The repository to query
     * @param command
     *            The command to query
     * @param format
     *            The download format
     * @param params
     *            The list of params
     * @return
     * @throws URISyntaxException
     */
    protected HttpGet buildGetMethod(String sissVocUrl, String repository, String command, Format format,
            List<NameValuePair> params) throws URISyntaxException {
        String requestUrl = this.urlPathConcat(sissVocUrl, repository, command);

        if (format != null) {
            switch (format) {
            case Html:
                requestUrl += ".html";
                break;
            case Rdf:
                requestUrl += ".rdf";
                break;
            case Json:
                requestUrl += ".json";
                break;
            case Ttl:
                requestUrl += ".ttl";
                break;
            }
        }

        URIBuilder builder = new URIBuilder(requestUrl);

        for (NameValuePair p : params) {
            builder.setParameter(p.getName(), p.getValue());
        }

        HttpGet method = new HttpGet();
        method.setURI(builder.build());

        return method;
    }

    /**
     * Appends elda params for paging to the specified list
     *
     * @param params
     *            The list to append params to
     * @param pageSize
     * @param pageNumber
     */
    protected void appendPagingParams(List<NameValuePair> params, Integer pageSize, Integer pageNumber) {
        if (pageSize != null) {
            params.add(new BasicNameValuePair("_pageSize", pageSize.toString()));
        }

        if (pageNumber != null) {
            params.add(new BasicNameValuePair("_page", pageNumber.toString()));
        }
    }

    /**
     * Appends the view parameter to the list, in the case where the vocabulary
     * service presents a limited description for a vocabulary by default
     * 
     * @param params
     *            The list to append params to
     * @param view
     *            The view parameter value to append
     */
    protected void appendViewParam(List<NameValuePair> params, String view) {
        params.add(new BasicNameValuePair("_view", view));
    }

    /**
     * Generates a method for requesting all concepts (as rdf:Descriptions) in
     * the specified repository
     *
     * The request supports rudimentary paging of the returned results
     *
     * @param sissVocUrl
     *            The base URL of a SISSVoc service
     * @param repository
     *            The repository name to query
     * @param format
     *            How the response should be structured.
     * @param pageSize
     *            [Optional] How many concepts should be returned per page
     * @param pageNumber
     *            [Optional] The page number to request (0 based)
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase getAllConcepts(String sissVocUrl, String repository, Format format, Integer pageSize,
            Integer pageNumber) throws URISyntaxException {

        return getAllConcepts(sissVocUrl, repository, format, null, pageSize, pageNumber);
    }

    /**
     * Generates a method for requesting all concepts (as rdf:Descriptions) in
     * the specified repository
     *
     * The request supports rudimentary paging of the returned results
     *
     * @param sissVocUrl
     *            The base URL of a SISSVoc service
     * @param repository
     *            The repository name to query
     * @param format
     *            How the response should be structured.
     * @param view
     *            Type of view to be returned
     * @param pageSize
     *            [Optional] How many concepts should be returned per page
     * @param pageNumber
     *            [Optional] The page number to request (0 based)
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase getAllConcepts(String sissVocUrl, String repository, Format format, View view,
            Integer pageSize, Integer pageNumber) throws URISyntaxException {

        List<NameValuePair> params = new ArrayList<>();

        appendPagingParams(params, pageSize, pageNumber);

        if (view != null) {
            appendViewParam(params, view.name());
        }

        return buildGetMethod(sissVocUrl, repository, "concept", format, params);
    }

    /**
     * Generates a method for requesting all concepts (as rdf:Descriptions) in
     * the specified repository that below to the scheme requested
     *
     * The request supports rudimentary paging of the returned results
     *
     * @param sissVocUrl
     *            The base URL of a SISSVoc service
     * @param repository
     *            The repository name to query
     * @param schemeUrl
     *            The scheme the vocabulary is in
     * @param format
     *            How the response should be structured.
     * @param pageSize
     *            [Optional] How many concepts should be returned per page
     * @param pageNumber
     *            [Optional] The page number to request (0 based)
     * @return
     * @throws URISyntaxException
     */

    public HttpRequestBase getAllConceptsInScheme(String sissVocUrl, String repository, String schemeUrl, Format format,
            View view, Integer pageSize, Integer pageNumber) throws URISyntaxException {

        List<NameValuePair> params = new ArrayList<>();

        params.add(new BasicNameValuePair("inScheme", schemeUrl));
        appendPagingParams(params, pageSize, pageNumber);
        if (view != null) {
            appendViewParam(params, view.name());
        }
        return buildGetMethod(sissVocUrl, repository, "concept", format, params);

    }

    /**
     * Generates a method for requesting all concepts (as rdf:Descriptions) that
     * match label in the specified repository
     *
     * The request supports rudimentary paging of the returned results
     *
     * @param sissVocUrl
     *            The base URL of a SISSVoc service
     * @param repository
     *            The repository name to query
     * @param format
     *            How the response should be structured.
     * @param label
     *            The label to lookup
     * @param pageSize
     *            [Optional] How many concepts should be returned per page
     * @param pageNumber
     *            [Optional] The page number to request (0 based)
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase getConceptsWithLabel(String sissVocUrl, String repository, String label, Format format,
            Integer pageSize, Integer pageNumber) throws URISyntaxException {
        List<NameValuePair> params = new ArrayList<>();

        appendPagingParams(params, pageSize, pageNumber);
        params.add(new BasicNameValuePair("anylabel", label));

        return buildGetMethod(sissVocUrl, repository, "concept", format, params);
    }

    /**
     * Generates a method for the concept with matching URI from the specified
     * repository
     *
     * @param sissVocUrl
     *            The base URL of a SISSVoc service
     * @param repository
     *            The repository name to query
     * @param format
     *            How the response should be structured.
     * @param conceptUri
     *            The URI of the concept to lookup
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase getResourceByUri(String sissVocUrl, String repository, String conceptUri, Format format)
            throws URISyntaxException {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("uri", conceptUri));

        return buildGetMethod(sissVocUrl, repository, "resource", format, params);
    }

    /**
     * Generates a method for requesting all concepts (as rdf:Descriptions) that
     * are broader than the specified concept as defined by skos:broader
     *
     * The request supports rudimentary paging of the returned results
     *
     * @param sissVocUrl
     *            The base URL of a SISSVoc service
     * @param repository
     *            The repository name to query
     * @param format
     *            How the response should be structured.
     * @param baseConceptUri
     *            The URI of a concept from which to base this request
     * @param pageSize
     *            [Optional] How many concepts should be returned per page
     * @param pageNumber
     *            [Optional] The page number to request (0 based)
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase getBroaderConcepts(String sissVocUrl, String repository, String baseConceptUri,
            Format format, Integer pageSize, Integer pageNumber) throws URISyntaxException {
        List<NameValuePair> params = new ArrayList<>();

        appendPagingParams(params, pageSize, pageNumber);
        params.add(new BasicNameValuePair("uri", baseConceptUri));

        return buildGetMethod(sissVocUrl, repository, "concept/broader", format, params);
    }

    /**
     * Generates a method for requesting all concepts (as rdf:Descriptions) that
     * are narrower than the specified concept as defined by skos:narrower
     *
     * The request supports rudimentary paging of the returned results
     *
     * @param sissVocUrl
     *            The base URL of a SISSVoc service
     * @param repository
     *            The repository name to query
     * @param format
     *            How the response should be structured.
     * @param baseConceptUri
     *            The URI of a concept from which to base this request
     * @param pageSize
     *            [Optional] How many concepts should be returned per page
     * @param pageNumber
     *            [Optional] The page number to request (0 based)
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase getNarrowerConcepts(String sissVocUrl, String repository, String baseConceptUri,
            Format format, Integer pageSize, Integer pageNumber) throws URISyntaxException {
        List<NameValuePair> params = new ArrayList<>();

        appendPagingParams(params, pageSize, pageNumber);
        params.add(new BasicNameValuePair("uri", baseConceptUri));

        return buildGetMethod(sissVocUrl, repository, "concept/narrower", format, params);
    }

}

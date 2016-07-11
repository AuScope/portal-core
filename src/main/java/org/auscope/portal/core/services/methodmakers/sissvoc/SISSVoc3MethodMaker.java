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
 * A class for generating HTTP methods to communicate with a SISSVoc version 3 service
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
         * Request the response styled using Turtle http://www.w3.org/TeamSubmission/turtle/
         */
        Ttl
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
    protected HttpGet buildGetMethod(final String sissVocUrl, final String repository, final String command, final Format format,
            final List<NameValuePair> params) throws URISyntaxException {
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

        final URIBuilder builder = new URIBuilder(requestUrl);

        for (final NameValuePair p : params) {
            builder.setParameter(p.getName(), p.getValue());
        }

        final HttpGet method = new HttpGet();
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
    protected void appendPagingParams(final List<NameValuePair> params, final Integer pageSize, final Integer pageNumber) {
        if (pageSize != null) {
            params.add(new BasicNameValuePair("_pageSize", pageSize.toString()));
        }

        if (pageNumber != null) {
            params.add(new BasicNameValuePair("_page", pageNumber.toString()));
        }
    }

    /**
     * Generates a method for requesting all concepts (as rdf:Descriptions) in the specified repository
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
    public HttpRequestBase getAllConcepts(final String sissVocUrl, final String repository, final Format format, final Integer pageSize,
            final Integer pageNumber) throws URISyntaxException {
        final List<NameValuePair> params = new ArrayList<>();

        appendPagingParams(params, pageSize, pageNumber);

        return buildGetMethod(sissVocUrl, repository, "concept", format, params);
    }

    /**
     * Generates a method for requesting all concepts (as rdf:Descriptions) that match label in the specified repository
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
    public HttpRequestBase getConceptsWithLabel(final String sissVocUrl, final String repository, final String label, final Format format,
            final Integer pageSize, final Integer pageNumber) throws URISyntaxException {
        final List<NameValuePair> params = new ArrayList<>();

        appendPagingParams(params, pageSize, pageNumber);
        params.add(new BasicNameValuePair("anylabel", label));

        return buildGetMethod(sissVocUrl, repository, "concept", format, params);
    }

    /**
     * Generates a method for the concept with matching URI from the specified repository
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
    public HttpRequestBase getResourceByUri(final String sissVocUrl, final String repository, final String conceptUri, final Format format)
            throws URISyntaxException {
        final List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("uri", conceptUri));

        return buildGetMethod(sissVocUrl, repository, "resource", format, params);
    }

    /**
     * Generates a method for requesting all concepts (as rdf:Descriptions) that are broader than the specified concept as defined by skos:broader
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
    public HttpRequestBase getBroaderConcepts(final String sissVocUrl, final String repository, final String baseConceptUri,
            final Format format, final Integer pageSize, final Integer pageNumber) throws URISyntaxException {
        final List<NameValuePair> params = new ArrayList<>();

        appendPagingParams(params, pageSize, pageNumber);
        params.add(new BasicNameValuePair("uri", baseConceptUri));

        return buildGetMethod(sissVocUrl, repository, "concept/broader", format, params);
    }

    /**
     * Generates a method for requesting all concepts (as rdf:Descriptions) that are narrower than the specified concept as defined by skos:narrower
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
    public HttpRequestBase getNarrowerConcepts(final String sissVocUrl, final String repository, final String baseConceptUri,
            final Format format, final Integer pageSize, final Integer pageNumber) throws URISyntaxException {
        final List<NameValuePair> params = new ArrayList<>();

        appendPagingParams(params, pageSize, pageNumber);
        params.add(new BasicNameValuePair("uri", baseConceptUri));

        return buildGetMethod(sissVocUrl, repository, "concept/narrower", format, params);
    }
}

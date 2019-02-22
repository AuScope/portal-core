package org.auscope.portal.core.services.methodmakers;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class for generating HTTP methods to communicate with a vocabulary
 * service that uses the Linked Data API
 *
 * @author Josh Vote
 *
 */
public class VocabularyMethodMaker extends AbstractMethodMaker {
    /**
     * The response format from the vocabulary service
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
     * @param serviceUrl
     *            The base URL
     * @param command
     *            The command to query
     * @param format
     *            The download format
     * @param params
     *            The list of params
     * @return
     * @throws URISyntaxException
     */
    protected HttpGet buildGetMethod(String serviceUrl, String command, Format format,
            List<NameValuePair> params) throws URISyntaxException {
        String requestUrl = this.urlPathConcat(serviceUrl, command);

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
     * Generates a method for requesting all concepts (as rdf:Descriptions) at
     * the specified service
     *
     * The request supports rudimentary paging of the returned results
     *
     * @param serviceUrl
     *            The base URL of a vocabulary service
     * @param format
     *            How the response should be structured.
     * @param pageSize
     *            [Optional] How many concepts should be returned per page
     * @param pageNumber
     *            [Optional] The page number to request (0 based)
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase getAllConcepts(String serviceUrl, Format format, Integer pageSize,
            Integer pageNumber) throws URISyntaxException {

        return getAllConcepts(serviceUrl, format, null, pageSize, pageNumber);
    }

    /**
     * Generates a method for requesting all concepts (as rdf:Descriptions) in
     * the specified service
     *
     * The request supports rudimentary paging of the returned results
     *
     * @param serviceUrl
     *            The base URL of a vocabulary service
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
    public HttpRequestBase getAllConcepts(String serviceUrl, Format format, View view,
            Integer pageSize, Integer pageNumber) throws URISyntaxException {

        List<NameValuePair> params = new ArrayList<>();

        appendPagingParams(params, pageSize, pageNumber);

        if (view != null) {
            appendViewParam(params, view.name());
        }
        params.add(new BasicNameValuePair("_lang", "en"));

        return buildGetMethod(serviceUrl, "concept", format, params);
    }

    /**
     * Generates a method for requesting all concepts (as rdf:Descriptions) in
     * the specified service that below to the scheme requested
     *
     * The request supports rudimentary paging of the returned results
     *
     * @param serviceUrl
     *            The base URL of a vocabulary service
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

    public HttpRequestBase getAllConceptsInScheme(String serviceUrl, String schemeUrl, Format format,
            View view, Integer pageSize, Integer pageNumber) throws URISyntaxException {

        List<NameValuePair> params = new ArrayList<>();

        params.add(new BasicNameValuePair("inScheme", schemeUrl));
        appendPagingParams(params, pageSize, pageNumber);
        if (view != null) {
            appendViewParam(params, view.name());
        }
        return buildGetMethod(serviceUrl, "concept", format, params);

    }

    /**
     * Generates a method for requesting all concepts (as rdf:Descriptions) that
     * match label in the specified service
     *
     * The request supports rudimentary paging of the returned results
     *
     * @param serviceUrl
     *            The base URL of a vocabulary service
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
    public HttpRequestBase getConceptsWithLabel(String serviceUrl, String label, Format format,
            Integer pageSize, Integer pageNumber) throws URISyntaxException {
        List<NameValuePair> params = new ArrayList<>();

        appendPagingParams(params, pageSize, pageNumber);
        params.add(new BasicNameValuePair("anylabel", label));

        return buildGetMethod(serviceUrl, "concept", format, params);
    }

    /**
     * Generates a method for the concept with matching URI from the specified
     * service
     *
     * @param serviceUrl
     *            The base URL of a vocabulary service
     * @param format
     *            How the response should be structured.
     * @param conceptUri
     *            The URI of the concept to lookup
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase getResourceByUri(String serviceUrl, String conceptUri, Format format)
            throws URISyntaxException {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("uri", conceptUri));

        return buildGetMethod(serviceUrl, "resource", format, params);
    }

    /**
     * Generates a method for requesting all concepts (as rdf:Descriptions) that
     * are broader than the specified concept as defined by skos:broader
     *
     * The request supports rudimentary paging of the returned results
     *
     * @param serviceUrl
     *            The base URL of a vocabulary service
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
    public HttpRequestBase getBroaderConcepts(String serviceUrl, String baseConceptUri,
            Format format, Integer pageSize, Integer pageNumber) throws URISyntaxException {
        List<NameValuePair> params = new ArrayList<>();

        appendPagingParams(params, pageSize, pageNumber);
        params.add(new BasicNameValuePair("uri", baseConceptUri));

        return buildGetMethod(serviceUrl,"concept/broader", format, params);
    }

    /**
     * @param serviceUrl
     * @param baseConceptUri
     * @param format
     * @param pageSize
     * @param pageNumber
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase getBroaderTransitiveConcepts(String serviceUrl, String baseConceptUri,
                                              Format format, Integer pageSize, Integer pageNumber) throws URISyntaxException {
        List<NameValuePair> params = new ArrayList<>();

        appendPagingParams(params, pageSize, pageNumber);
        params.add(new BasicNameValuePair("uri", baseConceptUri));

        return buildGetMethod(serviceUrl,"concept/broaderTransitive", format, params);
    }

    /**
     * Generates a method for requesting all concepts (as rdf:Descriptions) that
     * are narrower than the specified concept as defined by skos:narrower
     *
     * The request supports rudimentary paging of the returned results
     *
     * @param serviceUrl
     *            The base URL of a vocabulary service
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
    public HttpRequestBase getNarrowerConcepts(String serviceUrl, String baseConceptUri,
            Format format, Integer pageSize, Integer pageNumber) throws URISyntaxException {
        List<NameValuePair> params = new ArrayList<>();

        appendPagingParams(params, pageSize, pageNumber);
        params.add(new BasicNameValuePair("uri", baseConceptUri));

        return buildGetMethod(serviceUrl,"concept/narrower", format, params);
    }


    /**
     * @param serviceUrl
     * @param baseConceptUri
     * @param format
     * @param pageSize
     * @param pageNumber
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase getNarrowerTransitiveConcepts(String serviceUrl, String baseConceptUri,
                                               Format format, View view, Integer pageSize, Integer pageNumber) throws URISyntaxException {
        List<NameValuePair> params = new ArrayList<>();

        appendPagingParams(params, pageSize, pageNumber);
        params.add(new BasicNameValuePair("uri", baseConceptUri));


        return buildGetMethod(serviceUrl,"concept/narrowerTransitive", format, params);
    }
}

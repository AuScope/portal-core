package org.auscope.portal.core.services.methodmakers.sissvoc;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.auscope.portal.core.services.methodmakers.AbstractMethodMaker;

/**
 * A class for generating HTTP methods to communicate with a SISSVoc version 3 service
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
     * @param sissVocUrl The base URL
     * @param repository The repository to query
     * @param command The command to query
     * @param format The download format
     * @param params The list of params
     * @return
     */
    private GetMethod buildGetMethod(String sissVocUrl, String repository, String command, Format format, List<NameValuePair> params) {
        String requestUrl = this.urlPathConcat(sissVocUrl, repository, command);

        if (format != null) {
            switch(format) {
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

        GetMethod method = new GetMethod(requestUrl);

        if (params != null && params.size() > 0) {
            method.setQueryString(params.toArray(new NameValuePair[params.size()]));
        }

        return method;
    }

    /**
     * Appends elda params for paging to the specified list
     * @param params The list to append params to
     * @param pageSize
     * @param pageNumber
     */
    private void appendPagingParams(List<NameValuePair> params, Integer pageSize, Integer pageNumber) {
        if (pageSize != null) {
            params.add(new NameValuePair("_pageSize", pageSize.toString()));
        }

        if (pageNumber != null) {
            params.add(new NameValuePair("_page", pageNumber.toString()));
        }
    }


    /**
     * Generates a method for requesting all concepts (as rdf:Descriptions) in the specified repository
     *
     * The request supports rudimentary paging of the returned results
     *
     * @param sissVocUrl The base URL of a SISSVoc service
     * @param repository The repository name to query
     * @param format How the response should be structured.
     * @param pageSize [Optional] How many concepts should be returned per page
     * @param pageNumber [Optional] The page number to request (0 based)
     * @return
     */
    public HttpMethodBase getAllConcepts(String sissVocUrl, String repository, Format format, Integer pageSize, Integer pageNumber) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        appendPagingParams(params, pageSize, pageNumber);

        return buildGetMethod(sissVocUrl, repository, "concept", format, params);
    }

    /**
     * Generates a method for requesting all concepts (as rdf:Descriptions) that match label in the specified repository
     *
     * The request supports rudimentary paging of the returned results
     *
     * @param sissVocUrl The base URL of a SISSVoc service
     * @param repository The repository name to query
     * @param format How the response should be structured.
     * @param label The label to lookup
     * @param pageSize [Optional] How many concepts should be returned per page
     * @param pageNumber [Optional] The page number to request (0 based)
     * @return
     */
    public HttpMethodBase getConceptsWithLabel(String sissVocUrl, String repository, String label, Format format, Integer pageSize, Integer pageNumber) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        appendPagingParams(params, pageSize, pageNumber);
        params.add(new NameValuePair("anylabel", label));

        return buildGetMethod(sissVocUrl, repository, "concept", format, params);
    }

    /**
     * Generates a method for the concept with matching URI from the specified repository
     *
     * @param sissVocUrl The base URL of a SISSVoc service
     * @param repository The repository name to query
     * @param format How the response should be structured.
     * @param conceptUri The URI of the concept to lookup
     * @return
     */
    public HttpMethodBase getResourceByUri(String sissVocUrl, String repository, String conceptUri, Format format) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValuePair("uri", conceptUri));

        return buildGetMethod(sissVocUrl, repository, "resource", format, params);
    }

    /**
     * Generates a method for requesting all concepts (as rdf:Descriptions) that are broader than the specified concept
     * as defined by skos:broader
     *
     * The request supports rudimentary paging of the returned results
     *
     * @param sissVocUrl The base URL of a SISSVoc service
     * @param repository The repository name to query
     * @param format How the response should be structured.
     * @param baseConceptUri The URI of a concept from which to base this request
     * @param pageSize [Optional] How many concepts should be returned per page
     * @param pageNumber [Optional] The page number to request (0 based)
     * @return
     */
    public HttpMethodBase getBroaderConcepts(String sissVocUrl, String repository, String baseConceptUri, Format format, Integer pageSize, Integer pageNumber) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        appendPagingParams(params, pageSize, pageNumber);
        params.add(new NameValuePair("uri", baseConceptUri));

        return buildGetMethod(sissVocUrl, repository, "concept/broader", format, params);
    }

    /**
     * Generates a method for requesting all concepts (as rdf:Descriptions) that are narrower than the specified concept
     * as defined by skos:narrower
     *
     * The request supports rudimentary paging of the returned results
     *
     * @param sissVocUrl The base URL of a SISSVoc service
     * @param repository The repository name to query
     * @param format How the response should be structured.
     * @param baseConceptUri The URI of a concept from which to base this request
     * @param pageSize [Optional] How many concepts should be returned per page
     * @param pageNumber [Optional] The page number to request (0 based)
     * @return
     */
    public HttpMethodBase getNarrowerConcepts(String sissVocUrl, String repository, String baseConceptUri, Format format, Integer pageSize, Integer pageNumber) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        appendPagingParams(params, pageSize, pageNumber);
        params.add(new NameValuePair("uri", baseConceptUri));

        return buildGetMethod(sissVocUrl, repository, "concept/narrower", format, params);
    }
}

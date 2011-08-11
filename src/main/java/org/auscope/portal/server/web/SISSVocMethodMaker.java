package org.auscope.portal.server.web;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.stereotype.Repository;

/**
 * A class for generating HTTP methods that can communicate with a SISSVoc service
 * @author Josh Vote
 *
 */
@Repository
public class SISSVocMethodMaker {

    /**
     * Concatenates a path element onto the end of url
     * @param url The base URL (which must be ending in a path)
     * @param newPath The path to concat
     * @return
     */
    private String urlPathConcat(String url, String newPath) {
        if (url.charAt(url.length() - 1) != '/') {
            url = url + "/";
        }

        return url + newPath;
    }

    /**
     * Generates a method for requesting information about available repositories
     * @param vocabUrl The SISSVoc endpoint
     * @return
     */
    public HttpMethodBase getRepositoryInfoMethod(String vocabUrl) {
        return new GetMethod(urlPathConcat(vocabUrl, "RepositoryInfo"));
    }

    /**
     * Generates a method for getting vocabulary concept information based on a particular label
     * @param vocabUrl The SISSVoc endpoint
     * @param repository The repository to query
     * @param label The label to search with
     * @return
     */
    public HttpMethodBase getConceptByLabelMethod(String vocabUrl, String repository, String label) {
        GetMethod method = new GetMethod(urlPathConcat(vocabUrl, "getConceptByLabel"));
        method.setQueryString(repository + "/" + label);
        return method;
    }

    /**
     * Generates a method for getting vocabulary concept information based on a defined URI
     * @param vocabUrl The SISSVoc endpoint
     * @param repository The repository to query
     * @param uri The URI of the vocabulary concept
     * @return
     */
    public HttpMethodBase getConceptByUriMethod(String vocabUrl, String repository, String uri) {
        GetMethod method = new GetMethod(urlPathConcat(vocabUrl, "getConceptByURI"));
        method.setQueryString(repository + "/" + uri);
        return method;
    }

    /**
     * Generates a method for getting vocabulary concept information about
     * commodities (ERML specific SISSVoc function).
     *
     * @param vocabUrl The SISSVoc endpoint
     * @param repository The repository to query
     * @param commodity The URI of the commodity to query
     * @return
     */
    public HttpMethodBase getCommodityMethod(String vocabUrl, String repository, String commodity) {
        GetMethod method = new GetMethod(urlPathConcat(vocabUrl, "getCommodity"));
        method.setQueryString(repository + "/" + commodity);
        return method;
    }
}

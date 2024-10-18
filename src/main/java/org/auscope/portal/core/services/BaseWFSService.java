package org.auscope.portal.core.services;

import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker.ResultType;
import org.auscope.portal.core.services.responses.ows.OWSExceptionParser;
import org.auscope.portal.core.services.responses.wfs.WFSResponse;

/**
 * An abstract base class containing common functionality for all Service classes that intend to interact with a one or more Web Feature Services.
 *
 * @author Josh Vote
 */
public abstract class BaseWFSService {
    /**
     * The default spatial reference system to be used if none is specified
     */
    public static final String DEFAULT_SRS = "EPSG:4326";

    protected HttpServiceCaller httpServiceCaller;
    protected WFSGetFeatureMethodMaker wfsMethodMaker;

    /**
     * Creates a new instance of this class with the specified dependencies
     *
     * @param httpServiceCaller
     *            Will be used for making requests
     * @param wfsMethodMaker
     *            Will be used for generating WFS methods
     */
    public BaseWFSService(HttpServiceCaller httpServiceCaller,
            WFSGetFeatureMethodMaker wfsMethodMaker) {
        this.httpServiceCaller = httpServiceCaller;
        this.wfsMethodMaker = wfsMethodMaker;
    }

    /**
     * Utility method for choosing the correct WFS method to generate based on specified parameters
     *
     * @param wfsUrl
     *            [required] - the web feature service url
     * @param featureType
     *            [required] - the type name
     * @param featureId
     *            [optional] - A unique ID of a single feature type to query
     * @param filterString
     *            [optional] - A OGC filter string to constrain the request
     * @param maxFeatures
     *            [optional] - A maximum number of features to request
     * @param srs
     *            [optional] - The spatial reference system the response should be encoded to. If unspecified BaseWFSService.DEFAULT_SRS will be used
     * @param resultType
     *            [optional] - Whether to request all features (default) or just the count
     * @return
     * @throws URISyntaxException
     */
    protected HttpRequestBase generateWFSRequest(String wfsUrl, String featureType, String featureId,
            String filterString, Integer maxFeatures, String srs, ResultType resultType) throws URISyntaxException {
        return generateWFSRequest(wfsUrl, featureType, featureId, filterString, maxFeatures, srs, resultType, null,
                null);
    }

    protected HttpRequestBase generateWFSRequest(String wfsUrl, String featureType, String featureId,
            String filterString, Integer maxFeatures, String srs, ResultType resultType, String outputFormat)
                    throws URISyntaxException {
        return generateWFSRequest(wfsUrl, featureType, featureId, filterString, maxFeatures, srs, resultType,
                outputFormat, null);
    }

    /**
     * Utility method for choosing the correct WFS method to generate based on specified parameters
     *
     * @param wfsUrl
     *            [required] - the web feature service url
     * @param featureType
     *            [required] - the type name
     * @param featureId
     *            [optional] - A unique ID of a single feature type to query
     * @param filterString
     *            [optional] - A OGC filter string to constrain the request
     * @param maxFeatures
     *            [optional] - A maximum number of features to request
     * @param srs
     *            [optional] - The spatial reference system the response should be encoded to. If unspecified BaseWFSService.DEFAULT_SRS will be used
     * @param resultType
     *            [optional] - Whether to request all features (default) or just the count
     * @param outputFormat
     *            [optional] - The format the response should take
     * @return
     * @throws URISyntaxException
     */
    protected HttpRequestBase generateWFSRequest(String wfsUrl, String featureType, String featureId,
            String filterString, Integer maxFeatures, String srs, ResultType resultType, String outputFormat,
            String startIndex) throws URISyntaxException {
        int max = maxFeatures == null ? 0 : maxFeatures.intValue();

        //apply default value for srs
        if (srs == null || srs.isEmpty()) {
            srs = DEFAULT_SRS;
        }

        if (featureId == null) {
            return wfsMethodMaker.makePostMethod(wfsUrl, featureType, filterString, max, srs, resultType, outputFormat,
                    startIndex);
        } else {
            return wfsMethodMaker.makeGetMethod(wfsUrl, featureType, featureId, srs, outputFormat);
        }
    }

    /**
     * Executes a method that returns GML wrapped in a WFS response, converts that response using transformer and returns the lot bundled in a
     * WFSTransformedResponse
     *
     * @param method
     *            a WFS GetFeature request
     * @return
     * @throws PortalServiceException
     */
    protected WFSResponse getWFSResponse(HttpRequestBase method) throws PortalServiceException {
        try {
            //Make the request and parse the response
            String responseString = httpServiceCaller.getMethodResponseAsString(method);
            OWSExceptionParser.checkForExceptionResponse(responseString);

            return new WFSResponse(responseString, method);
        } catch (Exception ex) {
            throw new PortalServiceException(method, ex);
        }
    }

    /**
     * Download a wfs based on the type and filter.
     *
     * @param serviceUrl
     *            a Web Feature Service URL
     * @param type
     * @param filterString
     * @param maxFeatures
     *            The maximum number of features to request
     * @return
     * @throws PortalServiceException
     */
    public InputStream downloadWFS(String serviceUrl, String type, String filterString, Integer maxFeatures)
            throws PortalServiceException {

        HttpRequestBase method = null;
        try {

            method = generateWFSRequest(serviceUrl, type, null, filterString, maxFeatures, null, ResultType.Results);
            return httpServiceCaller.getMethodResponseAsStream(method);

        } catch (Exception ex) {
            throw new PortalServiceException(method, "Error when attempting to download from:" + serviceUrl, ex);
        }
    }

    /**
     * Download a CSV based on the type and filter.
     *
     * @param serviceUrl
     *            a Web Feature Service URL
     * @param type
     * @param filterString
     * @param maxFeatures
     *            The maximum number of features to request
     * @return
     * @throws PortalServiceException
     */
    public InputStream downloadCSV(String serviceUrl, String type, String filterString, Integer maxFeatures)
            throws PortalServiceException {

        HttpRequestBase method = null;
        try {

            method = generateWFSRequest(serviceUrl, type, null, filterString, maxFeatures, null, ResultType.Results,
                    "csv");
            return httpServiceCaller.getMethodResponseAsStream(method);

        } catch (Exception ex) {
            throw new PortalServiceException(method, "Error when attempting to download from:" + serviceUrl, ex);
        }
    }
    /**
     * Download a CSV based on the type and bbox.
     *
     * @param serviceUrl
     *            a Web Feature Service URL
     * @param type
     * @param bbox
     * @param maxFeatures
     *            The maximum number of features to request
     * @return
     * @throws PortalServiceException
     */
    public InputStream downloadCSVByBBox(String serviceUrl, String typeName, String bbox, Integer maxFeatures)
            throws PortalServiceException {

        HttpRequestBase method = null;
        try {

            method = wfsMethodMaker.makeCSVDownloadByBBoxMethod(serviceUrl, typeName, bbox, maxFeatures);
            return httpServiceCaller.getMethodResponseAsStream(method);

        } catch (Exception ex) {
            throw new PortalServiceException(method, "Error when attempting to download from:" + serviceUrl, ex);
        }
    }
    /**
     * Download a CSV based on the type and polygonFilter.
     *
     * @param serviceUrl
     *            a Web Feature Service URL
     * @param type
     * @param filterString
     *            Polygon OGC filter
     * @param maxFeatures
     *            The maximum number of features to request
     * @return
     * @throws PortalServiceException
     */

    public InputStream downloadCSVByPolygonFilter(String serviceUrl, String typeName, String filterString, Integer maxFeatures) 
            throws PortalServiceException {
        
        HttpRequestBase method = null;
        try {
            method = wfsMethodMaker.makeCSVDownloadByPolygonMethod(serviceUrl, typeName, filterString, maxFeatures);
            return httpServiceCaller.getMethodResponseAsStream(method);

        } catch (Exception ex) {
            throw new PortalServiceException(method, "Error when attempting to downloadCSVByPolygonFilter from:" + serviceUrl, ex);
        }
    }
}

package org.auscope.portal.core.services;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker.ResultType;
import org.auscope.portal.core.services.namespaces.WFSNamespaceContext;
import org.auscope.portal.core.services.responses.ows.OWSExceptionParser;
import org.auscope.portal.core.services.responses.wfs.WFSCountResponse;
import org.auscope.portal.core.services.responses.wfs.WFSGetCapabilitiesResponse;
import org.auscope.portal.core.services.responses.wfs.WFSResponse;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    public BaseWFSService(final HttpServiceCaller httpServiceCaller,
            final WFSGetFeatureMethodMaker wfsMethodMaker) {
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
     * @throws Exception
     */
    protected HttpRequestBase generateWFSRequest(final String wfsUrl, final String featureType, final String featureId,
            final String filterString, final Integer maxFeatures, final String srs, final ResultType resultType) throws URISyntaxException {
        return generateWFSRequest(wfsUrl, featureType, featureId, filterString, maxFeatures, srs, resultType, null,
                null);
    }

    protected HttpRequestBase generateWFSRequest(final String wfsUrl, final String featureType, final String featureId,
            final String filterString, final Integer maxFeatures, final String srs, final ResultType resultType, final String outputFormat)
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
     * @throws Exception
     */
    protected HttpRequestBase generateWFSRequest(final String wfsUrl, final String featureType, final String featureId,
            final String filterString, final Integer maxFeatures, String srs, final ResultType resultType, final String outputFormat,
            final String startIndex) throws URISyntaxException {
        final int max = maxFeatures == null ? 0 : maxFeatures.intValue();

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
     * Makes a WFS GetFeature request represented by method, only the count of features will be returned
     *
     * @param method
     * @return
     * @throws PortalServiceException
     */
    protected WFSCountResponse getWfsFeatureCount(final HttpRequestBase method) throws PortalServiceException {
        try (InputStream responseStream = httpServiceCaller.getMethodResponseAsStream(method)) {
            final Document responseDoc = DOMUtil.buildDomFromStream(responseStream);
            OWSExceptionParser.checkForExceptionResponse(responseDoc);

            final XPathExpression xPath = DOMUtil.compileXPathExpr("wfs:FeatureCollection/@numberOfFeatures",
                    new WFSNamespaceContext());
            final Node numNode = (Node) xPath.evaluate(responseDoc, XPathConstants.NODE);
            final int numNodeValue = Integer.parseInt(numNode.getTextContent());

            return new WFSCountResponse(numNodeValue);
        } catch (final Exception ex) {
            throw new PortalServiceException(method, ex);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
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
    protected WFSResponse getWFSResponse(final HttpRequestBase method) throws PortalServiceException {
        try {
            //Make the request and parse the response
            final String responseString = httpServiceCaller.getMethodResponseAsString(method);
            OWSExceptionParser.checkForExceptionResponse(responseString);

            return new WFSResponse(responseString, method);
        } catch (final Exception ex) {
            throw new PortalServiceException(method, ex);
        }
    }

    public WFSGetCapabilitiesResponse getCapabilitiesResponse(final String wfsUrl) throws PortalServiceException {
        HttpRequestBase method = null;

        try {
            //Make WFS request
            method = wfsMethodMaker.makeGetCapabilitiesMethod(wfsUrl);
            final String responseString = httpServiceCaller.getMethodResponseAsString(method);

            //Parse resulting XML
            final Document responseDoc = DOMUtil.buildDomFromString(responseString);
            OWSExceptionParser.checkForExceptionResponse(responseDoc);

            final WFSGetCapabilitiesResponse parsedGetCap = new WFSGetCapabilitiesResponse();

            //Get the output formats
            final XPathExpression xPathGetOf = DOMUtil
                    .compileXPathExpr(
                            "wfs:WFS_Capabilities/ows:OperationsMetadata/ows:Operation[@name=\"GetFeature\"]/ows:Parameter[@name=\"outputFormat\"]/ows:Value",
                            new WFSNamespaceContext());
            final NodeList formatNodes = (NodeList) xPathGetOf.evaluate(responseDoc, XPathConstants.NODESET);
            final String[] outputFormats = new String[formatNodes.getLength()];
            for (int i = 0; i < formatNodes.getLength(); i++) {
                outputFormats[i] = formatNodes.item(i).getTextContent();
            }
            parsedGetCap.setGetFeatureOutputFormats(outputFormats);

            //Get feature type names and abstracts
            final XPathExpression xPathGetTn = DOMUtil.compileXPathExpr(
                    "wfs:WFS_Capabilities/wfs:FeatureTypeList/wfs:FeatureType/wfs:Name", new WFSNamespaceContext());
            final NodeList nameNodes = (NodeList) xPathGetTn.evaluate(responseDoc, XPathConstants.NODESET);
            final String[] typeNames = new String[nameNodes.getLength()];
            final XPathExpression xPathGetAbstract = DOMUtil.compileXPathExpr(
                    "wfs:WFS_Capabilities/wfs:FeatureTypeList/wfs:FeatureType/wfs:Abstract", new WFSNamespaceContext());
            final NodeList abstractNodes = (NodeList) xPathGetAbstract.evaluate(responseDoc, XPathConstants.NODESET);
            final Map<String, String> featureAbstracts = new HashMap<>();
            final XPathExpression xPathGetMetadataURL = DOMUtil.compileXPathExpr(
                    "wfs:WFS_Capabilities/wfs:FeatureTypeList/wfs:FeatureType/wfs:MetadataURL", new WFSNamespaceContext());
            final NodeList metadataURLNodes = (NodeList) xPathGetMetadataURL.evaluate(responseDoc, XPathConstants.NODESET);
            final Map<String, String> metadataURLs = new HashMap<>();

            for (int i = 0; i < nameNodes.getLength(); i++) {
                final String typeName = nameNodes.item(i).getTextContent();
                typeNames[i] = typeName;
                if (abstractNodes.getLength() > i && abstractNodes.item(i) != null) {
                    featureAbstracts.put(typeName, abstractNodes.item(i).getTextContent());
                }
                if (metadataURLNodes.getLength() > i && metadataURLNodes.item(i) != null) {
                    metadataURLs.put(typeName, metadataURLNodes.item(i).getTextContent());
                }
            }
            parsedGetCap.setFeatureTypes(typeNames);
            parsedGetCap.setFeatureAbstracts(featureAbstracts);
            parsedGetCap.setMetadataURLs(metadataURLs);

            return parsedGetCap;
        } catch (final Exception e) {
            throw new PortalServiceException(method, e);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
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
    public InputStream downloadWFS(final String serviceUrl, final String type, final String filterString, final Integer maxFeatures)
            throws PortalServiceException {

        HttpRequestBase method = null;
        try {

            method = generateWFSRequest(serviceUrl, type, null, filterString, maxFeatures, null, ResultType.Results);
            return httpServiceCaller.getMethodResponseAsStream(method);

        } catch (final Exception ex) {
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
    public InputStream downloadCSV(final String serviceUrl, final String type, final String filterString, final Integer maxFeatures)
            throws PortalServiceException {

        HttpRequestBase method = null;
        try {

            method = generateWFSRequest(serviceUrl, type, null, filterString, maxFeatures, null, ResultType.Results,
                    "csv");
            return httpServiceCaller.getMethodResponseAsStream(method);

        } catch (final Exception ex) {
            throw new PortalServiceException(method, "Error when attempting to download from:" + serviceUrl, ex);
        }
    }
}

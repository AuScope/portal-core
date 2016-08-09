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
     * Makes a WFS GetFeature request represented by method, only the count of features will be returned
     *
     * @param method
     * @return
     * @throws PortalServiceException
     */
    protected WFSCountResponse getWfsFeatureCount(HttpRequestBase method) throws PortalServiceException {
        try (InputStream responseStream = httpServiceCaller.getMethodResponseAsStream(method)) {
            Document responseDoc = DOMUtil.buildDomFromStream(responseStream);
            OWSExceptionParser.checkForExceptionResponse(responseDoc);

            XPathExpression xPath = DOMUtil.compileXPathExpr("wfs:FeatureCollection/@numberOfFeatures",
                    new WFSNamespaceContext());
            Node numNode = (Node) xPath.evaluate(responseDoc, XPathConstants.NODE);
            int numNodeValue = Integer.parseInt(numNode.getTextContent());

            return new WFSCountResponse(numNodeValue);
        } catch (Exception ex) {
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

    public WFSGetCapabilitiesResponse getCapabilitiesResponse(String wfsUrl) throws PortalServiceException {
        HttpRequestBase method = null;

        try {
            //Make WFS request
            method = wfsMethodMaker.makeGetCapabilitiesMethod(wfsUrl);
            String responseString = httpServiceCaller.getMethodResponseAsString(method);

            //Parse resulting XML
            Document responseDoc = DOMUtil.buildDomFromString(responseString);
            OWSExceptionParser.checkForExceptionResponse(responseDoc);

            WFSGetCapabilitiesResponse parsedGetCap = new WFSGetCapabilitiesResponse();

            //Get the output formats
            XPathExpression xPathGetOf = DOMUtil
                    .compileXPathExpr(
                            "wfs:WFS_Capabilities/ows:OperationsMetadata/ows:Operation[@name=\"GetFeature\"]/ows:Parameter[@name=\"outputFormat\"]/ows:Value",
                            new WFSNamespaceContext());
            NodeList formatNodes = (NodeList) xPathGetOf.evaluate(responseDoc, XPathConstants.NODESET);
            String[] outputFormats = new String[formatNodes.getLength()];
            for (int i = 0; i < formatNodes.getLength(); i++) {
                outputFormats[i] = formatNodes.item(i).getTextContent();
            }
            parsedGetCap.setGetFeatureOutputFormats(outputFormats);

            //Get feature type names and abstracts
            XPathExpression xPathGetTn = DOMUtil.compileXPathExpr(
                    "wfs:WFS_Capabilities/wfs:FeatureTypeList/wfs:FeatureType/wfs:Name", new WFSNamespaceContext());
            NodeList nameNodes = (NodeList) xPathGetTn.evaluate(responseDoc, XPathConstants.NODESET);
            String[] typeNames = new String[nameNodes.getLength()];
            XPathExpression xPathGetAbstract = DOMUtil.compileXPathExpr(
                    "wfs:WFS_Capabilities/wfs:FeatureTypeList/wfs:FeatureType/wfs:Abstract", new WFSNamespaceContext());
            NodeList abstractNodes = (NodeList) xPathGetAbstract.evaluate(responseDoc, XPathConstants.NODESET);
            Map<String, String> featureAbstracts = new HashMap<>();
            XPathExpression xPathGetMetadataURL = DOMUtil.compileXPathExpr(
                    "wfs:WFS_Capabilities/wfs:FeatureTypeList/wfs:FeatureType/wfs:MetadataURL", new WFSNamespaceContext());
            NodeList metadataURLNodes = (NodeList) xPathGetMetadataURL.evaluate(responseDoc, XPathConstants.NODESET);
            Map<String, String> metadataURLs = new HashMap<>();

            for (int i = 0; i < nameNodes.getLength(); i++) {
                String typeName = nameNodes.item(i).getTextContent();
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
        } catch (Exception e) {
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
}

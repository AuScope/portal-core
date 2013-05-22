package org.auscope.portal.core.services;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker.ResultType;
import org.auscope.portal.core.services.namespaces.WFSNamespaceContext;
import org.auscope.portal.core.services.responses.ows.OWSExceptionParser;
import org.auscope.portal.core.services.responses.wfs.WFSCountResponse;
import org.auscope.portal.core.services.responses.wfs.WFSTransformedResponse;
import org.auscope.portal.core.util.DOMUtil;
import org.auscope.portal.core.xslt.PortalXSLTTransformer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * An abstract base class containing common functionality for all Service classes
 * that intend to interact with a one or more Web Feature Services.
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
     * @param httpServiceCaller Will be used for making requests
     * @param wfsMethodMaker Will be used for generating WFS methods
     * @param gmlToKml Will be used for transforming GML (WFS responses) into KML
     * @param gmlToHtml Will be used for transforming GML (WFS responses) into HTML
     */
    public BaseWFSService(HttpServiceCaller httpServiceCaller,
            WFSGetFeatureMethodMaker wfsMethodMaker) {
        this.httpServiceCaller = httpServiceCaller;
        this.wfsMethodMaker = wfsMethodMaker;
    }

    /**
     * Utility method for choosing the correct WFS method to generate based on specified parameters
     * @param wfsUrl [required] - the web feature service url
     * @param featureType [required] - the type name
     * @param featureId [optional] - A unique ID of a single feature type to query
     * @param filterString [optional] - A OGC filter string to constrain the request
     * @param maxFeatures [optional] - A maximum number of features to request
     * @param srs [optional] - The spatial reference system the response should be encoded to. If unspecified BaseWFSService.DEFAULT_SRS will be used
     * @param resultType [optional] - Whether to request all features (default) or just the count
     * @return
     * @throws URISyntaxException
     * @throws Exception
     */
    protected HttpRequestBase generateWFSRequest(String wfsUrl, String featureType, String featureId, String filterString, Integer maxFeatures, String srs, ResultType resultType) throws URISyntaxException {
        return generateWFSRequest(wfsUrl, featureType, featureId, filterString, maxFeatures, srs, resultType, null);
    }

    /**
     * Utility method for choosing the correct WFS method to generate based on specified parameters
     * @param wfsUrl [required] - the web feature service url
     * @param featureType [required] - the type name
     * @param featureId [optional] - A unique ID of a single feature type to query
     * @param filterString [optional] - A OGC filter string to constrain the request
     * @param maxFeatures [optional] - A maximum number of features to request
     * @param srs [optional] - The spatial reference system the response should be encoded to. If unspecified BaseWFSService.DEFAULT_SRS will be used
     * @param resultType [optional] - Whether to request all features (default) or just the count
     * @param outputFormat [optional] - The format the response should take
     * @return
     * @throws URISyntaxException
     * @throws Exception
     */
    protected HttpRequestBase generateWFSRequest(String wfsUrl, String featureType, String featureId, String filterString, Integer maxFeatures, String srs, ResultType resultType, String outputFormat) throws URISyntaxException {
        int max = maxFeatures == null ? 0 : maxFeatures.intValue();

        //apply default value for srs
        if (srs == null || srs.isEmpty()) {
            srs = DEFAULT_SRS;
        }

        if (featureId == null) {
            return wfsMethodMaker.makePostMethod(wfsUrl, featureType, filterString, max, srs, resultType, outputFormat);
        } else {
            return wfsMethodMaker.makeGetMethod(wfsUrl, featureType, featureId, srs, outputFormat);
        }
    }

    /**
     * Makes a WFS GetFeature request represented by method, only the count
     * of features will be returned
     *
     * @param method
     * @return
     * @throws PortalServiceException
     */
    protected WFSCountResponse getWfsFeatureCount(HttpRequestBase method) throws PortalServiceException {
        try {
            //Make the request and parse the response
            InputStream responseStream = httpServiceCaller.getMethodResponseAsStream(method);
            Document responseDoc = DOMUtil.buildDomFromStream(responseStream);
            OWSExceptionParser.checkForExceptionResponse(responseDoc);

            XPathExpression xPath = DOMUtil.compileXPathExpr("wfs:FeatureCollection/@numberOfFeatures", new WFSNamespaceContext());
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
     * Executes a method that returns GML wrapped in a WFS response, converts that
     * response using transformer and returns the lot bundled in a WFSTransformedResponse
     * @param method a WFS GetFeature request
     * @param transformer A transformer to work with the resulting WFS response
     * @param styleSheetParams Properties to apply to the transformer
     * @return
     * @throws PortalServiceException
     */
    protected WFSTransformedResponse getTransformedWFSResponse(HttpRequestBase method, PortalXSLTTransformer transformer, Properties styleSheetParams) throws PortalServiceException {
        try {
            //Make the request and parse the response
            String responseString = httpServiceCaller.getMethodResponseAsString(method);
            OWSExceptionParser.checkForExceptionResponse(responseString);

            String transformed = transformer.convert(responseString, styleSheetParams);

            return new WFSTransformedResponse(responseString, transformed, method);
        } catch (Exception ex) {
            throw new PortalServiceException(method, ex);
        }
    }
}

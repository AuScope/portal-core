package org.auscope.portal.server.web.service;

import java.io.InputStream;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.server.domain.ows.OWSException;
import org.auscope.portal.server.domain.ows.OWSExceptionParser;
import org.auscope.portal.server.domain.wfs.WFSCountResponse;
import org.auscope.portal.server.domain.wfs.WFSHTMLResponse;
import org.auscope.portal.server.domain.wfs.WFSKMLResponse;
import org.auscope.portal.server.domain.wfs.WFSNamespaceContext;
import org.auscope.portal.server.util.DOMUtil;
import org.auscope.portal.server.util.GmlToHtml;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker.ResultType;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * An abstract base class containing common functionality for all Service classes
 * that intend to interact with a one or more Web Feature Services.
 *
 * @author Josh Vote
 */
public abstract class BaseWFSService {
    protected HttpServiceCaller httpServiceCaller;
    protected WFSGetFeatureMethodMaker wfsMethodMaker;
    protected GmlToKml gmlToKml;
    protected GmlToHtml gmlToHtml;

    /**
     * Creates a new instance of this class with the specified dependencies
     * @param httpServiceCaller Will be used for making requests
     * @param wfsMethodMaker Will be used for generating WFS methods
     * @param gmlToKml Will be used for transforming GML (WFS responses) into KML
     * @param gmlToHtml Will be used for transforming GML (WFS responses) into HTML
     */
    @Autowired
    public BaseWFSService(HttpServiceCaller httpServiceCaller,
            WFSGetFeatureMethodMaker wfsMethodMaker,
            GmlToKml gmlToKml, GmlToHtml gmlToHtml) {
        this.httpServiceCaller = httpServiceCaller;
        this.wfsMethodMaker = wfsMethodMaker;
        this.gmlToKml = gmlToKml;
        this.gmlToHtml = gmlToHtml;
    }

    /**
     * Utility method for choosing the correct WFS method to generate based on specified parameters
     * @param wfsUrl [required] - the web feature service url
     * @param featureType [required] - the type name
     * @param featureId [optional] - A unique ID of a single feature type to query
     * @param filterString [optional] - A OGC filter string to constrain the request
     * @param maxFeatures [optional] - A maximum number of features to request
     * @param srs [optional] - The spatial reference system the response should be encoded to
     * @param resultType [optional] - Whether to request all features (default) or just the count
     * @return
     * @throws Exception
     */
    protected HttpMethodBase generateWFSRequest(String wfsUrl, String featureType, String featureId, String filterString, Integer maxFeatures, String srs, ResultType resultType) {
        int max = maxFeatures == null ? 0 : maxFeatures.intValue();

        if (featureId == null) {
            return wfsMethodMaker.makeMethod(wfsUrl, featureType, filterString, max, srs, resultType);
        } else {
            return wfsMethodMaker.makeMethod(wfsUrl, featureType, featureId);
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
    protected WFSCountResponse getWfsFeatureCount(HttpMethodBase method) throws PortalServiceException {
        try {
            //Make the request and parse the response
            InputStream responseStream = httpServiceCaller.getMethodResponseAsStream(method, httpServiceCaller.getHttpClient());
            Document responseDoc = DOMUtil.buildDomFromStream(responseStream);
            checkForOWSException(responseDoc);

            XPathExpression xPath = DOMUtil.compileXPathExpr("wfs:FeatureCollection/@numberOfFeatures", new WFSNamespaceContext());
            Node numNode = (Node) xPath.evaluate(responseDoc, XPathConstants.NODE);
            int numNodeValue = Integer.parseInt(numNode.getTextContent());

            return new WFSCountResponse(numNodeValue);
        } catch (Exception ex) {
            throw new PortalServiceException(method, ex);
        }
    }

    /**
     * Makes a WFS GetFeature request represented by method
     *
     * The response is returned as a String in both GML and KML forms.
     * @param wfsUrl The web feature service url (passed as parameter to style sheet)
     * @param method The method for making the WFS request
     * @return
     * @throws Exception
     */
    protected WFSKMLResponse getWfsResponseAsKml(String wfsUrl, HttpMethodBase method) throws PortalServiceException {
        try {
            //Make the request and transform the response
            String responseGml = httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
            checkForOWSException(responseGml);
            String responseKml = gmlToKml.convert(responseGml, wfsUrl);

            return new WFSKMLResponse(responseGml, responseKml, method);
        } catch (Exception ex) {
            throw new PortalServiceException(method, ex);
        }
    }

    /**
     * Makes a WFS GetFeature request represented by method
     *
     * The response is returned as a String in both GML and HTML forms.
     * @param wfsUrl The web feature service url (passed as parameter to style sheet)
     * @param method The method for making the WFS request
     * @return
     * @throws Exception
     */
    protected WFSHTMLResponse getWfsResponseAsHtml(String wfsUrl, HttpMethodBase method) throws PortalServiceException {
        try {
            //Make the request and transform the response
            String responseGml = httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
            checkForOWSException(responseGml);
            String responseHtml = gmlToHtml.convert(responseGml, wfsUrl);

            return new WFSHTMLResponse(responseGml, responseHtml, method);
        } catch (Exception ex) {
            throw new PortalServiceException(method, ex);
        }
    }

    /**
     * Checks the specified xmlString for any OWS exceptions. An OWSException is thrown if xmlString contains said exception.
     *
     * @param xmlString the string to check, must contain valid XML
     * @throws OWSException
     */
    protected void checkForOWSException(String xmlString) throws OWSException {
        OWSExceptionParser.checkForExceptionResponse(xmlString);
    }

    /**
     * Checks the specified xml document for any OWS exceptions. An OWSException is thrown if the doc contains said exception.
     *
     * @param doc the document to check
     * @throws OWSException
     */
    protected void checkForOWSException(Document doc) throws OWSException {
        OWSExceptionParser.checkForExceptionResponse(doc);
    }
}

package org.auscope.portal.server.web.service;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.domain.wfs.WFSHTMLResponse;
import org.auscope.portal.server.domain.wfs.WFSKMLResponse;
import org.auscope.portal.server.util.GmlToHtml;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service class encapsulating high level access to a remote Web Feature Service
 * @author Josh Vote
 *
 */
@Service
public class WFSService {
    private final Log log = LogFactory.getLog(getClass());

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
    public WFSService(HttpServiceCaller httpServiceCaller,
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
    protected HttpMethodBase generateWFSRequest(String wfsUrl, String featureType, String featureId, String filterString, Integer maxFeatures, String srs) {
        int max = maxFeatures == null ? 0 : maxFeatures.intValue();

        if (featureId == null) {
            return wfsMethodMaker.makeMethod(wfsUrl, featureType, filterString, max, srs);
        } else {
            return wfsMethodMaker.makeMethod(wfsUrl, featureType, featureId);
        }
    }

    /**
     * Makes a WFS GetFeature request constrained by the specified parameters
     *
     * The response is returned as a String in both GML and KML forms.
     * @param wfsUrl the web feature service url
     * @param featureType the type name
     * @param featureId A unique ID of a single feature type to query
     * @return
     * @throws Exception
     */
    public WFSKMLResponse getWfsResponseAsKml(String wfsUrl, String featureType, String featureId) throws PortalServiceException {
        HttpMethodBase method = generateWFSRequest(wfsUrl, featureType, featureId, null, null, null);
        return getWfsResponseAsKml(wfsUrl, method);
    }

    /**
     * Makes a WFS GetFeature request constrained by the specified parameters
     *
     * The response is returned as a String in both GML and KML forms.
     * @param wfsUrl the web feature service url
     * @param featureType the type name
     * @param filterString A OGC filter string to constrain the request
     * @param maxFeatures  A maximum number of features to request
     * @param srs The spatial reference system the response should be encoded to
     * @param resultType Whether to request all features (default) or just the count
     * @return
     * @throws Exception
     */
    public WFSKMLResponse getWfsResponseAsKml(String wfsUrl, String featureType, String filterString, Integer maxFeatures, String srs) throws PortalServiceException {
        HttpMethodBase method = generateWFSRequest(wfsUrl, featureType, null, filterString, maxFeatures, srs);
        return getWfsResponseAsKml(wfsUrl, method);
    }

    /**
     * Makes a WFS GetFeature request constrained by the specified parameters
     *
     * The response is returned as a String in both GML and HTML forms.
     * @param wfsUrl the web feature service url
     * @param featureType the type name
     * @param featureId A unique ID of a single feature type to query
     * @return
     * @throws Exception
     */
    public WFSHTMLResponse getWfsResponseAsHtml(String wfsUrl, String featureType, String featureId) throws PortalServiceException {
        HttpMethodBase method = generateWFSRequest(wfsUrl, featureType, featureId, null, null, null);
        return getWfsResponseAsHtml(wfsUrl, method);
    }

    /**
     * Makes a HTTP Get request to the specified URL.
     *
     * The response is returned as a String in both GML and HTML forms.
     * @param wfsUrl the web feature service url
     * @param featureType the type name
     * @param featureId A unique ID of a single feature type to query
     * @return
     * @throws Exception
     */
    public WFSHTMLResponse getWfsResponseAsHtml(String wfsUrl) throws PortalServiceException {
        HttpMethodBase method = new GetMethod(wfsUrl);
        return getWfsResponseAsHtml(wfsUrl, method);
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
            String responseHtml = gmlToHtml.convert(responseGml, wfsUrl);

            return new WFSHTMLResponse(responseGml, responseHtml, method);
        } catch (Exception ex) {
            throw new PortalServiceException(method, ex);
        }
    }

}

package org.auscope.portal.server.web.service;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.auscope.portal.server.domain.wfs.WFSCountResponse;
import org.auscope.portal.server.domain.wfs.WFSHTMLResponse;
import org.auscope.portal.server.domain.wfs.WFSKMLResponse;
import org.auscope.portal.server.util.GmlToHtml;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker.ResultType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service class encapsulating high level access to a remote Web Feature Service
 * @author Josh Vote
 *
 */
@Service
public class WFSService extends BaseWFSService {

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
        super(httpServiceCaller, wfsMethodMaker, gmlToKml, gmlToHtml);
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
        HttpMethodBase method = generateWFSRequest(wfsUrl, featureType, featureId, null, null, null, null);
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
     * @param srs [Optional] The spatial reference system the response should be encoded to @param srsName - will use BaseWFSService.DEFAULT_SRS if unspecified
     * @return
     * @throws Exception
     */
    public WFSKMLResponse getWfsResponseAsKml(String wfsUrl, String featureType, String filterString, Integer maxFeatures, String srs) throws PortalServiceException {
        HttpMethodBase method = generateWFSRequest(wfsUrl, featureType, null, filterString, maxFeatures, srs, ResultType.Results);
        return getWfsResponseAsKml(wfsUrl, method);
    }

    /**
     * Makes a WFS GetFeature request constrained by the specified parameters. Instead
     * of returning the full response only the count of features will be returned.
     *
     * @param wfsUrl the web feature service url
     * @param featureType the type name
     * @param filterString A OGC filter string to constrain the request
     * @param maxFeatures  A maximum number of features to request
     * @param srsName [Optional] the SRS to make the WFS request using - will use BaseWFSService.DEFAULT_SRS if unspecified
     * @return
     * @throws PortalServiceException
     */
    public WFSCountResponse getWfsFeatureCount(String wfsUrl, String featureType, String filterString, Integer maxFeatures, String srsName) throws PortalServiceException {
        HttpMethodBase method = generateWFSRequest(wfsUrl, featureType, null, filterString, maxFeatures, srsName, ResultType.Hits);
        return getWfsFeatureCount(method);
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
        HttpMethodBase method = generateWFSRequest(wfsUrl, featureType, featureId, null, null, null, null);
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
}

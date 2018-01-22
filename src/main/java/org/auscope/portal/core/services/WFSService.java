package org.auscope.portal.core.services;

import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.BaseWFSService;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker.ResultType;
import org.auscope.portal.core.services.responses.ows.OWSExceptionParser;
import org.auscope.portal.core.services.responses.wfs.WFSCountResponse;
import org.auscope.portal.core.services.responses.wfs.WFSResponse;
import org.auscope.portal.core.services.responses.wfs.WFSTransformedResponse;
import org.auscope.portal.core.xslt.GmlToHtml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service class encapsulating high level access to a remote Web Feature Service
 *
 * @author Josh Vote
 *
 */
@Service
public class WFSService extends BaseWFSService {

    private GmlToHtml gmlToHtml;

    /**
     * Creates a new instance of this class with the specified dependencies
     *
     * @param httpServiceCaller
     *            Will be used for making requests
     * @param wfsMethodMaker
     *            Will be used for generating WFS methods
     * @param gmlToKml
     *            Will be used for transforming GML (WFS responses) into KML
     * @param gmlToHtml
     *            Will be used for transforming GML (WFS responses) into HTML
     */
    @Autowired
    public WFSService(HttpServiceCaller httpServiceCaller,
            WFSGetFeatureMethodMaker wfsMethodMaker,
            GmlToHtml gmlToHtml) {
        super(httpServiceCaller, wfsMethodMaker);
        this.gmlToHtml = gmlToHtml;
    }

    private WFSResponse doRequest(HttpRequestBase method, String serviceUrl)
            throws PortalServiceException {
        try {
            String wfs = httpServiceCaller.getMethodResponseAsString(method);
            OWSExceptionParser.checkForExceptionResponse(wfs);

            return new WFSResponse(wfs, method);
        } catch (Exception ex) {
            throw new PortalServiceException(method, ex);
        }
    }

    private WFSTransformedResponse doRequestAndHtmlTransform(HttpRequestBase method, String serviceUrl)
            throws PortalServiceException {
        try {
            String wfs = httpServiceCaller.getMethodResponseAsString(method);
            OWSExceptionParser.checkForExceptionResponse(wfs);
            String kml = gmlToHtml.convert(wfs, serviceUrl);

            return new WFSTransformedResponse(wfs, kml, method);
        } catch (Exception ex) {
            throw new PortalServiceException(method, ex);
        }
    }

    /**
     * Makes a WFS GetFeature request constrained by the specified parameters
     *
     * The response is returned as a String
     *
     * @param wfsUrl
     *            the web feature service url
     * @param featureType
     *            the type name
     * @param featureId
     *            A unique ID of a single feature type to query
     * @return
     * @throws URISyntaxException
     * @throws Exception
     */
    public WFSResponse getWfsResponse(String wfsUrl, String featureType, String featureId)
            throws PortalServiceException, URISyntaxException {
        HttpRequestBase method = generateWFSRequest(wfsUrl, featureType, featureId, null, null, null, null);
        return doRequest(method, wfsUrl);
    }

    /**
     * Makes a WFS GetFeature request constrained by the specified parameters
     *
     * The response is returned as a String
     *
     * @param wfsUrl
     *            the web feature service url
     * @param featureType
     *            the type name
     * @param filterString
     *            A OGC filter string to constrain the request
     * @param maxFeatures
     *            A maximum number of features to request
     * @param srs
     *            [Optional] The spatial reference system the response should be encoded to @param srsName - will use BaseWFSService.DEFAULT_SRS if unspecified
     * @return
     * @throws URISyntaxException
     * @throws Exception
     */
    public WFSResponse getWfsResponse(String wfsUrl, String featureType, String filterString,
            Integer maxFeatures, String srs) throws PortalServiceException, URISyntaxException {
        HttpRequestBase method = generateWFSRequest(wfsUrl, featureType, null, filterString, maxFeatures, srs,
                ResultType.Results);
        
        return doRequest(method, wfsUrl);
    }
    
    public WFSResponse getGml32WfsResponse(String wfsUrl, String featureType, String filterString,
            Integer maxFeature, String srs) throws PortalServiceException, URISyntaxException {
        HttpRequestBase method = generateWFSRequest(wfsUrl, featureType, null, filterString, maxFeature, srs,
                ResultType.Results, "gml32", null);
        
        return doRequest(method, wfsUrl);
    }

    /**
     * Makes a WFS GetFeature request constrained by the specified parameters. Instead of returning the full response only the count of features will be
     * returned.
     *
     * @param wfsUrl
     *            the web feature service url
     * @param featureType
     *            the type name
     * @param filterString
     *            A OGC filter string to constrain the request
     * @param maxFeatures
     *            A maximum number of features to request
     * @param srsName
     *            [Optional] the SRS to make the WFS request using - will use BaseWFSService.DEFAULT_SRS if unspecified
     * @return
     * @throws PortalServiceException
     * @throws URISyntaxException
     */
    public WFSCountResponse getWfsFeatureCount(String wfsUrl, String featureType, String filterString,
            Integer maxFeatures, String srsName) throws PortalServiceException, URISyntaxException {
        HttpRequestBase method = generateWFSRequest(wfsUrl, featureType, null, filterString, maxFeatures, srsName,
                ResultType.Hits);
        return getWfsFeatureCount(method);
    }

    /**
     * Makes a WFS GetFeature request constrained by the specified parameters
     *
     * The response is returned as a String in both GML and HTML forms.
     *
     * @param wfsUrl
     *            the web feature service url
     * @param featureType
     *            the type name
     * @param featureId
     *            A unique ID of a single feature type to query
     * @return
     * @throws URISyntaxException
     * @throws Exception
     */
    public WFSTransformedResponse getWfsResponseAsHtml(String wfsUrl, String featureType, String featureId)
            throws PortalServiceException, URISyntaxException {
        HttpRequestBase method = generateWFSRequest(wfsUrl, featureType, featureId, null, null, null, null);
        return doRequestAndHtmlTransform(method, wfsUrl);
    }

    /**
     * Makes a HTTP Get request to the specified URL.
     *
     * The response is returned as a String in both GML and HTML forms.
     *
     * @param wfsUrl
     *            the web feature service url
     * @param featureType
     *            the type name
     * @param featureId
     *            A unique ID of a single feature type to query
     * @return
     * @throws Exception
     */
    public WFSTransformedResponse getWfsResponseAsHtml(String wfsUrl) throws PortalServiceException {
        HttpRequestBase method = new HttpGet(wfsUrl);
        return doRequestAndHtmlTransform(method, wfsUrl);
    }
}

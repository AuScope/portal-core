package org.auscope.portal.core.services;

import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker.ResultType;
import org.auscope.portal.core.services.responses.wfs.WFSResponse;
import org.auscope.portal.core.xslt.GmlToHtml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service class encapsulating high level access to a remote Web Feature Service
 * that supports GML 3.2.
 *
 * @author Josh Vote
 * @author Rini Angreani
 *
 */
@Service
public class WFSGml32Service extends WFSService {

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
    public WFSGml32Service(HttpServiceCaller httpServiceCaller,
            WFSGetFeatureMethodMaker wfsMethodMaker,
            GmlToHtml gmlToHtml) {
        super(httpServiceCaller, wfsMethodMaker, gmlToHtml);
    }

    /**
     * Makes a WFS GetFeature request with gml32 outputFormat constrained by the specified parameters
     *
     * The response is returned as a String
     *
     * @see WFSService#getWfsResponse(String, String, String, Integer, String)
     */
    @Override
	public WFSResponse getWfsResponse(String wfsUrl, String featureType, String filterString,
            Integer maxFeature, String srs) throws PortalServiceException, URISyntaxException {
        HttpRequestBase method = generateWFSRequest(wfsUrl, featureType, null, filterString, maxFeature, srs,
                ResultType.Results, "gml32", null);

        return super.doRequest(method);
    }
}

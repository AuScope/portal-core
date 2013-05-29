package org.auscope.portal.core.services;

import java.io.InputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.WMSMethodMaker;
import org.auscope.portal.core.services.responses.ows.OWSExceptionParser;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesRecord;

/**
 * Service class providing functionality for interacting with a Web Map Service
 */
public class WMSService {

    // -------------------------------------------------------------- Constants
    private final Log log = LogFactory.getLog(getClass());


    // ----------------------------------------------------- Instance variables
    private HttpServiceCaller serviceCaller;
    private WMSMethodMaker methodMaker;


    // ----------------------------------------------------------- Constructors
    public WMSService(HttpServiceCaller serviceCaller, WMSMethodMaker methodMaker) {
        this.serviceCaller = serviceCaller;
        this.methodMaker = methodMaker;
    }


    // ------------------------------------------- Property Setters and Getters
    /**
     * Request GetCapabilities document from the given service
     *
     * @param serviceUrl Url of WMS service
     * @return GetCapabilitiesRecord
     */
    public GetCapabilitiesRecord getWmsCapabilities(final String serviceUrl) throws PortalServiceException {
        HttpRequestBase method = null;
        try {
            // Do the request
            method = methodMaker.getCapabilitiesMethod(serviceUrl);
            InputStream response = serviceCaller.getMethodResponseAsStream(method);

            return new GetCapabilitiesRecord(response);
        } catch (Exception ex) {
            throw new PortalServiceException(method, "Failure getting/parsing wms capabilities", ex);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }

    /**
     * Makes a WMS GetFeatureInfo request using the specified parameters. Returns the response
     * as a string
     *
     * @param wmsUrl The WMS endpoint (will have any existing query parameters preserved)
     * @param format The desired mime type of the response
     * @param layer The name of the layer to download
     * @param srs The spatial reference system for the bounding box
     * @param westBoundLongitude The west bound longitude of the bounding box
     * @param southBoundLatitude The south bound latitude of the bounding box
     * @param eastBoundLongitude The east bound longitude of the bounding box
     * @param northBoundLatitude The north bound latitude of the bounding box
     * @param width The desired output image width in pixels
     * @param height The desired output image height in pixels
     * @param styles [Optional] What style should be included
     * @param pointLng Where the user clicked (longitude)
     * @param pointLat Where the user clicked (latitude)
     * @param pointX Where the user clicked in pixel coordinates relative to the GetMap that was used (X direction)
     * @param pointY Where the user clicked in pixel coordinates relative to the GetMap that was used (Y direction)
     * @return
     * @throws PortalServiceException
     */
    public String getFeatureInfo(String wmsUrl, String format, String layer, String srs, double westBoundLongitude, double southBoundLatitude, double eastBoundLongitude, double northBoundLatitude, int width, int height, double pointLng, double pointLat, int pointX, int pointY, String styles,String sld) throws PortalServiceException {
        // Do the request
        HttpRequestBase method = null;
        try {
            method = methodMaker.getFeatureInfo(wmsUrl, format, layer, srs, westBoundLongitude, southBoundLatitude, eastBoundLongitude, northBoundLatitude, width, height, pointLng, pointLat, pointX, pointY, styles,sld);
            String response =  serviceCaller.getMethodResponseAsString(method);

            OWSExceptionParser.checkForExceptionResponse(response);

            return response;
        } catch (Exception ex) {
            throw new PortalServiceException(method, "Failure getting/parsing wms capabilities", ex);
        }
    }
}

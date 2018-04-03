package org.auscope.portal.core.services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.naming.OperationNotSupportedException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpClientInputStream;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.WMSMethodMakerInterface;
import org.auscope.portal.core.services.responses.ows.OWSExceptionParser;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesRecord;

/**
 * Service class providing functionality for interacting with a Web Map Service
 */
public class WMSService {

    // -------------------------------------------------------------- Constants
    private final Log log = LogFactory.getLog(getClass());

    // ----------------------------------------------------- Instance variables
    protected HttpServiceCaller serviceCaller;

    protected List<WMSMethodMakerInterface> listOfSupportedWMSMethodMaker;

    // ----------------------------------------------------------- Constructors
    public WMSService(HttpServiceCaller serviceCaller, List<WMSMethodMakerInterface> methodMaker) {
        this.serviceCaller = serviceCaller;
        this.listOfSupportedWMSMethodMaker = methodMaker;

    }

    // ------------------------------------------- Property Setters and Getters

    protected WMSMethodMakerInterface getSupportedMethodMaker(String wmsUrl, String version)
            throws OperationNotSupportedException {
        log.trace("WMSService::getsupportedMethodMaker() START");
        StringBuilder errStr = new StringBuilder();
        for (WMSMethodMakerInterface maker : listOfSupportedWMSMethodMaker) {
            if (maker.accepts(wmsUrl, version, errStr)) {
                log.trace("WMSService::getsupportedMethodMaker() END");
                return maker;
            }
        }
        log.debug("WMSService::getsupportedMethodMaker() throwing exception: "+errStr.toString());
        log.trace("WMSService::getsupportedMethodMaker() END");
        throw new OperationNotSupportedException(errStr.toString());
    }

    /**
     * Request GetCapabilities document from the given service
     *
     * @param serviceUrl
     *            Url of WMS service
     * @return GetCapabilitiesRecord
     */
    public GetCapabilitiesRecord getWmsCapabilities(final String serviceUrl, String version)
            throws PortalServiceException {
        HttpRequestBase method = null;
        try {

            WMSMethodMakerInterface methodMaker = getSupportedMethodMaker(serviceUrl, version);
            // Do the request
            method = methodMaker.getCapabilitiesMethod(serviceUrl);
            return methodMaker.getGetCapabilitiesRecord(method);

        } catch (NullPointerException npe) {
            npe.printStackTrace();
            throw new NullPointerException("Call configWMSVersion to setup the right wms method maker to use");
        } catch (Exception ex) {
            throw new PortalServiceException(method, ex.getMessage(), ex);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }

    /**
     * Makes a WMS GetFeatureInfo request using the specified parameters. Returns the response as a string
     *
     * @param wmsUrl
     *            The WMS endpoint (will have any existing query parameters preserved)
     * @param format
     *            The desired mime type of the response
     * @param layer
     *            The name of the layer to download
     * @param srs
     *            The spatial reference system for the bounding box
     * @param westBoundLongitude
     *            The west bound longitude of the bounding box
     * @param southBoundLatitude
     *            The south bound latitude of the bounding box
     * @param eastBoundLongitude
     *            The east bound longitude of the bounding box
     * @param northBoundLatitude
     *            The north bound latitude of the bounding box
     * @param width
     *            The desired output image width in pixels
     * @param height
     *            The desired output image height in pixels
     * @param styles
     *            [Optional] What style should be included
     * @param pointLng
     *            Where the user clicked (longitude)
     * @param pointLat
     *            Where the user clicked (latitude)
     * @param pointX
     *            Where the user clicked in pixel coordinates relative to the GetMap that was used (X direction)
     * @param pointY
     *            Where the user clicked in pixel coordinates relative to the GetMap that was used (Y direction)
     * @return
     * @throws PortalServiceException
     */
    public String getFeatureInfo(String wmsUrl, String format, String layer, String srs, double westBoundLongitude,
            double southBoundLatitude, double eastBoundLongitude, double northBoundLatitude, int width, int height,
            double pointLng, double pointLat, int pointX, int pointY, String styles, String sldBody,
            boolean postMethod,
            String version, String feature_count, boolean attemptOtherVersion) throws PortalServiceException {
        // Do the request
        HttpRequestBase method = null;
        WMSMethodMakerInterface methodMaker;
        try {
            methodMaker = getSupportedMethodMaker(wmsUrl, version);

            if (postMethod) {
                method = methodMaker.getFeatureInfoPost(wmsUrl, format, layer, srs, westBoundLongitude,
                        southBoundLatitude, eastBoundLongitude, northBoundLatitude, width, height, pointLng, pointLat,
                        pointX, pointY, styles, sldBody, feature_count);
            } else {
                method = methodMaker.getFeatureInfo(wmsUrl, format, layer, srs, westBoundLongitude, southBoundLatitude,
                        eastBoundLongitude, northBoundLatitude, width, height, pointLng, pointLat, pointX, pointY,
                        styles, sldBody, feature_count);
            }
            String response = serviceCaller.getMethodResponseAsString(method);
            //VT: a html response may not be xml valid therefore cannot go through the same validation process.
            //Rely on the service to return meaningful response to the user.
            if (format.toLowerCase().equals("text/html") ||
                format.toLowerCase().equals("text/plain") ||
                format.toLowerCase().equals("application/vnd.ogc.gml") ||
                format.toLowerCase().equals("application/vnd.ogc.gml/3.1.1")) {
                return response;
            } else {
                OWSExceptionParser.checkForExceptionResponse(response);
                return response;
            }

        } catch (NullPointerException npe) {
            npe.printStackTrace();
            throw new NullPointerException("Call configWMSVersion to setup the right wms method maker to use");

        } catch (Exception ex) {

            //VT:Making this more robust, maybe the wrong version is used;
            if (attemptOtherVersion) {
                for (WMSMethodMakerInterface maker : listOfSupportedWMSMethodMaker) {
                    if (!maker.getSupportedVersion().equals(version)) {
                        try {
                            return this.getFeatureInfo(wmsUrl, format, layer, srs, westBoundLongitude,
                                    southBoundLatitude, eastBoundLongitude, northBoundLatitude, width,
                                    height, pointLng, pointLat, pointX, pointY, styles, sldBody, postMethod,
                                    maker.getSupportedVersion(), feature_count, false);
                        } catch (Exception e) {
                            throw new PortalServiceException(method, "Failure requesting feature info", ex);
                        }
                    }
                }
            } else {
                throw new PortalServiceException(method, "Failure requesting feature info", ex);

            }

        }
        return "";
    }

    public HttpClientInputStream getMap(String url,String layer,String bbox,String sldBody, String version, String crs) throws OperationNotSupportedException, URISyntaxException, IOException{

        WMSMethodMakerInterface methodMaker;
        methodMaker = getSupportedMethodMaker(url, version);
        HttpRequestBase method = methodMaker.getMap(url,layer,bbox, sldBody, crs);
        HttpClientInputStream  response = serviceCaller.getMethodResponseAsStream(method);
        return response;

    }
    
    public String getStyle(String url,String sldUrl, String version) throws OperationNotSupportedException, URISyntaxException, IOException{
        WMSMethodMakerInterface methodMaker;
        methodMaker = getSupportedMethodMaker(url, version);
        String sldBody = methodMaker.getStyle(sldUrl);
        return sldBody;
    }    
}

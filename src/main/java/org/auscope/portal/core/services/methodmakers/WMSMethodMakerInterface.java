package org.auscope.portal.core.services.methodmakers;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesRecord;

public interface WMSMethodMakerInterface {

    /**
     *
     * @return the supported version
     */
    public String getSupportedVersion();

    /**
     * Generates a WMS method for making a GetCapabilities request
     *
     * @param wmsUrl
     *            The WMS endpoint (will have any existing query parameters preserved)
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase getCapabilitiesMethod(String wmsUrl) throws URISyntaxException;

    public GetCapabilitiesRecord getGetCapabilitiesRecord(HttpRequestBase method) throws IOException;

    /**
     * Test whether wms 1.3.0 is accepted. Not sure if there is a better way of testing though.
     * @param wmsUrl
     *             The WMS endpoint
     * @param version
     *             If supplied will return 'true' iff this version is supported
     * @param errStr
     *             Contains an error message iff returns false
     * @return
     *             Returns true iff WMS URL and its response can be accepted
     */
    public boolean accepts(String wmsUrl, String version, StringBuilder errStr );

    /**
     * Same as accepts() above, but included for backward compatibility.
     * Does not return an error message, only true/false.
     *
     * @param wmsUrl
     *             The WMS endpoint
     * @param version
     *             If supplied will return 'true' iff this version is supported
     * @return
     *             Returns true iff WMS URL and its response can be accepted
     */
    public boolean accepts(String wmsUrl, String version);

    /**
     * Generates a WMS request for downloading part of a map layer as an image
     *
     * @param wmsUrl
     *            The WMS endpoint (will have any existing query parameters preserved)
     * @param layer
     *            The name of the layer to download
     * @param imageMimeType
     *            The format of the image to download as
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
     *            [Optional] What style name should be applied
     * @param styleBody
     *            [Optional] Only valid for Geoserver WMS, a style sheet definition
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase getMapMethod(String wmsUrl, String layer, String imageMimeType, String srs,
            double westBoundLongitude, double southBoundLatitude, double eastBoundLongitude, double northBoundLatitude,
            int width, int height, String styles, String styleBody) throws URISyntaxException;

    /**
     * Returns a method for requesting a legend/key image for a particular layer
     *
     * @param wmsUrl
     *            The WMS endpoint (will have any existing query parameters preserved)
     * @param layerName
     *            The WMS layer name
     * @param width
     *            Desired output width in pixels
     * @param height
     *            Desired output height in pixels
     * @param styles
     *            What style name should be applied
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase getLegendGraphic(String wmsUrl, String layerName, int width, int height, String styles)
            throws URISyntaxException;

    /**
     * Generates a WMS request for downloading information about a user click on a particular GetMap request.
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
     * @param feature_count
     *            Maximum number of features that should be returned in the getFeatureInfo request
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase getFeatureInfo(String wmsUrl, String format, String layer, String srs,
            double westBoundLongitude, double southBoundLatitude, double eastBoundLongitude, double northBoundLatitude,
            int width, int height, double pointLng, double pointLat, int pointX, int pointY, String styles, String sld,
            String feature_count) throws URISyntaxException;

    /**
     * Generates a WMS request for downloading information about a user click on a particular GetMap request via the post method.
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
     * @param feature_count
     *            Maximum number of features that should be returned in the getFeatureInfo request
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase getFeatureInfoPost(String wmsUrl, String format, String layer, String srs,
            double westBoundLongitude, double southBoundLatitude, double eastBoundLongitude, double northBoundLatitude,
            int width, int height, double pointLng, double pointLat, int pointX, int pointY, String styles, String sld,
            String feature_count) throws URISyntaxException;

    /**
     * WMS get map
     * @throws URISyntaxException
     * @throws IOException
     */
    public HttpRequestBase getMap(String url,String layer,String bbox, String sldBody, String crs) throws URISyntaxException, IOException;
    /**
     * get style
     * @throws URISyntaxException
     * @throws IOException
     */
    public String getStyle(String sldUrl) throws URISyntaxException, IOException;    

}

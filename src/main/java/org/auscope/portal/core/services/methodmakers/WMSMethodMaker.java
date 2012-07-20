package org.auscope.portal.core.services.methodmakers;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.params.BasicHttpParams;

/**
 * A class for generating methods that can interact with a OGC Web Map Service
 * @author Josh Vote
 *
 */
public class WMSMethodMaker extends AbstractMethodMaker {

    /**
     * Generates a WMS method for making a GetCapabilities request
     * @param wmsUrl The WMS endpoint (will have any existing query parameters preserved)
     * @return
     */
    public HttpRequestBase getCapabilitiesMethod(String wmsUrl) {
        HttpGet method = new HttpGet(wmsUrl);

        BasicHttpParams params = extractQueryParams(wmsUrl);
        params.setParameter("service", "WMS");
        params.setParameter("request", "GetCapabilities");
        params.setParameter("version", "1.1.1");

        method.setParams(params);

        return method;
    }


    /**
     * Generates a WMS request for downloading part of a map layer as an image
     * @param wmsUrl The WMS endpoint (will have any existing query parameters preserved)
     * @param layer The name of the layer to download
     * @param imageMimeType The format of the image to download as
     * @param srs The spatial reference system for the bounding box
     * @param westBoundLongitude The west bound longitude of the bounding box
     * @param southBoundLatitude The south bound latitude of the bounding box
     * @param eastBoundLongitude The east bound longitude of the bounding box
     * @param northBoundLatitude The north bound latitude of the bounding box
     * @param width The desired output image width in pixels
     * @param height The desired output image height in pixels
     * @param styles [Optional] What style name should be applied
     * @param styleBody [Optional] Only valid for Geoserver WMS, a style sheet definition
     * @return
     */
    public HttpRequestBase getMapMethod(String wmsUrl, String layer, String imageMimeType, String srs, double westBoundLongitude, double southBoundLatitude, double eastBoundLongitude, double northBoundLatitude, int width, int height, String styles, String styleBody) {
        HttpGet method = new HttpGet(wmsUrl);

        BasicHttpParams params = extractQueryParams(wmsUrl);
        params.setParameter("service", "WMS");
        params.setParameter("request", "GetMap");
        params.setParameter("version", "1.1.1");
        params.setParameter("format", imageMimeType);
        params.setParameter("transparent", "TRUE");
        params.setParameter("layers", layer);
        if (styles != null) {
            params.setParameter("styles", styles);
        }
        //This is a geoserver specific URL param
        if (styleBody != null) {
            params.setParameter("sld_body", styleBody);
        }
        params.setParameter("srs", srs);
        params.setParameter("bbox", String.format("%1$s,%2$s,%3$s,%4$s",
                westBoundLongitude,southBoundLatitude, eastBoundLongitude, northBoundLatitude));

        params.setParameter("width", Integer.toString(width));
        params.setParameter("height", Integer.toString(height));

        method.setParams(params);

        return method;
    }

    /**
     * Returns a method for requesting a legend/key image for a particular layer
     * @param wmsUrl The WMS endpoint (will have any existing query parameters preserved)
     * @param layerName The WMS layer name
     * @param width Desired output width in pixels
     * @param height Desired output height in pixels
     * @param styles What style name should be applied
     * @return
     */
    public HttpRequestBase getLegendGraphic(String wmsUrl, String layerName, int width, int height, String styles) {
        HttpGet method = new HttpGet(wmsUrl);

        BasicHttpParams params = extractQueryParams(wmsUrl);
        params.setParameter("service", "WMS");
        params.setParameter("request", "GetLegendGraphic");
        params.setParameter("version", "1.1.1");
        params.setParameter("format", "image/png");
        params.setParameter("layers", layerName);
        params.setParameter("layer", layerName);
        if (styles != null && styles.trim().length() > 0) {
            params.setParameter("styles", styles.trim());
        }
        if (width > 0) {
            params.setParameter("width", Integer.toString(width));
        }
        if (height > 0) {
            params.setParameter("height", Integer.toString(height));
        }


        method.setParams(params);


        return method;
    }

    /**
     * Generates a WMS request for downloading information about a user click on a particular
     * GetMap request.
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
     */
    public HttpRequestBase getFeatureInfo(String wmsUrl, String format, String layer, String srs, double westBoundLongitude, double southBoundLatitude, double eastBoundLongitude, double northBoundLatitude, int width, int height, double pointLng, double pointLat, int pointX, int pointY, String styles) {
        HttpGet method = new HttpGet(wmsUrl);

        String bboxString = String.format("%1$s,%2$s,%3$s,%4$s",
                westBoundLongitude,
                southBoundLatitude,
                eastBoundLongitude,
                northBoundLatitude);

        BasicHttpParams params = extractQueryParams(wmsUrl);
        params.setParameter("service", "WMS");
        params.setParameter("request", "GetFeatureInfo");
        params.setParameter("version", "1.1.1");
        params.setParameter("layers", layer);
        params.setParameter("layer", layer);
        params.setParameter("BBOX", bboxString);
        params.setParameter("QUERY_LAYERS", layer);
        params.setParameter("INFO_FORMAT", format);
        params.setParameter("lng", Double.toString(pointLng));
        params.setParameter("lat", Double.toString(pointLat));
        params.setParameter("x", Integer.toString(pointX));
        params.setParameter("y", Integer.toString(pointY));
        params.setParameter("width", Integer.toString(width));
        params.setParameter("height", Integer.toString(height));
        if (styles != null && styles.trim().length() > 0) {
            params.setParameter("styles", styles.trim());
        }

        method.setParams(params);

        return method;
    }
}

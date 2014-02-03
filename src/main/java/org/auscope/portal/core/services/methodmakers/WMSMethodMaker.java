package org.auscope.portal.core.services.methodmakers;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesRecord;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesRecord_1_1_1;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesRecord_1_3_0;
import org.auscope.portal.core.util.HttpUtil;



/**
 * A class for generating methods that can interact with a OGC Web Map Service
 * @author Josh Vote
 *
 */
public class WMSMethodMaker extends AbstractMethodMaker implements WMSMethodMakerInterface {

    HttpServiceCaller serviceCaller = null;

    public WMSMethodMaker(HttpServiceCaller serviceCaller) {
        this.serviceCaller = serviceCaller;
    }

    /**
     * Generates a WMS method for making a GetCapabilities request
     * @param wmsUrl The WMS endpoint (will have any existing query parameters preserved)
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase getCapabilitiesMethod(String wmsUrl) throws URISyntaxException {

        List<NameValuePair> existingParam = this.extractQueryParams(wmsUrl); //preserve any existing query params

        existingParam.add(new BasicNameValuePair("service", "WMS"));
        existingParam.add(new BasicNameValuePair("request", "GetCapabilities"));
        existingParam.add(new BasicNameValuePair("version", "1.1.1"));
        //String paramString = URLEncodedUtils.format(existingParam, "utf-8");
        HttpGet method = new HttpGet();
        method.setURI(HttpUtil.parseURI(wmsUrl, existingParam));

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
     * @throws URISyntaxException
     */
    public HttpRequestBase getMapMethod(String wmsUrl, String layer, String imageMimeType, String srs, double westBoundLongitude, double southBoundLatitude, double eastBoundLongitude, double northBoundLatitude, int width, int height, String styles, String styleBody) throws URISyntaxException {

        List<NameValuePair> existingParam = this.extractQueryParams(wmsUrl); //preserve any existing query params

        existingParam.add(new BasicNameValuePair("service", "WMS"));
        existingParam.add(new BasicNameValuePair("request", "GetMap"));
        existingParam.add(new BasicNameValuePair("version", "1.1.1"));
        existingParam.add(new BasicNameValuePair("format", imageMimeType));
        existingParam.add(new BasicNameValuePair("transparent", "TRUE"));
        existingParam.add(new BasicNameValuePair("layers", layer));
        if (styles != null) {
            existingParam.add(new BasicNameValuePair("styles", styles));
        }
        //This is a geoserver specific URL param
        if (styleBody != null) {
            existingParam.add(new BasicNameValuePair("sld_body", styleBody));
        }
        existingParam.add(new BasicNameValuePair("srs", srs));
        existingParam.add(new BasicNameValuePair("bbox", String.format("%1$s,%2$s,%3$s,%4$s",
                westBoundLongitude,southBoundLatitude, eastBoundLongitude, northBoundLatitude)));

        existingParam.add(new BasicNameValuePair("width", Integer.toString(width)));
        existingParam.add(new BasicNameValuePair("height", Integer.toString(height)));

        HttpGet method = new HttpGet(wmsUrl);
        method.setURI(HttpUtil.parseURI(wmsUrl,existingParam));
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
     * @throws URISyntaxException
     */
    public HttpRequestBase getLegendGraphic(String wmsUrl, String layerName, int width, int height, String styles) throws URISyntaxException {

        List<NameValuePair> existingParam = this.extractQueryParams(wmsUrl); //preserve any existing query params

        existingParam.add(new BasicNameValuePair("service", "WMS"));
        existingParam.add(new BasicNameValuePair("request", "GetLegendGraphic"));
        existingParam.add(new BasicNameValuePair("version", "1.1.1"));
        existingParam.add(new BasicNameValuePair("format", "image/png"));
        existingParam.add(new BasicNameValuePair("layers", layerName));
        existingParam.add(new BasicNameValuePair("layer", layerName));
        if (styles != null && styles.trim().length() > 0) {
            existingParam.add(new BasicNameValuePair("styles", styles.trim()));
        }
        if (width > 0) {
            existingParam.add(new BasicNameValuePair("width", Integer.toString(width)));
        }
        if (height > 0) {
            existingParam.add(new BasicNameValuePair("height", Integer.toString(height)));
        }


        HttpGet method = new HttpGet(wmsUrl);
        method.setURI(HttpUtil.parseURI(wmsUrl,existingParam));


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
     * @throws URISyntaxException
     */
    public HttpRequestBase getFeatureInfo(String wmsUrl, String format, String layer, String srs, double westBoundLongitude, double southBoundLatitude, double eastBoundLongitude, double northBoundLatitude, int width, int height, double pointLng, double pointLat, int pointX, int pointY, String styles,String sldBody) throws URISyntaxException {

        String bboxString = String.format("%1$s,%2$s,%3$s,%4$s",
                westBoundLongitude,
                southBoundLatitude,
                eastBoundLongitude,
                northBoundLatitude);

        List<NameValuePair> existingParam = this.extractQueryParams(wmsUrl); //preserve any existing query params

        existingParam.add(new BasicNameValuePair("service", "WMS"));
        existingParam.add(new BasicNameValuePair("request", "GetFeatureInfo"));
        existingParam.add(new BasicNameValuePair("version", "1.1.1"));
        existingParam.add(new BasicNameValuePair("layers", layer));
        existingParam.add(new BasicNameValuePair("layer", layer));
        existingParam.add(new BasicNameValuePair("BBOX", bboxString));
        existingParam.add(new BasicNameValuePair("QUERY_LAYERS", layer));
        existingParam.add(new BasicNameValuePair("INFO_FORMAT", format));
        existingParam.add(new BasicNameValuePair("lng", Double.toString(pointLng)));
        existingParam.add(new BasicNameValuePair("lat", Double.toString(pointLat)));
        existingParam.add(new BasicNameValuePair("x", Integer.toString(pointX)));
        existingParam.add(new BasicNameValuePair("y", Integer.toString(pointY)));
        existingParam.add(new BasicNameValuePair("width", Integer.toString(width)));
        existingParam.add(new BasicNameValuePair("height", Integer.toString(height)));
        existingParam.add(new BasicNameValuePair("SRS", srs));
        if(sldBody != null && sldBody.trim().length() > 0){
            existingParam.add(new BasicNameValuePair("SLD_BODY", sldBody));
        }
        if (styles != null && styles.trim().length() > 0) {
            existingParam.add(new BasicNameValuePair("styles", styles.trim()));
        }



        HttpGet method = new HttpGet(wmsUrl);
        method.setURI(HttpUtil.parseURI(wmsUrl,existingParam));

        return method;
    }


    /**
     * Test whether wms 1.3.0 is accepted. Not sure if there is a better way of testing though.
     */
    @Override
    public boolean accepts(String wmsUrl) {
        try{
            List<NameValuePair> existingParam = this.extractQueryParams(wmsUrl); //preserve any existing query params

            existingParam.add(new BasicNameValuePair("service", "WMS"));
            existingParam.add(new BasicNameValuePair("request", "GetCapabilities"));
            existingParam.add(new BasicNameValuePair("version", "1.1.1"));
            //String paramString = URLEncodedUtils.format(existingParam, "utf-8");
            HttpGet method = new HttpGet();
            method.setURI(HttpUtil.parseURI(wmsUrl, existingParam));


            InputStream response = serviceCaller.getMethodResponseAsStream(method);

            GetCapabilitiesRecord record= new GetCapabilitiesRecord_1_1_1(response);

            return true;
        }catch(Exception e){
            return false;
        }

    }


    @Override
    public GetCapabilitiesRecord getGetCapabilitiesRecord(HttpRequestBase method)
            throws Exception {
        InputStream response = serviceCaller.getMethodResponseAsStream(method);

        return new GetCapabilitiesRecord_1_1_1(response);
    }
}

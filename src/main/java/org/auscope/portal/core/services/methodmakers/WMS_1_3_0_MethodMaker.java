package org.auscope.portal.core.services.methodmakers;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesRecord;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesRecord_1_3_0;
import org.auscope.portal.core.util.HttpUtil;
import org.xml.sax.SAXException;

/**
 * A class for generating methods that can interact with a OGC Web Map Service
 *
 * @author Josh Vote
 *
 */
public class WMS_1_3_0_MethodMaker extends AbstractMethodMaker implements WMSMethodMakerInterface {

    HttpServiceCaller serviceCaller = null;

    // -------------------------------------------------------------- Constants
    public static final String VERSION = "1.3.0";
    private final Log log = LogFactory.getLog(getClass());

    public WMS_1_3_0_MethodMaker(HttpServiceCaller serviceCaller) {
        this.serviceCaller = serviceCaller;
    }

    /**
     * Generates a WMS method for making a GetCapabilities request
     *
     * @param wmsUrl
     *            The WMS endpoint (will have any existing query parameters preserved)
     * @return
     * @throws URISyntaxException
     */
    @Override
    public HttpRequestBase getCapabilitiesMethod(String wmsUrl) throws URISyntaxException {

        List<NameValuePair> existingParam = this.extractQueryParams(wmsUrl); //preserve any existing query params

        existingParam.add(new BasicNameValuePair("service", "WMS"));
        existingParam.add(new BasicNameValuePair("request", "GetCapabilities"));
        existingParam.add(new BasicNameValuePair("version", "1.3.0"));
        //String paramString = URLEncodedUtils.format(existingParam, "utf-8");
        HttpGet method = new HttpGet();
        method.setURI(HttpUtil.parseURI(wmsUrl, existingParam));

        return method;
    }

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
    @Override
    public HttpRequestBase getMapMethod(String wmsUrl, String layer, String imageMimeType, String srs,
            double westBoundLongitude, double southBoundLatitude, double eastBoundLongitude, double northBoundLatitude,
            int width, int height, String styles, String styleBody) throws URISyntaxException {

        List<NameValuePair> existingParam = this.extractQueryParams(wmsUrl); //preserve any existing query params

        existingParam.add(new BasicNameValuePair("service", "WMS"));
        existingParam.add(new BasicNameValuePair("request", "GetMap"));
        existingParam.add(new BasicNameValuePair("version", "1.3.0"));
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
                southBoundLatitude, westBoundLongitude, northBoundLatitude, eastBoundLongitude)));

        existingParam.add(new BasicNameValuePair("width", Integer.toString(width)));
        existingParam.add(new BasicNameValuePair("height", Integer.toString(height)));

        HttpGet method = new HttpGet(wmsUrl);
        method.setURI(HttpUtil.parseURI(wmsUrl, existingParam));
        return method;
    }

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
    @Override
    public HttpRequestBase getLegendGraphic(String wmsUrl, String layerName, int width, int height, String styles)
            throws URISyntaxException {

        List<NameValuePair> existingParam = this.extractQueryParams(wmsUrl); //preserve any existing query params

        existingParam.add(new BasicNameValuePair("service", "WMS"));
        existingParam.add(new BasicNameValuePair("request", "GetLegendGraphic"));
        existingParam.add(new BasicNameValuePair("version", "1.3.0"));
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
        method.setURI(HttpUtil.parseURI(wmsUrl, existingParam));

        return method;
    }

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
     * @return
     * @throws URISyntaxException
     */
    @Override
    public HttpRequestBase getFeatureInfo(String wmsUrl, String format, String layer, String srs,
            double westBoundLongitude, double southBoundLatitude, double eastBoundLongitude, double northBoundLatitude,
            int width, int height, double pointLng, double pointLat, int pointX, int pointY, String styles,
            String sldBody, String feature_count) throws URISyntaxException {
        //VT: this is the same axis ordering as the GetMap request that works but somehow the GetFeatureInfo request is opposite
        //        String bboxString = String.format("%1$s,%2$s,%3$s,%4$s",
        //                southBoundLatitude,
        //                westBoundLongitude,
        //                northBoundLatitude,
        //                eastBoundLongitude
        //                );

        String bboxString = String.format("%1$s,%2$s,%3$s,%4$s",
                westBoundLongitude,
                southBoundLatitude,
                eastBoundLongitude,
                northBoundLatitude);

        if (feature_count.equals("0") || feature_count.isEmpty()) {
            feature_count = this.defaultFeature_count;
        }

        List<NameValuePair> existingParam = this.extractQueryParams(wmsUrl); //preserve any existing query params

        existingParam.add(new BasicNameValuePair("service", "WMS"));
        existingParam.add(new BasicNameValuePair("request", "GetFeatureInfo"));
        existingParam.add(new BasicNameValuePair("version", "1.3.0"));
        existingParam.add(new BasicNameValuePair("layers", layer));
        existingParam.add(new BasicNameValuePair("layer", layer));
        existingParam.add(new BasicNameValuePair("BBOX", bboxString));
        existingParam.add(new BasicNameValuePair("QUERY_LAYERS", layer));
        existingParam.add(new BasicNameValuePair("INFO_FORMAT", format));
        existingParam.add(new BasicNameValuePair("feature_count", feature_count));
        existingParam.add(new BasicNameValuePair("lng", Double.toString(pointLng)));
        existingParam.add(new BasicNameValuePair("lat", Double.toString(pointLat)));
        existingParam.add(new BasicNameValuePair("i", Integer.toString(pointX)));
        existingParam.add(new BasicNameValuePair("j", Integer.toString(pointY)));
        existingParam.add(new BasicNameValuePair("width", Integer.toString(width)));
        existingParam.add(new BasicNameValuePair("height", Integer.toString(height)));
        existingParam.add(new BasicNameValuePair("crs", srs));
        if (sldBody != null && sldBody.trim().length() > 0) {
            existingParam.add(new BasicNameValuePair("SLD_BODY", sldBody));
        }
        if (styles != null && styles.trim().length() > 0) {
            existingParam.add(new BasicNameValuePair("styles", styles.trim()));
        }

        HttpGet method = new HttpGet(wmsUrl);
        method.setURI(HttpUtil.parseURI(wmsUrl, existingParam));

        return method;
    }

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
     * @return
     * @throws URISyntaxException
     */
    @Override
    public HttpRequestBase getFeatureInfoPost(String wmsUrl, String format, String layer, String srs,
            double westBoundLongitude, double southBoundLatitude, double eastBoundLongitude, double northBoundLatitude,
            int width, int height, double pointLng, double pointLat, int pointX, int pointY, String styles,
            String sldBody, String feature_count) throws URISyntaxException {
        //VT: this is the same axis ordering as the GetMap request that works but somehow the GetFeatureInfo request is opposite
        //        String bboxString = String.format("%1$s,%2$s,%3$s,%4$s",
        //                southBoundLatitude,
        //                westBoundLongitude,
        //                northBoundLatitude,
        //                eastBoundLongitude
        //                );

        String bboxString = String.format("%1$s,%2$s,%3$s,%4$s",
                westBoundLongitude,
                southBoundLatitude,
                eastBoundLongitude,
                northBoundLatitude);

        if (feature_count.equals("0") || feature_count.isEmpty()) {
            feature_count = this.defaultFeature_count;
        }

        List<NameValuePair> existingParam = this.extractQueryParams(wmsUrl); //preserve any existing query params

        existingParam.add(new BasicNameValuePair("service", "WMS"));
        existingParam.add(new BasicNameValuePair("request", "GetFeatureInfo"));
        existingParam.add(new BasicNameValuePair("version", "1.3.0"));
        existingParam.add(new BasicNameValuePair("layers", layer));
        existingParam.add(new BasicNameValuePair("layer", layer));
        existingParam.add(new BasicNameValuePair("BBOX", bboxString));
        existingParam.add(new BasicNameValuePair("QUERY_LAYERS", layer));
        existingParam.add(new BasicNameValuePair("INFO_FORMAT", format));
        existingParam.add(new BasicNameValuePair("feature_count", feature_count));
        existingParam.add(new BasicNameValuePair("lng", Double.toString(pointLng)));
        existingParam.add(new BasicNameValuePair("lat", Double.toString(pointLat)));
        existingParam.add(new BasicNameValuePair("i", Integer.toString(pointX)));
        existingParam.add(new BasicNameValuePair("j", Integer.toString(pointY)));
        existingParam.add(new BasicNameValuePair("width", Integer.toString(width)));
        existingParam.add(new BasicNameValuePair("height", Integer.toString(height)));
        existingParam.add(new BasicNameValuePair("crs", srs));
        if (sldBody != null && sldBody.trim().length() > 0) {
            existingParam.add(new BasicNameValuePair("SLD_BODY", sldBody));
        }
        if (styles != null && styles.trim().length() > 0) {
            existingParam.add(new BasicNameValuePair("styles", styles.trim()));
        }

        HttpPost method = new HttpPost(wmsUrl);
        UrlEncodedFormEntity entity;
        try {
            entity = new UrlEncodedFormEntity(existingParam, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new URISyntaxException(e.getMessage(), "Error parsing UrlEncodedFormEntity");
        }
        method.setEntity(entity);

        return method;
    }


    @Override
    public HttpRequestBase getMap(String url,String layer,String bbox, String sldBody, String crs) throws URISyntaxException, IOException {

        List<NameValuePair> existingParam = this.extractQueryParams(url); //preserve any existing query params

        existingParam.add(new BasicNameValuePair("service", "WMS"));
        existingParam.add(new BasicNameValuePair("request", "GetMap"));
        existingParam.add(new BasicNameValuePair("version", "1.3.0"));
        if (sldBody != null && sldBody.trim().length() > 0) {
            existingParam.add(new BasicNameValuePair("SLD_BODY", sldBody));
        }
        existingParam.add(new BasicNameValuePair("DISPLAYOUTSIDEMAXEXTENT", "TRUE"));
        existingParam.add(new BasicNameValuePair("EXCEPTIONS", "BLANK"));
        existingParam.add(new BasicNameValuePair("LAYERS", layer));
        existingParam.add(new BasicNameValuePair("FORMAT", "image/png"));
        existingParam.add(new BasicNameValuePair("TRANSPARENT", "TRUE"));
        existingParam.add(new BasicNameValuePair("CRS", crs));
        existingParam.add(new BasicNameValuePair("BBOX", bbox));
        existingParam.add(new BasicNameValuePair("WIDTH", "256"));
        existingParam.add(new BasicNameValuePair("HEIGHT", "256"));
        existingParam.add(new BasicNameValuePair("STYLES", ""));

        HttpPost method = new HttpPost(url);
        UrlEncodedFormEntity entity;
        try {
            //VT: this encodes white spaces to "+" and argis doesn't seem to accept "+" instead requiring it to be %20.
            entity = new UrlEncodedFormEntity(existingParam, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new URISyntaxException(e.getMessage(), "Error parsing UrlEncodedFormEntity");
        }
        method.setEntity(entity);

        return method;
    }
    
    @Override
    public String getStyle(String sldUrl) throws URISyntaxException, IOException {

        HttpGet getSld = new HttpGet(sldUrl);
        String sldBody =  serviceCaller.getMethodResponseAsString(getSld);

        return sldBody;
    }
    /**
     * Test whether wms 1.3.0 is accepted. Not sure if there is a better way of testing though.
     */
    @SuppressWarnings("unused")
    @Override
    public boolean accepts(String wmsUrl, String version, StringBuilder errStr) {
        if (version != null) {
            if (version.equals(this.getSupportedVersion())==false) {
                log.error("WMS_1_3_0_MethodMaker::accepts() WMS version not supported");
                errStr.delete(0, errStr.length());
                errStr.append("I can resolve your WMS URL, but the WMS version is not supported");
                return false;
            }
            return true;
        }
        try {
            List<NameValuePair> existingParam = this.extractQueryParams(wmsUrl); //preserve any existing query params

            existingParam.add(new BasicNameValuePair("service", "WMS"));
            existingParam.add(new BasicNameValuePair("request", "GetCapabilities"));
            existingParam.add(new BasicNameValuePair("version", "1.3.0"));
            //String paramString = URLEncodedUtils.format(existingParam, "utf-8");
            HttpGet method = new HttpGet();
            method.setURI(HttpUtil.parseURI(wmsUrl, existingParam));

            try (InputStream response = serviceCaller.getMethodResponseAsStream(method)) {
                new GetCapabilitiesRecord_1_3_0(response);
            } catch (IOException e) {
                // IOException is equivalent to HTTPException
                // So we have to catch IOException here, rather than below, in order to distinguish
                // between HTTP errors and IO errors
                log.error("WMS_1_3_0_MethodMaker::Accepts(): IOException: "+e.getMessage());
                errStr.delete(0, errStr.length());
                errStr.append("I can resolve your WMS URL, but could not retrieve the web page");
                return false;
            }
            return true;

        } catch (SAXException|ParserConfigurationException e) {
            log.error("WMS_1_3_0_MethodMaker::Accepts(): SAXException or ParserConfigurationException: "+e.getMessage()+"| type: "+e.toString());
            errStr.delete(0, errStr.length());
            errStr.append("I can resolve your WMS URL, but there was an XML format error");
            return false;

        } catch (URISyntaxException e1) {
            log.error("WMS_1_3_0_MethodMaker::Accepts(): URISyntaxException: "+e1.getMessage()+"| type: "+e1.toString());
            errStr.delete(0, errStr.length());
            errStr.append("Either I cannot resolve your WMS URL or cannot retrieve the web page");
            return false;
        }
    }

    /**
     *
     * Same as accepts() above, but included for backward compatibility
     */
    @Override
    public boolean accepts(String wmsUrl, String version) {
        StringBuilder errStr = new StringBuilder();
        return accepts(wmsUrl,version,errStr);
    }


    @Override
    public GetCapabilitiesRecord getGetCapabilitiesRecord(HttpRequestBase method) throws IOException {
        try (InputStream response = serviceCaller.getMethodResponseAsStream(method)) {
            return new GetCapabilitiesRecord_1_3_0(response);
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public String getSupportedVersion() {
        return WMS_1_3_0_MethodMaker.VERSION;
    }
}

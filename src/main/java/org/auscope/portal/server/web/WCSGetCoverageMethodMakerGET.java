package org.auscope.portal.server.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.csw.record.CSWGeographicBoundingBox;
import org.springframework.stereotype.Repository;

@Repository
public class WCSGetCoverageMethodMakerGET implements
        IWCSGetCoverageMethodMaker {

    private final Log logger = LogFactory.getLog(getClass());

    public HttpMethodBase makeMethod(String serviceURL, String layerName,
            String format, String outputCrs, int outputWidth, int outputHeight, double outputResX, double outputResY, String inputCrs,
            CSWGeographicBoundingBox bbox, String timeConstraint, Map<String, String> customParams) throws Exception {
        GetMethod httpMethod = new GetMethod(serviceURL);

        //Do some simple error checking to align with WCS standard
        if ((outputWidth > 0 && outputHeight == 0) ||
                (outputHeight > 0 && outputWidth == 0))
            throw new IllegalArgumentException("Height/Width cannot be used without Width/Height");
        if ((outputResX > 0 && outputResY == 0 ||
                outputResY > 0 && outputResX == 0))
            throw new IllegalArgumentException("outputResX/outputResY cannot be used without outputResY/outputResX");
        if ((outputResX > 0 || outputResY > 0) &&
                (outputWidth > 0 || outputHeight > 0))
            throw new IllegalArgumentException("outputResX/outputResY cannot be used with outputWidth/outputHeight");
        if (outputResX == 0 && outputResY == 0 &&
                outputWidth == 0 && outputHeight == 0)
            throw new IllegalArgumentException("One of outputResX/outputResY or outputWidth/outputHeight");
        if (bbox==null && (timeConstraint == null || timeConstraint.isEmpty()))
            throw new IllegalArgumentException("You must specify at least one bbox or time constraint");
        if (inputCrs==null || inputCrs.isEmpty())
            throw new IllegalArgumentException("You must specify an inputCrs");

        List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new NameValuePair("service", "WCS"));
        params.add(new NameValuePair("version", "1.0.0"));
        params.add(new NameValuePair("request", "GetCoverage"));
        params.add(new NameValuePair("coverage", layerName));
        params.add(new NameValuePair("format", format));

        if (outputCrs != null && !outputCrs.isEmpty()) {
            params.add(new NameValuePair("response_crs", outputCrs));
        }

        if (inputCrs != null && !inputCrs.isEmpty()) {
            params.add(new NameValuePair("crs", inputCrs));
        }

        if (bbox != null) {
            double adjustedWestLng = Math.min(bbox.getWestBoundLongitude(), bbox.getEastBoundLongitude());
            double adjustedEastLng = Math.max(bbox.getWestBoundLongitude(), bbox.getEastBoundLongitude());

            //this is so we can fetch data when our bbox is crossing the anti meridian
            //Otherwise our bbox wraps around the WRONG side of the planet
            if (adjustedWestLng <= 0 && adjustedEastLng >= 0 ||
                adjustedWestLng >= 0 && adjustedEastLng <= 0) {
                adjustedWestLng = (bbox.getWestBoundLongitude() < 0) ? (180 - bbox.getWestBoundLongitude()) : bbox.getWestBoundLongitude();
                adjustedEastLng = (bbox.getEastBoundLongitude() < 0) ? (180 - bbox.getEastBoundLongitude()) : bbox.getEastBoundLongitude();
            }

            params.add(new NameValuePair("bbox",
                    String.format("%1$f,%2$f,%3$f,%4$f",
                            Math.min(adjustedWestLng, adjustedEastLng),
                            bbox.getSouthBoundLatitude(),
                            Math.max(adjustedWestLng, adjustedEastLng),
                            bbox.getNorthBoundLatitude())));
        }

        if (timeConstraint != null && !timeConstraint.isEmpty()) {
            params.add(new NameValuePair("time", timeConstraint));
        }

        if (outputWidth > 0) {
            params.add(new NameValuePair("width", Integer.toString(outputWidth)));
        }

        if (outputHeight > 0) {
            params.add(new NameValuePair("height", Integer.toString(outputHeight)));
        }

        if (outputResX > 0) {
            params.add(new NameValuePair("resx", Double.toString(outputResX)));
        }

        if (outputResY > 0) {
            params.add(new NameValuePair("resy", Double.toString(outputResY)));
        }

        if (customParams != null) {
            for (String key : customParams.keySet()) {
                params.add(new NameValuePair(key, customParams.get(key).toString()));
            }
        }

        httpMethod.setQueryString(params.toArray(new NameValuePair[params.size()]));

        logger.debug(String.format("url='%1$s' query='%2$s'", serviceURL, httpMethod.getQueryString()));

        return httpMethod;
    }
}

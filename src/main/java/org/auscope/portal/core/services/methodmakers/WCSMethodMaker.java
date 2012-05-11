package org.auscope.portal.core.services.methodmakers;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;
import org.auscope.portal.core.services.responses.wcs.Resolution;
import org.auscope.portal.core.services.responses.wcs.TimeConstraint;
import org.springframework.stereotype.Repository;

/**
 * Class for creating HttpMethods for interacting with an OGC Web Coverage Service
 * @author Josh Vote
 *
 */
@Repository
public class WCSMethodMaker {

    private final Log logger = LogFactory.getLog(getClass());

    /**
     * Method for creating a GetCoverage HttpMethodBase
     * @param serviceUrl The WCS endpoint to query
     * @param coverageName The coverage layername to request
     * @param inputCrs the coordinate reference system to query
     * @param format File format to request
     * @param outputCrs [Optional] The Coordinate reference system of the output data
     * @param outputSize The size of the coverage to request (cannot be used with outputResolution)
     * @param outputResolution When requesting a georectified grid coverage, this requests a subset with a specific spatial resolution (Not compatible with outputSize)
     * @param bbox [Optional] Spatial bounds to limit request
     * @param timeConstraint [Optional] Temporal bounds to limit request
     * @param customParams [Optional] a list of additional request parameters
     * @return
     * @throws Exception
     */
    public HttpMethodBase getCoverageMethod(String serviceURL, String coverageName,
            String format, String outputCrs, Dimension outputSize, Resolution outputResolution, String inputCrs,
            CSWGeographicBoundingBox bbox, TimeConstraint timeConstraint, Map<String, String> customParams) {
        GetMethod httpMethod = new GetMethod(serviceURL);

        //Do some simple error checking to align with WCS standard
        if ((outputSize != null) && (outputResolution != null)) {
            throw new IllegalArgumentException("outputResolution cannot be used with outputSize");
        }
        if ((outputSize == null) && (outputResolution == null)) {
            throw new IllegalArgumentException("One of outputResolution or outputSize must be used");
        }
        if (bbox==null && (timeConstraint == null)) {
            throw new IllegalArgumentException("You must specify at least one bbox or time constraint");
        }
        if (inputCrs==null || inputCrs.isEmpty()) {
            throw new IllegalArgumentException("You must specify an inputCrs");
        }

        List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new NameValuePair("service", "WCS"));
        params.add(new NameValuePair("version", "1.0.0"));
        params.add(new NameValuePair("request", "GetCoverage"));
        params.add(new NameValuePair("coverage", coverageName));
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

        if (timeConstraint != null) {
            params.add(new NameValuePair("time", timeConstraint.getConstraint()));
        }

        if (outputSize != null) {
            params.add(new NameValuePair("width", Integer.toString(outputSize.width)));
            params.add(new NameValuePair("height", Integer.toString(outputSize.height)));
        }

        if (outputResolution != null) {
            params.add(new NameValuePair("resx", Double.toString(outputResolution.getX())));
            params.add(new NameValuePair("resy", Double.toString(outputResolution.getY())));
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

    /**
     * Method for creating a DescribeCoverage request for a particular coverage
     * @param serviceUrl The WCS endpoint to query
     * @param coverageName The name of the coverage to query
     * @return
     * @throws Exception
     */
    public HttpMethodBase describeCoverageMethod(String serviceUrl, String coverageName) {
        GetMethod httpMethod = new GetMethod(serviceUrl);
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        //Do some simple error checking to align with WCS standard
        if (serviceUrl == null || serviceUrl.isEmpty())
            throw new IllegalArgumentException("You must specify a serviceUrl");
        if (coverageName == null || coverageName.isEmpty())
            throw new IllegalArgumentException("You must specify a coverageName");

        params.add(new NameValuePair("service", "WCS"));
        params.add(new NameValuePair("version", "1.0.0"));
        params.add(new NameValuePair("request", "DescribeCoverage"));
        params.add(new NameValuePair("coverage", coverageName));

        httpMethod.setQueryString(params.toArray(new NameValuePair[params.size()]));

        logger.debug(String.format("url='%1$s' query='%2$s'", serviceUrl, httpMethod.getQueryString()));

        return httpMethod;
    }

}

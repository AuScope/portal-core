package org.auscope.portal.core.services.methodmakers;

import java.awt.Dimension;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;
import org.auscope.portal.core.services.responses.wcs.Resolution;
import org.auscope.portal.core.services.responses.wcs.TimeConstraint;

/**
 * Class for creating HttpMethods for interacting with an OGC Web Coverage Service
 * 
 * @author Josh Vote
 *
 */
public class WCSMethodMaker extends AbstractMethodMaker {

    private final Log logger = LogFactory.getLog(getClass());

    /**
     * Method for creating a GetCoverage HttpMethodBase
     * 
     * @param serviceUrl
     *            The WCS endpoint to query
     * @param coverageName
     *            The coverage layername to request
     * @param inputCrs
     *            the coordinate reference system to query
     * @param format
     *            File format to request
     * @param outputCrs
     *            [Optional] The Coordinate reference system of the output data
     * @param outputSize
     *            The size of the coverage to request (cannot be used with outputResolution)
     * @param outputResolution
     *            When requesting a georectified grid coverage, this requests a subset with a specific spatial resolution (Not compatible with outputSize)
     * @param bbox
     *            [Optional] Spatial bounds to limit request
     * @param timeConstraint
     *            [Optional] Temporal bounds to limit request
     * @param customParams
     *            [Optional] a list of additional request parameters
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase getCoverageMethod(String serviceUrl, String coverageName,
            String format, String outputCrs, Dimension outputSize, Resolution outputResolution, String inputCrs,
            CSWGeographicBoundingBox bbox, TimeConstraint timeConstraint, Map<String, String> customParams)
            throws URISyntaxException {
        HttpGet httpMethod = new HttpGet(serviceUrl);

        //Do some simple error checking to align with WCS standard
        if ((outputSize != null) && (outputResolution != null)) {
            throw new IllegalArgumentException("outputResolution cannot be used with outputSize");
        }
        if ((outputSize == null) && (outputResolution == null)) {
            throw new IllegalArgumentException("One of outputResolution or outputSize must be used");
        }
        if (bbox == null && (timeConstraint == null)) {
            throw new IllegalArgumentException("You must specify at least one bbox or time constraint");
        }
        if (inputCrs == null || inputCrs.isEmpty()) {
            throw new IllegalArgumentException("You must specify an inputCrs");
        }

        URIBuilder builder = new URIBuilder(serviceUrl);

        builder.setParameter("service", "WCS");
        builder.setParameter("version", "1.0.0");
        builder.setParameter("request", "GetCoverage");
        builder.setParameter("coverage", coverageName);
        builder.setParameter("format", format);

        if (outputCrs != null && !outputCrs.isEmpty()) {
            builder.setParameter("response_crs", outputCrs);
        }

        if (!inputCrs.isEmpty()) {
            builder.setParameter("crs", inputCrs);
        }

        if (bbox != null) {
            double adjustedWestLng = Math.min(bbox.getWestBoundLongitude(), bbox.getEastBoundLongitude());
            double adjustedEastLng = Math.max(bbox.getWestBoundLongitude(), bbox.getEastBoundLongitude());

            //this is so we can fetch data when our bbox is crossing the anti meridian
            //Otherwise our bbox wraps around the WRONG side of the planet
            if (adjustedWestLng <= 0 && adjustedEastLng >= 0 ||
                    adjustedWestLng >= 0 && adjustedEastLng <= 0) {
                adjustedWestLng = (bbox.getWestBoundLongitude() < 0) ? (180 - bbox.getWestBoundLongitude()) : bbox
                        .getWestBoundLongitude();
                adjustedEastLng = (bbox.getEastBoundLongitude() < 0) ? (180 - bbox.getEastBoundLongitude()) : bbox
                        .getEastBoundLongitude();
            }

            builder.setParameter("bbox",
                    String.format("%1$f,%2$f,%3$f,%4$f",
                            Math.min(adjustedWestLng, adjustedEastLng),
                            bbox.getSouthBoundLatitude(),
                            Math.max(adjustedWestLng, adjustedEastLng),
                            bbox.getNorthBoundLatitude()));
        }

        if (timeConstraint != null) {
            builder.setParameter("time", timeConstraint.getConstraint());
        }

        if (outputSize != null) {
            builder.setParameter("width", Integer.toString(outputSize.width));
            builder.setParameter("height", Integer.toString(outputSize.height));
        }

        if (outputResolution != null) {
            builder.setParameter("resx", Double.toString(outputResolution.getX()));
            builder.setParameter("resy", Double.toString(outputResolution.getY()));
        }

        if (customParams != null) {
            for (String key : customParams.keySet()) {
                builder.setParameter(key, customParams.get(key).toString());
            }
        }

        httpMethod.setURI(builder.build());

        logger.debug(String.format("url='%1$s' query='%2$s'", serviceUrl, httpMethod.getURI().getQuery()));

        return httpMethod;
    }

    /**
     * Method for creating a DescribeCoverage request for a particular coverage
     * 
     * @param serviceUrl
     *            The WCS endpoint to query
     * @param coverageName
     *            The name of the coverage to query
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase describeCoverageMethod(String serviceUrl, String coverageName) throws URISyntaxException {
        HttpGet httpMethod = new HttpGet();
        URIBuilder builder = new URIBuilder(serviceUrl);

        //Do some simple error checking to align with WCS standard
        if (serviceUrl == null || serviceUrl.isEmpty())
            throw new IllegalArgumentException("You must specify a serviceUrl");
        if (coverageName == null || coverageName.isEmpty())
            throw new IllegalArgumentException("You must specify a coverageName");

        builder.setParameter("service", "WCS");
        builder.setParameter("version", "1.0.0");
        builder.setParameter("request", "DescribeCoverage");
        builder.setParameter("coverage", coverageName);

        httpMethod.setURI(builder.build());

        logger.debug(String.format("url='%1$s' query='%2$s'", serviceUrl, httpMethod.getURI().getQuery()));

        return httpMethod;
    }

    /**
     * Method for creating a GetCapabilities request for a WCS service
     * 
     * @param serviceUrl
     *            The WCS endpoint to query
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase getCapabilitiesMethod(String serviceUrl) throws URISyntaxException {
        HttpGet httpMethod = new HttpGet();
        URIBuilder builder = new URIBuilder(serviceUrl);

        //Do some simple error checking to align with WCS standard
        if (serviceUrl == null || serviceUrl.isEmpty())
            throw new IllegalArgumentException("You must specify a serviceUrl");

        builder.setParameter("service", "WCS");
        builder.setParameter("version", "1.0.0");
        builder.setParameter("request", "GetCapabilities");

        httpMethod.setURI(builder.build());

        logger.debug(String.format("url='%1$s' query='%2$s'", serviceUrl, httpMethod.getURI().getQuery()));

        return httpMethod;
    }
}

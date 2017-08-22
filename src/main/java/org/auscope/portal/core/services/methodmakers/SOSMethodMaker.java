package org.auscope.portal.core.services.methodmakers;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.joda.time.DateTime;

/**
 * A class for generating Sensor Observation Service requests.
 *
 * @author Florence Tan
 */
public class SOSMethodMaker extends AbstractMethodMaker {
    public static final String SOS_VERSION = "2.0.0";
    public static final String SOS_SERVICE = "SOS";

    /** Log object for this class. */
    private final Log log = LogFactory.getLog(getClass());

    /**
     * Generates a SOS method for making a GetCapabilities request
     * 
     * @param serviceUrl
     *            - required, SOS End Point
     * @return
     */
    //    public HttpMethodBase getCapabilitiesMethod(String serviceUrl) {
    //        GetMethod method = new GetMethod(serviceUrl);
    //        List<NameValuePair> options = new ArrayList<NameValuePair>();
    //
    //        options.addAll(this.extractQueryParams(serviceUrl)); //preserve any existing query params
    //        options.add(new NameValuePair("service", SOS_SERVICE));
    //        options.add(new NameValuePair("request", "GetCapabilities"));
    //        options.add(new NameValuePair("acceptVersions", SOS_VERSION));
    //
    //        method.setQueryString(options.toArray(new NameValuePair[options.size()]));
    //
    //        return method;
    //    }

    /**
     * Creates a PostMethod given the following parameters.
     * 
     * @param serviceUrl
     *            - required, SOS End Point
     * @param request
     *            - required, service type identifier (e.g. GetObservation)
     * @param featureOfInterest
     *            - optional - pointer to a feature of interest for which observations are requested
     * @param beginPosition
     *            - optional - start time period for which observations are requested the time should conform to ISO format: YYYY-MM-DDTHH:mm:ss+HH.
     * @param endPosition
     *            - optional - end time period(s) for which observations are requested the time should conform to ISO format: YYYY-MM-DDTHH:mm:ss+HH. - both
     *            beginPosition and endPosition must go in pair, if one exists, the other must exists
     * @param bbox
     *            - optional - FilterBoundingBox object -> convert to 52NorthSOS BBOX format : maxlat,minlon,minlat,maxlon(,srsURI) srsURI format :
     *            "http://www.opengis.net/def/crs/EPSG/0/"+epsg code
     * @return httpMethod
     */
    public HttpRequestBase makePostMethod(String serviceUrl, String request, String featureOfInterest,
            Date beginPosition, Date endPosition, FilterBoundingBox bbox) {

        // Make sure the required parameters are given
        if (serviceUrl == null || serviceUrl.equals("")) {
            throw new IllegalArgumentException("serviceUrl parameter can not be null or empty.");
        }

        if (request == null || request.equals("")) {
            throw new IllegalArgumentException("request parameter can not be null or empty.");
        }

        HttpPost httpMethod = new HttpPost(serviceUrl);

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append(String.format("<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\"\n"));
        sb.append("              xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        sb.append("              xsi:schemaLocation=\"http://www.w3.org/2003/05/soap-envelope \n");
        sb.append("                                  http://www.w3.org/2003/05/soap-envelope\">\n");
        sb.append("  <env:Body>\n");
        sb.append("    <sos:" + request + "\n");
        sb.append("        service=\"" + SOS_SERVICE + "\" version=\"" + SOS_VERSION + "\"\n");
        sb.append("        xmlns:sos=\"http://www.opengis.net/sos/2.0\"\n");
        sb.append("        xmlns:fes=\"http://www.opengis.net/fes/2.0\"\n");
        sb.append("        xmlns:gml=\"http://www.opengis.net/gml/3.2\"\n");
        sb.append("        xmlns:swe=\"http://www.opengis.net/swe/2.0\"\n");
        sb.append("        xmlns:swes=\"http://www.opengis.net/swes/2.0\"\n");
        sb.append("        xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n");
        sb.append("        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        sb.append("        xsi:schemaLocation=\"http://www.opengis.net/sos/2.0 http://schemas.opengis.net/sos/2.0/sos.xsd\">\n");
        if ((beginPosition != null && !beginPosition.equals("")) && (!endPosition.equals(""))) {
            DateTime bpDateTime = new DateTime(beginPosition);
            DateTime epDateTime = new DateTime(endPosition);
            sb.append("        <sos:temporalFilter>\n");
            sb.append("          <fes:During>\n");
            sb.append("            <fes:ValueReference>phenomenonTime</fes:ValueReference>\n");
            sb.append("              <gml:TimePeriod gml:id=\"tp_1\">\n");
            sb.append("                <gml:beginPosition>" + bpDateTime + "</gml:beginPosition>\n");
            sb.append("                <gml:endPosition>" + epDateTime + "</gml:endPosition>\n");
            sb.append("              </gml:TimePeriod>\n");
            sb.append("          </fes:During>\n");
            sb.append("        </sos:temporalFilter>\n");
        }
        if (featureOfInterest != null && !featureOfInterest.isEmpty()) {
            sb.append("        <sos:featureOfInterest>" + featureOfInterest + "</sos:featureOfInterest>\n");
        }
        if (bbox != null && !bbox.equals("")) {
            sb.append("        <sos:spatialFilter>\n");
            sb.append("          <fes:BBOX>\n");
            sb.append("            <fes:ValueReference>om:featureOfInterest/sams:SF_SpatialSamplingFeature/sams:shape</fes:ValueReference>\n");
            if (bbox.getBboxSrs() != null && !bbox.getBboxSrs().equals("")) {
                String[] epsgcode = bbox.getBboxSrs().split(":");
                sb.append("              <gml:Envelope srsName=\"http://www.opengis.net/def/crs/EPSG/0/" + epsgcode[1]
                        + "\">\n");
            } else {
                sb.append("              <gml:Envelope srsName=\"http://www.opengis.net/def/crs/EPSG/0/4326\">\n");
            }
            //Converting Google Map bbox format : lower corner point (minWestBoundLong,minNothBoundLatitude) and
            //                                    upper corner point (maxWestBoundLong,maxNothBoundLatitude)
            //TO SOS bbox format                : lower corner point (maxNothBoundLatitude,minWestBoundLong) and
            //                                    upper corner point (minNothBoundLatitude,maxWestBoundLong)
            double[] lowerCornerPoints = bbox.getLowerCornerPoints();
            double[] upperCornerPoints = bbox.getUpperCornerPoints();
            sb.append("                <gml:lowerCorner>" + upperCornerPoints[1] + " " + lowerCornerPoints[0]
                    + "</gml:lowerCorner>\n");
            sb.append("                <gml:upperCorner>" + lowerCornerPoints[1] + " " + upperCornerPoints[0]
                    + "</gml:upperCorner>\n");
            sb.append("              </gml:Envelope>\n");
            sb.append("          </fes:BBOX>\n");
            sb.append("        </sos:spatialFilter>\n");
        }
        sb.append("    </sos:" + request + ">\n");
        sb.append("  </env:Body>");
        sb.append("</env:Envelope>");

        log.debug("Service URL:\n\t" + serviceUrl);
        log.debug("Get Feature Query:\n" + sb.toString());

        // If this does not work, try params: "text/xml; charset=ISO-8859-1"
        httpMethod.setEntity(new StringEntity(sb.toString(), "UTF-8"));

        return httpMethod;
    }

    public HttpRequestBase makePostMethod(String serviceUrl, String request) {
        return makePostMethod(serviceUrl, request, null, null, null, null);
    }

}

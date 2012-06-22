package org.auscope.portal.core.services.methodmakers;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker.ResultType;
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
     * @param serviceURL - required, SOS End Point
     * @return
     * @throws Exception if service URL or request  not provided
     * 
     * @return
     */
//    public HttpMethodBase getCapabilitiesMethod(String serviceURL) {
//        GetMethod method = new GetMethod(serviceURL);
//        List<NameValuePair> options = new ArrayList<NameValuePair>();
//
//        options.addAll(this.extractQueryParams(serviceURL)); //preserve any existing query params
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
     * @param serviceURL - required, SOS End Point
     * @param request - required, service type identifier (e.g. GetObservation) 
     * @param featureOfInterest- optional - pointer to a feature of interest for which observations are requested 
     * @param eventTime - optional - time period(s) (start and end) for which observations are requested 
     *                             - the time should conform to ISO format: YYYY-MM-DDTHH:mm:ss+HH. 
     *                             - Periods of time (start and end) are separated by "/". 
     *                               For example: 1990-01-01T00:00:00.000+08:00/2010-02-20T00:00:00.000+08:00
     * @param BBOX - optional - Bounding Box format : minlon,minlat,maxlon,maxlat(,srsURI). 
     * 												  The first four parameters are expected as decimal degrees. 
     * 												  SrsURI is optional and could take a value of "urn:ogc:def:crs:EPSG:6.5:4326"                             
     * @return httpMethod
     * @throws Exception if service URL or request are not provided
     */
    public HttpMethodBase makePostMethod(String serviceURL, String request, String featureOfInterest, String eventTime, String BBOX) {
    	
    	String[] timePeriod;
    	String[] boundingBox;
    	
        // Make sure the required parameters are given
        if (serviceURL == null || serviceURL.equals("")) {
            throw new IllegalArgumentException("serviceURL parameter can not be null or empty.");
        }
        
        if (request == null || request.equals("")) {
            throw new IllegalArgumentException("request parameter can not be null or empty.");
        }        

        PostMethod httpMethod = new PostMethod(serviceURL);

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
        if (eventTime != null && !eventTime.isEmpty()) {
        	timePeriod = eventTime.split("/");
        	sb.append("        <sos:temporalFilter>\n");
        	sb.append("          <fes:During>\n");
        	sb.append("            <fes:ValueReference>phenomenonTime</fes:ValueReference>\n");
        	sb.append("              <gml:TimePeriod gml:id=\"tp_1\">\n");
        	sb.append("                <gml:beginPosition>" + timePeriod[0] + "</gml:beginPosition>\n");
        	sb.append("                <gml:endPosition>" + timePeriod[1] + "</gml:endPosition>\n");
        	sb.append("              </gml:TimePeriod>\n");
        	sb.append("          </fes:During>\n");
        	sb.append("        </sos:temporalFilter>\n");
        }
        if (featureOfInterest != null && !featureOfInterest.isEmpty()) {
            sb.append("        <sos:featureOfInterest>" + featureOfInterest + "</sos:featureOfInterest>\n");
        }  
        if (BBOX != null && !BBOX.isEmpty()) {
        	boundingBox = BBOX.split(",");
        	sb.append("        <sos:spatialFilter>\n");
        	sb.append("          <fes:BBOX>\n");
        	sb.append("            <fes:ValueReference>om:featureOfInterest/sams:SF_SpatialSamplingFeature/sams:shape</fes:ValueReference>\n");
        	if (boundingBox.length>4) {
        		sb.append("              <gml:Envelope srsName=\"" + boundingBox[4] + "\">\n");
        	} else {
        		sb.append("              <gml:Envelope srsName=\"http://www.opengis.net/def/crs/EPSG/0/4326\">\n");
        	}
        	sb.append("                <gml:lowerCorner>" + boundingBox[0] + " " + boundingBox[2] + "</gml:lowerCorner>\n");
        	sb.append("                <gml:upperCorner>" + boundingBox[1] + " " + boundingBox[3] + "</gml:upperCorner>\n");
        	sb.append("              </gml:Envelope>\n");
        	sb.append("          </fes:BBOX>\n");
        	sb.append("        </sos:spatialFilter>\n");
        }
        sb.append("    </sos:" + request + ">\n");
        sb.append("  </env:Body>");
        sb.append("</env:Envelope>");

        log.debug("Service URL:\n\t" + serviceURL);
        log.debug("Get Feature Query:\n" + sb.toString());

        // If this does not work, try params: "text/xml; charset=ISO-8859-1"
        try {
            httpMethod.setRequestEntity(new StringRequestEntity(sb.toString(), null, null));
        } catch (UnsupportedEncodingException e) {
            log.error("Unsupported encoding", e);
        }

        return httpMethod;
    }
        
    public HttpMethodBase makePostMethod(String serviceURL, String request) {
    	return makePostMethod(serviceURL,request,null,null,null);
    }
   
}        
    
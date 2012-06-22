package org.auscope.portal.core.services;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.SOSMethodMaker;
import org.auscope.portal.core.services.responses.ows.OWSExceptionParser;
import org.auscope.portal.core.services.responses.sos.SOSResponse;

/**
 * An abstract base class containing common functionality for all Service classes
 * that intend to interact with a one or more Web Feature Services.
 *
 * @author Florence Tan
 */
public  class SOSService {

	protected HttpServiceCaller httpServiceCaller;
    protected SOSMethodMaker sosMethodMaker;

    /**
     * Creates a new instance of this class with the specified dependencies
     * @param httpServiceCaller Will be used for making requests
     * @param sosMethodMaker Will be used for generating WFS methods
     */
    public SOSService(HttpServiceCaller httpServiceCaller,
            SOSMethodMaker sosMethodMaker) {
        this.httpServiceCaller = httpServiceCaller;
        this.sosMethodMaker = sosMethodMaker;
    }
      
    
    /**
     * Utility method for choosing the correct SOS method to generate based on specified parameters
     * @param sosUrl [required] - the sensor observation service url
     * @param request - required, service type identifier (e.g. GetCapabilities or GetObservation) 
     * @param featureOfInterest- optional - pointer to a feature of interest for which observations are requested 
     * @param eventTime - optional - time period(s) (start and end) for which observations are requested 
     *                             - the time should conform to ISO format: YYYY-MM-DDTHH:mm:ss±HH. 
     *                             - Periods of time (start and end) are separated by "/". 
     *                               For example: 1990-01-01T00:00:00.000+08:00/2010-02-20T00:00:00.000+08:00
     * @param BBOX - optional - Bounding Box format : minlon,minlat,maxlon,maxlat(,srsURI). 
     * 												  The first four parameters are expected as decimal degrees. 
     * 												  SrsURI is optional and could take a value of "urn:ogc:def:crs:EPSG:6.5:4326"                             
     * @return HttpMethodBase object
     * @throws Exception
     */
    protected HttpMethodBase generateSOSRequest(String sosUrl, String request, String featureOfInterest, String eventTime, String BBOX) {    	
            return sosMethodMaker.makePostMethod(sosUrl, request, featureOfInterest, eventTime, BBOX);
    }

    
    /**
     * Makes a GetObservation request, transform the response using transformer and returns the 
     * lot bundled in a SOSTransformedResopnse
     * @param method a SOS GetObservation request
     * @param transformer A transformer to work with the resulting SOS response
     * @param styleSheetParams Properties to apply to the transformer
     * @return
     * @throws PortalServiceException
     */
    protected SOSResponse getSOSResponse(HttpMethodBase method) throws PortalServiceException {
        try {
            //Make the request and parse the response
            String responseString = httpServiceCaller.getMethodResponseAsString(method);
            OWSExceptionParser.checkForExceptionResponse(responseString);

            return new SOSResponse(responseString, method);
        } catch (Exception ex) {
            throw new PortalServiceException(method, ex);
        }
    }
}

package org.auscope.portal.core.services;

import java.util.Date;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.SOSMethodMaker;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.responses.ows.OWSExceptionParser;
import org.auscope.portal.core.services.responses.sos.SOSResponse;

/**
 * An abstract base class containing common functionality for all Service classes that intend to interact with a one or more Web Feature Services.
 *
 * @author Florence Tan
 */
public class SOSService {

    protected HttpServiceCaller httpServiceCaller;
    protected SOSMethodMaker sosMethodMaker;

    /**
     * Creates a new instance of this class with the specified dependencies
     * 
     * @param httpServiceCaller
     *            Will be used for making requests
     * @param sosMethodMaker
     *            Will be used for generating WFS methods
     */
    public SOSService(HttpServiceCaller httpServiceCaller,
            SOSMethodMaker sosMethodMaker) {
        this.httpServiceCaller = httpServiceCaller;
        this.sosMethodMaker = sosMethodMaker;
    }

    /**
     * Utility method for choosing the correct SOS method to generate based on specified parameters
     * 
     * @param sosUrl
     *            [required] - the sensor observation service url
     * @param request
     *            - required, service type identifier (e.g. GetCapabilities or GetObservation)
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
     * @return HttpMethodBase object
     */
    protected HttpRequestBase generateSOSRequest(String sosUrl, String request, String featureOfInterest,
            Date beginPosition, Date endPosition, FilterBoundingBox bbox) {
        return sosMethodMaker.makePostMethod(sosUrl, request, featureOfInterest, beginPosition, endPosition, bbox);
    }

    /**
     * Public method that receive parameters from Client, generate the SOSMethodMaker, trigger the "GetObservation" request, receive and return the response
     * 
     * @param sosUrl
     *            [required] - the sensor observation service url
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
     * @return HttpMethodBase object
     * @throws PortalServiceException
     */
    public SOSResponse getObservationsForFeature(String sosUrl, String featureOfInterest, Date beginPosition,
            Date endPosition, FilterBoundingBox bbox) throws PortalServiceException {
        //Generate SOSMethodMaker
        HttpRequestBase method = this.generateSOSRequest(sosUrl, "GetObservation", featureOfInterest, beginPosition,
                endPosition, bbox);

        try {
            //Make the request and parse the response
            String responseString = httpServiceCaller.getMethodResponseAsString(method);
            OWSExceptionParser.checkForExceptionResponse(responseString);

            return new SOSResponse(responseString, method);
        } catch (Exception ex) {
            throw new PortalServiceException(method, "Error while making SOS GetObservationForFeature request", ex);
        }

    }

}

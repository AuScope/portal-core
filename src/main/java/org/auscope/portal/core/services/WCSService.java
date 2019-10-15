package org.auscope.portal.core.services;

import java.awt.Dimension;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.WCSMethodMaker;
import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;
import org.auscope.portal.core.services.responses.ows.OWSExceptionParser;
import org.auscope.portal.core.services.responses.wcs.DescribeCoverageRecord;
import org.auscope.portal.core.services.responses.wcs.GetCapabilitiesRecord_1_0_0;
import org.auscope.portal.core.services.responses.wcs.Resolution;
import org.auscope.portal.core.services.responses.wcs.TimeConstraint;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Document;

/**
 * Service class for interacting with a Web Coverage Service
 * 
 * @author Josh Vote
 */
public class WCSService {
    @SuppressWarnings("unused")
    private final Log logger = LogFactory.getLog(getClass());

    private HttpServiceCaller serviceCaller;
    private WCSMethodMaker methodMaker;

    public WCSService(HttpServiceCaller serviceCaller, WCSMethodMaker methodMaker) {
        this.serviceCaller = serviceCaller;
        this.methodMaker = methodMaker;
    }

    /**
     * Makes a GetCoverage request, returns the response as a stream of data
     * 
     * @param serviceUrl
     *            The WCS endpoint to query
     * @param coverageName
     *            The coverage layername to request
     * @param inputCrs
     *            the coordinate reference system to query
     * @param downloadFormat
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
     * @param customParameters
     *            [Optional] a list of additional request parameters
     *
     *
     * @throws PortalServiceException
     */
    public InputStream getCoverage(String serviceUrl, String coverageName, String downloadFormat,
            Dimension outputSize, Resolution outputResolution, String outputCrs, String inputCrs,
            CSWGeographicBoundingBox bbox, TimeConstraint timeConstraint, Map<String, String> customParameters)
            throws PortalServiceException {

        HttpRequestBase method = null;

        try {
            method = methodMaker.getCoverageMethod(serviceUrl, coverageName, downloadFormat, outputCrs, outputSize,
                    outputResolution, inputCrs, bbox, timeConstraint, customParameters);
            return serviceCaller.getMethodResponseAsStream(method);
        } catch (Exception ex) {
            throw new PortalServiceException(method, "Error while making GetCoverage request", ex);
        }
    }
    
    /**
     * Makes a GetCapabilities request. Currently assumes a version 1.0.0 response.
     * 
     * @param 
     * 		serviceUrl the URL for the WCS GetCapabilities request
     * @return 
     * 		GetCapabilities response for WCS as a {@link GetCapabilitiesRecord_1_0_0}
     * @throws PortalServiceException
     */
    public GetCapabilitiesRecord_1_0_0 getWcsCapabilities(String serviceUrl) throws PortalServiceException {
    	HttpRequestBase method = null;
    	try {
    		method = methodMaker.getCapabilitiesMethod(serviceUrl);
    		try (InputStream response = serviceCaller.getMethodResponseAsStream(method)) {
    			return new GetCapabilitiesRecord_1_0_0(response);
            }
    	} catch(Exception ex) {
    		throw new PortalServiceException(method, "Error while making GetCapabilities request", ex);
    	}  finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }
    
    /**
     * Constructs a GetCoverage request for the specified WCS.
     * 
     * @param serviceUrl 
     * 		the WCS service URL
     * @param coverageName
     * 		the coverage name
     * @param format
     * 		format for the response
     * @param outputCrs
     * 		output CRS
     * @param outputSize
     * 		output size (width and height)
     * @param outputResolution
     * 		output resolution (x and Y)
     * @param inputCrs
     * 		input CRS
     * @param bbox
     * 		bounding box for the request
     * @param timeConstraint
     * 		time constraint
     * @param customParams
     * 		any remaining parameters
     * @return
     * 		Full URL for the GetCoverage request
     * @throws PortalServiceException
     */
    public String getCoverageRequestAsString(String serviceUrl, String coverageName,
            String format, String outputCrs, Dimension outputSize, Resolution outputResolution,
            String inputCrs, CSWGeographicBoundingBox bbox, TimeConstraint timeConstraint,
            Map<String, String> customParams) throws PortalServiceException {
		try {
			return this.methodMaker.getCoverageMethod(serviceUrl, coverageName, format, outputCrs,
					outputSize, outputResolution, inputCrs, bbox, timeConstraint, customParams).getURI().toString();
		} catch(URISyntaxException e) {
			throw new PortalServiceException("Unable to create GetCoverage request",e);
		}
	}

    /**
     * Makes a DescribeCoverage request, returns the response as an array of DescribeCoverageRecords
     * 
     * @param serviceUrl
     *            The WCS endpoint to query
     * @param coverageName
     *            The coverage name to describe
     * @throws URISyntaxException
     */
    public DescribeCoverageRecord[] describeCoverage(String serviceUrl, String coverageName)
            throws PortalServiceException {

        HttpRequestBase method = null;
        try {
            method = methodMaker.describeCoverageMethod(serviceUrl, coverageName);
            try (InputStream response = serviceCaller.getMethodResponseAsStream(method)) {

                Document responseDoc = DOMUtil.buildDomFromStream(response);
                OWSExceptionParser.checkForExceptionResponse(responseDoc);

                return DescribeCoverageRecord.parseRecords(responseDoc);
            }
        } catch (Exception ex) {
            throw new PortalServiceException(method, "Error while making DescribeCoverage request", ex);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }

}

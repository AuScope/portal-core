package org.auscope.portal.core.services.admin;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.OgcServiceProviderType;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.services.methodmakers.CSWMethodMakerGetDataRecords;
import org.auscope.portal.core.services.methodmakers.CSWMethodMakerGetDataRecords.ResultType;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.methodmakers.WMSMethodMaker;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.methodmakers.filter.IFilter;
import org.auscope.portal.core.services.methodmakers.filter.SimpleBBoxFilter;
import org.auscope.portal.core.services.responses.csw.CSWGetRecordResponse;
import org.auscope.portal.core.services.responses.ows.OWSException;
import org.auscope.portal.core.services.responses.ows.OWSExceptionParser;
import org.auscope.portal.core.util.DOMUtil;
import org.auscope.portal.core.util.FileIOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;

/**
 * Service class providing access into some portal low level functionality purely for the purposes of getting diagnostic information
 * 
 * @author Josh Vote
 *
 */
public class AdminService {
    /** For testing basic requests */
    protected HttpServiceCaller serviceCaller;

    /**
     * Creates a new AdminService
     * 
     * @param serviceCaller
     *            For testing basic requests
     * @param cswServiceList
     *            For accessing the various CSW's
     * @param portalProperties
     *            for checking config options
     */
    @Autowired
    public AdminService(HttpServiceCaller serviceCaller) {
        super();
        this.serviceCaller = serviceCaller;
    }

    /**
     * Tests external connectivity by attempting to access the specified URLs
     * 
     * @param urlsToTest
     *            The URLs tp test
     * @return
     */
    public AdminDiagnosticResponse externalConnectivity(URL[] urlsToTest) {
        AdminDiagnosticResponse response = new AdminDiagnosticResponse();
        for (URL url : urlsToTest) {
            String protocol = url.getProtocol().toLowerCase();
            String urlString = url.toString();

            try {
                HttpGet method = new HttpGet(urlString);
                serviceCaller.getMethodResponseAsString(method); // we dont care about the response
                response.addDetail(String.format("Succesfully connected to %1$s via '%2$s'.", urlString, protocol));
            } catch (Exception ex) {
                // We treat HTTP errors as critical, non http as warnings (such as https)
                if (protocol.equals("http")) {
                    response.addError(String.format("Unable to connect to %1$s via http. The error was %2$s",
                            urlString, ex));
                } else {
                    response.addWarning(String.format("Unable to connect to %1$s via '%2$s'. The error was %3$s",
                            urlString, protocol, ex));
                }
            }
        }

        return response;
    }

    /**
     * Tests connectivity to a set of CSW's - also tests some basic CSW requests
     * 
     * @param serviceItems
     *            The services to test
     * @return
     */
    public AdminDiagnosticResponse cswConnectivity(List<CSWServiceItem> serviceItems) {
        AdminDiagnosticResponse response = new AdminDiagnosticResponse();
        final int numRecordsToRequest = 1;

        // Iterate our configured registries performing a simple CSW request to ensure they are 'available'
        for (CSWServiceItem item : serviceItems) {

            CSWMethodMakerGetDataRecords methodMaker = new CSWMethodMakerGetDataRecords();
            HttpRequestBase method = methodMaker.makeMethod(item.getServiceUrl(), null, ResultType.Results,
                    numRecordsToRequest, item.getServerType());

            try (InputStream responseStream = serviceCaller.getMethodResponseAsStream(method)) {
                Document responseDoc = DOMUtil.buildDomFromStream(responseStream);
                OWSExceptionParser.checkForExceptionResponse(responseDoc);

                CSWGetRecordResponse responseRecs = new CSWGetRecordResponse(item, responseDoc);
                if (numRecordsToRequest != responseRecs.getRecords().size()) {
                    response.addWarning(String.format(
                            "Expecting a response with %1$s records. Got %2$s records instead.", numRecordsToRequest,
                            responseRecs.getRecords().size()));
                } else {
                    response.addDetail(String.format(
                            "Succesfully requested %1$s record(s) from '%2$s'. There are %3$s records available.",
                            numRecordsToRequest, item.getServiceUrl(), responseRecs.getRecordsMatched()));
                }
            } catch (Exception ex) {
                response.addError(String.format("Unable to parse a CSW record response from '%1$s': %2$s",
                        item.getServiceUrl(), ex));
            } 
        }
        return response;
    }

    /**
     * Utility interface for usage with httpMethodValidator
     */
    private interface ResponseValidator {
        /**
         * Implementors should validate the response and append any findings to diagnosticResponse
         * 
         * @param response
         *            The actual datastream
         * @param callingMethod
         *            Used to make the request
         * @param endpoint
         *            Used to generate callingMethod
         * @param diagnosticResponse
         *            Will receive info/warnings/errors
         * @return
         */
        public void validateResponse(InputStream response, HttpRequestBase callingMethod, EndpointAndSelector endpoint,
                AdminDiagnosticResponse diagnosticResponse);
    }

    /**
     * Utility function for making a HTTP request to each and every method in methods and validating the response with validator.
     *
     * The must be a 1-1 mapping between methods and endpoints
     *
     * Endpoints that return an outright HTTP error will be blacklisted and skipped for future queries.
     *
     * @param methods
     *            The HTTP methods to validate. Must be same length as endpoints.
     * @param endpoints
     *            The endpoints used to generate methods. Must be same length as methods.
     * @param validator
     *            Will be called on each successful method response
     * @return
     */
    private AdminDiagnosticResponse httpMethodValidator(List<HttpRequestBase> methods,
            List<EndpointAndSelector> endpoints, ResponseValidator validator) {
        AdminDiagnosticResponse diagnosticResponse = new AdminDiagnosticResponse();
        List<String> blacklistedUrls = new ArrayList<>();

        for (int i = 0; i < methods.size(); i++) {
            HttpRequestBase method = methods.get(i);
            EndpointAndSelector endpoint = endpoints.get(i);

            // Check for blacklist
            if (blacklistedUrls.contains(endpoint.getEndpoint())) {
                diagnosticResponse
                        .addError(String
                                .format("Endpoint '%1$s' with selector '%2$s' will be skipped because the endpoint previously returned a HTTP error.",
                                        endpoint.getEndpoint(), endpoint.getSelector()));
                continue;
            }

            // Make our request - offload testing to the validator and if there is a
            // HTTP error, skip that endpoint for the rest of this test
            InputStream response = null;
            try {
                response = serviceCaller.getMethodResponseAsStream(method);
                validator.validateResponse(response, method, endpoint, diagnosticResponse);
            } catch (Exception ex) {
                blacklistedUrls.add(endpoint.getEndpoint());
                diagnosticResponse.addError(String.format(
                        "Endpoint '%1$s' cannot be reached when using selector '%2$s' - %3$s", endpoint.getEndpoint(),
                        endpoint.getSelector(), ex));
            } finally {
                FileIOUtil.closeQuietly(response);
            }
        }

        return diagnosticResponse;
    }

    /**
     * Iterates through wfsEndpoints making 2 GetFeature requests to both. The first will be requesting the first feature, the second will do the same but
     * constrained to bbox
     * 
     * @param wfsEndpoints
     *            A list of wfs endpoint/wfs type name combinations
     * @param bboxJson
     *            A bounding box to constrain some requests.  In raw JSON format.
     * @return
     * @throws URISyntaxException
     */
    public AdminDiagnosticResponse wfsConnectivity(List<EndpointAndSelector> wfsEndpoints, String bboxJson)
            throws URISyntaxException {
        List<HttpRequestBase> methodsToTest = new ArrayList<>();
        List<EndpointAndSelector> endpointsToTest = new ArrayList<>();

        // Iterate our service urls, making a basic WFS GetFeature and more complicated BBOX request to each
        WFSGetFeatureMethodMaker methodMaker = new WFSGetFeatureMethodMaker();
        for (EndpointAndSelector endpoint : wfsEndpoints) {
            String serviceUrl = endpoint.getEndpoint();
            String typeName = endpoint.getSelector();

            // Make a request for a single feature, no filter
            methodsToTest.add(methodMaker.makeGetMethod(serviceUrl, typeName, 1, null));
            endpointsToTest.add(endpoint);

            OgcServiceProviderType ogcServiceProviderType = OgcServiceProviderType.parseUrl(serviceUrl);
            FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson, ogcServiceProviderType);

            // Next make a slightly more complex BBOX filter (to ensure we have a spatial field set at the WFS)
            IFilter filter = new SimpleBBoxFilter();
            String filterString = filter.getFilterStringBoundingBox(bbox);
            methodsToTest.add(methodMaker.makePostMethod(serviceUrl, typeName, filterString, 1, bbox.getBboxSrs(),
                    WFSGetFeatureMethodMaker.ResultType.Results));
            endpointsToTest.add(endpoint);
        }

        AdminDiagnosticResponse diagnosticResponse = httpMethodValidator(methodsToTest, endpointsToTest,
                new ResponseValidator() {
                    @Override
                    public void validateResponse(InputStream response, HttpRequestBase callingMethod,
                            EndpointAndSelector endpoint, AdminDiagnosticResponse diagnosticResp) {
                        try {
                            Document doc = DOMUtil.buildDomFromStream(response);
                            OWSExceptionParser.checkForExceptionResponse(doc);
                        } catch (OWSException ex) {
                            diagnosticResp.addError(String.format(
                                    "WFS '%1$s' returned an OWS exception for type '%2$s' - %3$s",
                                    endpoint.getEndpoint(), endpoint.getSelector(), ex));
                        } catch (Exception ex) {
                            diagnosticResp.addError(String.format(
                                    "WFS '%1$s' is returning invalid XML for type '%2$s' - %3$s",
                                    endpoint.getEndpoint(), endpoint.getSelector(), ex));
                        }
                    }
                });

        // Some nice statistical info
        diagnosticResponse.addDetail(String.format("Testing %1$s different endpoint/type name combinations",
                wfsEndpoints.size()));

        return diagnosticResponse;
    }
    
    /**
     * Iterates through wfsEndpoints making 2 GetFeature requests to both. The first will be requesting the first feature, the second will do the same but
     * constrained to bbox.  
     * 
     * This version is for backwards compatibility.
     * 
     * @param wfsEndpoints
     *            A list of wfs endpoint/wfs type name combinations
     * @param bboxJson
     *            A bounding box to constrain some requests.
     * @return
     * @throws URISyntaxException
     */
    public AdminDiagnosticResponse wfsConnectivity(List<EndpointAndSelector> wfsEndpoints, FilterBoundingBox bbox)
            throws URISyntaxException {
        String json = bbox.toJsonNewsFormat(OgcServiceProviderType.GeoServer);
        return wfsConnectivity(wfsEndpoints, json);
    }

    /**
     * Iterates through wmsEndpoints making a simple GetMap and GetFeatureInfo request based on the specified bbox
     * 
     * @param wmsEndpoints
     *            The WMS endpoints to test
     * @param bboxJson
     *            The bounding box to test the map query, in raw JSON format.
     * @return
     * @throws URISyntaxException
     */
    public AdminDiagnosticResponse wmsConnectivity(List<EndpointAndSelector> wmsEndpoints, String bboxJson)
            throws URISyntaxException {
        List<HttpRequestBase> methodsToTest = new ArrayList<>();
        List<EndpointAndSelector> endpointsToTest = new ArrayList<>();

        // Set our constants
        final String imageMimeType = "image/png";
        final String infoMimeType = "text/html";
        final int width = 128;
        final int height = width;

        // Build our request methods
        for (EndpointAndSelector endpoint : wmsEndpoints) {

            WMSMethodMaker methodMaker = new WMSMethodMaker(serviceCaller);
            String serviceUrl = endpoint.getEndpoint();
            OgcServiceProviderType ogcServiceProviderType = OgcServiceProviderType.parseUrl(serviceUrl);
            FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson, ogcServiceProviderType);
            final double north = bbox.getUpperCornerPoints()[1];
            final double south = bbox.getLowerCornerPoints()[1];
            final double east = bbox.getUpperCornerPoints()[0];
            final double west = bbox.getLowerCornerPoints()[0];

            // Make a GetMap request
            methodsToTest.add(methodMaker.getMapMethod(endpoint.getEndpoint(), endpoint.getSelector(), imageMimeType,
                    bbox.getBboxSrs(), west, south, east, north, width, height, null, null));
            endpointsToTest.add(endpoint);

            // Make a GetFeatureInfo request
            methodsToTest.add(methodMaker.getFeatureInfo(endpoint.getEndpoint(), infoMimeType, endpoint.getSelector(),
                    bbox.getBboxSrs(), west, south, east, north, width, height, west, north, 0, 0, null, null, "0"));
            endpointsToTest.add(endpoint);
        }

        // Validate the methods by comparing the response Content-Type header
        AdminDiagnosticResponse diagnosticResponse = httpMethodValidator(methodsToTest, endpointsToTest,
                new ResponseValidator() {
                    @Override
                    public void validateResponse(InputStream response, HttpRequestBase callingMethod,
                            EndpointAndSelector endpoint, AdminDiagnosticResponse diagnosticResp) {
                        // We need the URL
                        String uriString = "";
                        try {
                            uriString = callingMethod.getURI().toString();
                        } catch (Exception ex) {
                            diagnosticResp.addError(String.format(
                                    "Error decoding request URI for '%1$s' - '%2$s' - %3$s", endpoint.getEndpoint(),
                                    endpoint.getSelector(), ex));
                            return;
                        }

                        // What sort of request is this?
                        String requestType = "unknown";
                        String expectedContentType = "";
                        if (uriString.contains("request=GetMap")) {
                            requestType = "GetMap";
                            expectedContentType = imageMimeType;
                        } else if (uriString.contains("request=GetFeatureInfo")) {
                            requestType = "GetFeatureInfo";
                            expectedContentType = infoMimeType;
                        }

                        // Validate content type
                        try {
                            Header contentType = callingMethod.getFirstHeader("Content-Type");
                            if (contentType == null) {
                                diagnosticResp.addWarning(String
                                        .format("Backend received a '%1$s' response with no 'Content-Type' header for layer '%2$s' from '%3$s'",
                                                requestType, endpoint.getSelector(), endpoint.getEndpoint()));
                            } else if (!contentType.getValue().contains(expectedContentType)) {
                                diagnosticResp.addError(String
                                        .format("Backend received a '%1$s' response with a bad 'Content-Type' header for layer '%2$s' from '%3$s'. Was expecting '%4$s' but got '%5$s' instead",
                                                requestType, endpoint.getSelector(), endpoint.getEndpoint(),
                                                expectedContentType, contentType.getValue()));
                            }
                        } catch (Exception ex) {
                            diagnosticResp.addError(String.format(
                                    "Error decoding '%1$s' response ContentType from '%2$s' - '%3$s' - %4$s",
                                    requestType, endpoint.getEndpoint(), endpoint.getSelector(), ex));
                        }
                    }
                });

        diagnosticResponse.addDetail(String.format(
                "Requesting map/feature info from %1$s different endpoint/layer combinations", wmsEndpoints.size()));

        return diagnosticResponse;
    }
    
    /**
     * Iterates through wmsEndpoints making a simple GetMap and GetFeatureInfo request based on the specified bbox
     * 
     * @param wmsEndpoints
     *            The WMS endpoints to test
     * @param bbox
     *            The bounding box to test the map query.
     * @return
     * @throws URISyntaxException
     */
    public AdminDiagnosticResponse wmsConnectivity(List<EndpointAndSelector> wmsEndpoints, FilterBoundingBox bbox)
            throws URISyntaxException {
        return wmsConnectivity(wmsEndpoints, bbox.toJsonNewsFormat(OgcServiceProviderType.GeoServer));
    }
}

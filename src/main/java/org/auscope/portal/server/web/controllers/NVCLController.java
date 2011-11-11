package org.auscope.portal.server.web.controllers;

import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.domain.nvcldataservice.GetDatasetCollectionResponse;
import org.auscope.portal.server.domain.nvcldataservice.GetLogCollectionResponse;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.ErrorMessages;
import org.auscope.portal.server.web.service.BoreholeService;
import org.auscope.portal.server.web.service.CSWCacheService;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.web.service.NVCLDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for handling requests for the NVCL boreholes
 * @author Josh Vote
 *
 */
@Controller
public class NVCLController extends AbstractBaseWFSToKMLController {

    private BoreholeService boreholeService;
    private NVCLDataService dataService;
    private CSWCacheService cswService;

    @Autowired
    public NVCLController(GmlToKml gmlToKml,
                            BoreholeService boreholeService,
                            HttpServiceCaller httpServiceCaller,
                            CSWCacheService cswService,
                            NVCLDataService dataService) {

        this.boreholeService = boreholeService;
        this.gmlToKml = gmlToKml;
        this.httpServiceCaller = httpServiceCaller;
        this.cswService = cswService;
        this.dataService = dataService;
    }



    /**
     * Handles the borehole filter queries.
     *
     * @param serviceUrl the url of the service to query
     * @param mineName   the name of the mine to query for
     * @param request    the HTTP client request
     * @return a WFS response converted into KML
     * @throws Exception
     */
    @RequestMapping("/doBoreholeFilter.do")
    public ModelAndView doBoreholeFilter(@RequestParam("serviceUrl") String serviceUrl,
                                      @RequestParam(required=false, value="boreholeName", defaultValue="")     String boreholeName,
                                      @RequestParam(required=false, value="custodian", defaultValue="")        String custodian,
                                      @RequestParam(required=false, value="dateOfDrilling", defaultValue="")   String dateOfDrilling,
                                      @RequestParam(required=false, value="maxFeatures", defaultValue="0") int maxFeatures,
                                      @RequestParam(required=false, value="bbox") String bboxJson,
                                      @RequestParam(required=false, value="onlyHylogger") String onlyHyloggerString,
                                      HttpServletRequest request) throws Exception {

        boolean onlyHylogger = false;
        if (onlyHyloggerString != null && onlyHyloggerString.length() > 0) {
            if (onlyHyloggerString.equals("on")) {
                onlyHylogger = true;
            } else {
                onlyHylogger = Boolean.parseBoolean(onlyHyloggerString);
            }
        }

        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);
        return doBoreholeFilter(serviceUrl,boreholeName, custodian, dateOfDrilling, maxFeatures,bbox, onlyHylogger, request);
    }

    /**
     * Handles the borehole filter queries.
     *
     * @param serviceUrl the url of the service to query
     * @param mineName   the name of the mine to query for
     * @param request    the HTTP client request
     * @return a WFS response converted into KML
     * @throws Exception
     */
    public ModelAndView doBoreholeFilter(String serviceUrl,String boreholeName,String custodian,
                                        String dateOfDrilling,int maxFeatures,FilterBoundingBox bbox,
                                        boolean onlyHylogger,HttpServletRequest request) throws Exception {
        List<String> hyloggerBoreholeIDs = null;
        if (onlyHylogger) {
            try {
                hyloggerBoreholeIDs = this.boreholeService.discoverHyloggerBoreholeIDs(this.cswService);
            } catch (Exception e) {
                log.warn("Error requesting list of hylogger borehole ID's", e);
                return makeModelAndViewFailure("Failure when identifying which boreholes have Hylogger data.", null);
            }

            if (hyloggerBoreholeIDs.size() == 0) {
                log.warn("No hylogger boreholes exist (or the services are missing)");
                return makeModelAndViewFailure("Unable to identify any boreholes with Hylogger data.", null);
            }
        }

        HttpMethodBase method = null;
        try {
            method = this.boreholeService.getAllBoreholes(serviceUrl, boreholeName, custodian, dateOfDrilling, maxFeatures, bbox, hyloggerBoreholeIDs);
            String gmlBlob = this.httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());

            String kmlBlob = convertToKml(gmlBlob, request, serviceUrl);

            //log.debug(kmlBlob);
            // This failure test should be more robust,
            // it should try to extract an error message
            if (kmlBlob == null || kmlBlob.length() == 0) {
                log.error(String.format("Transform failed serviceUrl='%1$s' gmlBlob='%2$s'", serviceUrl, gmlBlob));
                return makeModelAndViewFailure(ErrorMessages.OPERATION_FAILED, method);
            } else {
                return makeModelAndViewKML(kmlBlob, gmlBlob, method);
            }
        } catch (Exception e) {
            return this.generateExceptionResponse(e, serviceUrl, method);
        }
    }

    /**
     * Gets the list of datasets for given borehole from the specified NVCL dataservice url.
     * @param serviceUrl The URL of an NVCL Data service
     * @param holeIdentifier The unique ID of a borehole
     * @return
     */
    @RequestMapping("getNVCLDatasets.do")
    public ModelAndView getNVCLDatasets(@RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("holeIdentifier") String holeIdentifier) {
        List<GetDatasetCollectionResponse> responseObjs = null;
        try {
            responseObjs = dataService.getDatasetCollection(serviceUrl, holeIdentifier);

            return generateJSONResponseMAV(true, responseObjs, "");
        } catch (Exception ex) {
            log.warn("Unable to request dataset collection", ex);
            return generateJSONResponseMAV(false);
        }
    }

    /**
     * Gets the list of logs for given NVCL dataset from the specified NVCL dataservice url.
     * @param serviceUrl The URL of an NVCL Data service
     * @param datasetId The unique ID of a dataset
     * @return
     */
    @RequestMapping("getNVCLLogs.do")
    public ModelAndView getNVCLLogs(@RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("datasetId") String datasetId) {
        List<GetLogCollectionResponse> responseObjs = null;
        try {
            responseObjs = dataService.getLogCollection(serviceUrl, datasetId);

            return generateJSONResponseMAV(true, responseObjs, "");
        } catch (Exception ex) {
            log.warn("Unable to request log collection", ex);
            return generateJSONResponseMAV(false);
        }
    }

    /**
     * A proxy for handling GET request.
     *
     * @param serviceUrl the url of the service to query
     * @throws Exception
     */
    @RequestMapping("HttpGetXmlProxy.do")
    public void HttpGetXmlProxy(@RequestParam("serviceUrl") String serviceUrl,
            HttpServletResponse response) throws Exception {
        // set the content type for xml files
        response.setContentType("text/xml");
        // create the output stream
        OutputStream out = (response.getOutputStream());
        String xml = httpServiceCaller.callHttpUrlGET(new URL(serviceUrl));
        out.write(xml.getBytes());
    }
}

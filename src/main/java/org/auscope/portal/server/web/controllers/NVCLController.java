package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.domain.nvcldataservice.AbstractStreamResponse;
import org.auscope.portal.server.domain.nvcldataservice.GetDatasetCollectionResponse;
import org.auscope.portal.server.domain.nvcldataservice.GetLogCollectionResponse;
import org.auscope.portal.server.domain.nvcldataservice.MosaicResponse;
import org.auscope.portal.server.domain.nvcldataservice.PlotScalarResponse;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.ErrorMessages;
import org.auscope.portal.server.web.NVCLDataServiceMethodMaker;
import org.auscope.portal.server.web.KnownLayerWFS;
import org.auscope.portal.server.web.NVCLDataServiceMethodMaker.PlotScalarGraphType;
import org.auscope.portal.server.web.service.BoreholeService;
import org.auscope.portal.server.web.service.CSWCacheService;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.web.service.NVCLDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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
    private ArrayList<KnownLayerWFS> boreholes;

    @Autowired
    public NVCLController(GmlToKml gmlToKml,
                            BoreholeService boreholeService,
                            HttpServiceCaller httpServiceCaller,
                            CSWCacheService cswService,
                            NVCLDataService dataService,
                            @Qualifier("knownTypeBorehole") ArrayList<KnownLayerWFS> boreholes) {

        this.boreholeService = boreholeService;
        this.gmlToKml = gmlToKml;
        this.httpServiceCaller = httpServiceCaller;
        this.cswService = cswService;
        this.dataService = dataService;
        this.boreholes=boreholes;
    }

    /**
     * Handles the request for all the service urls for boreholes linked to a title
     *
     * @param title the title as specified on the UI for the borehole
     * @return ModelAndView a standard reply of all the service urls
     * @throws MalformedURLException
     */
    @RequestMapping("/getBoreholeServices.do")
    public ModelAndView getBoreholeServices(@RequestParam("title") String title) throws MalformedURLException{
        //String title="National Virtual Core Library";

        String[] urls=null;
        List<ModelMap> response = new ArrayList<ModelMap>();
        for(KnownLayerWFS borehole:boreholes){
            if(borehole.getTitle().equals(title) && (urls=borehole.getServiceEndpoints())!=null){
                for (String url : urls) {
                    ModelMap modelMap = new ModelMap();
                    modelMap.put("url", (new URL(url)).getHost());
                    response.add(modelMap);
                }

            }
        }
        return generateJSONResponseMAV(true, response, "");
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
                                      @RequestParam(required=false, value="serviceFilter", defaultValue="")String serviceFilter,
                                      HttpServletRequest request) throws Exception {

        if(!serviceFilter.equals("") && !(new URL(serviceUrl).getHost()).equalsIgnoreCase(serviceFilter)){
            return this.generateJSONResponseMAV(false);
        }
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
            @RequestParam("datasetId") String datasetId,
            @RequestParam("mosaicService") Boolean forMosaicService) {
        List<GetLogCollectionResponse> responseObjs = null;
        try {
            responseObjs = dataService.getLogCollection(serviceUrl, datasetId, forMosaicService);

            return generateJSONResponseMAV(true, responseObjs, "");
        } catch (Exception ex) {
            log.warn("Unable to request log collection", ex);
            return generateJSONResponseMAV(false);
        }
    }

    /**
     * Utility function for piping the contents of serviceResponse to servletResponse
     */
    private void writeStreamResponse(HttpServletResponse servletResponse, AbstractStreamResponse serviceResponse) throws IOException {
        InputStream serviceInputStream = serviceResponse.getResponse();
        OutputStream responseOutput = null;

        //write our response
        try {
            servletResponse.setContentType(serviceResponse.getContentType());
            responseOutput = servletResponse.getOutputStream();

            writeInputToOutputStream(serviceInputStream, responseOutput, 1024 * 1024);
        } finally {
            if (serviceInputStream != null) {
                serviceInputStream.close();
            }
            if (responseOutput != null) {
                responseOutput.close();
            }
        }
    }

    /**
     * Proxies a NVCL Mosaic request for mosaic imagery. Writes directly to the HttpServletResponse
     * @param serviceUrl The URL of an NVCL Data service
     * @param logId The unique ID of a log (from a getNVCLLogs.do request)
     * @return
     */
    @RequestMapping("getNVCLMosaic.do")
    public void getNVCLMosaic(@RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("logId") String logId,
            @RequestParam(required=false,value="width") Integer width,
            @RequestParam(required=false,value="startSampleNo") Integer startSampleNo,
            @RequestParam(required=false,value="endSampleNo") Integer endSampleNo,
            HttpServletResponse response) throws Exception {

        //Make our request
        MosaicResponse serviceResponse = null;
        try {
            serviceResponse = dataService.getMosaic(serviceUrl, logId, width, startSampleNo, endSampleNo);
        } catch (Exception ex) {
            log.warn("Unable to request log collection", ex);
            response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        writeStreamResponse(response, serviceResponse);
    }

    /**
     * Proxies a NVCL Plot Scalar request. Writes directly to the HttpServletResponse
     * @param serviceUrl The URL of an NVCL Data service
     * @param logId The unique ID of a log (from a getNVCLLogs.do request)
     * @return
     */
    @RequestMapping("getNVCLPlotScalar.do")
    public void getNVCLPlotScalar(@RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("logId") String logId,
            @RequestParam(required=false,value="startDepth") Integer startDepth,
            @RequestParam(required=false,value="endDepth") Integer endDepth,
            @RequestParam(required=false,value="width") Integer width,
            @RequestParam(required=false,value="height") Integer height,
            @RequestParam(required=false,value="samplingInterval") Double samplingInterval,
            @RequestParam(required=false,value="graphType") Integer graphTypeInt,
            HttpServletResponse response) throws Exception {

        //Parse our graph type
        NVCLDataServiceMethodMaker.PlotScalarGraphType graphType = null;
        if (graphTypeInt != null) {
            switch(graphTypeInt) {
            case 1:
                graphType = PlotScalarGraphType.StackedBarChart;
                break;
            case 2:
                graphType = PlotScalarGraphType.ScatteredChart;
                break;
            case 3:
                graphType = PlotScalarGraphType.LineChart;
                break;
            default:
                log.warn("Inalid graphType: " + graphTypeInt);
                response.sendError(HttpStatus.SC_BAD_REQUEST);
                return;
            }
        }

        //Make our request
        PlotScalarResponse serviceResponse = null;
        try {
            serviceResponse = dataService.getPlotScalar(serviceUrl, logId, startDepth, endDepth, width, height, samplingInterval, graphType);
        } catch (Exception ex) {
            log.warn("Unable to request log collection", ex);
            response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        writeStreamResponse(response, serviceResponse);
    }
}

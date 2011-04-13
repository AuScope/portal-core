package org.auscope.portal.server.web.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.csw.CSWOnlineResource;
import org.auscope.portal.csw.CSWRecord;
import org.auscope.portal.csw.CSWOnlineResource.OnlineResourceType;
import org.auscope.portal.nvcl.NVCLNamespaceContext;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.util.Util;
import org.auscope.portal.server.web.ErrorMessages;
import org.auscope.portal.server.web.IWFSGetFeatureMethodMaker;
import org.auscope.portal.server.web.service.BoreholeService;
import org.auscope.portal.server.web.service.CSWService;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Controller for handling requests for the NVCL boreholes
 * @author Josh Vote
 *
 */
@Controller
public class NVCLController extends BaseWFSToKMLController {
    
    private BoreholeService boreholeService;
    private CSWService cswService;
    
    @Autowired
    public NVCLController(GmlToKml gmlToKml,
                            BoreholeService boreholeService,
                            HttpServiceCaller httpServiceCaller,
                            CSWService cswService) {

        this.boreholeService = boreholeService;
        this.gmlToKml = gmlToKml;
        this.httpServiceCaller = httpServiceCaller;
        this.cswService = cswService;
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
    public ModelAndView doBoreholeFilter( @RequestParam("serviceUrl") String serviceUrl,
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
            return this.handleExceptionResponse(e, serviceUrl, method);
        }
    }
}

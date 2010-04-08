package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.auscope.portal.mineraloccurrence.Mine;
import org.auscope.portal.mineraloccurrence.MineralOccurrencesResponseHandler;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.ErrorMessages;
import org.auscope.portal.server.web.service.MineralOccurrenceService;
import org.auscope.portal.server.web.service.NvclService;
import org.auscope.portal.server.web.view.JSONModelAndView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import org.xml.sax.SAXException;

/**
 * Controller that handles all Earth Resource related requests
 * <p>
 * It handles the following WFS features:
 * <ul>
 * <li>Mine</li>
 * <li>Mineral Occurrence</li>
 * <li>Mininig Activity</li>
 * </ul>
 * </p>
 * 
 * @author Jarek Sanders
 * @version $Id$
 */
@Controller
public class EarthResourcesFilterController {

    // -------------------------------------------------------------- Constants
    
    /** Log object for this class. */
    protected final Log log = LogFactory.getLog(getClass());
    
    /** Query all mines command */
    private static String ALL_MINES = "";
    
    // ----------------------------------------------------- Instance variables
    
    private MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler;
    private MineralOccurrenceService mineralOccurrenceService;
    private NvclService nvclService;
    private GmlToKml gmlToKml;

    // ----------------------------------------------------------- Constructors
    
    @Autowired
    public EarthResourcesFilterController
        ( MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler,
          MineralOccurrenceService mineralOccurrenceService,
          NvclService nvclService,
          GmlToKml gmlToKml ) {
        
        this.mineralOccurrencesResponseHandler = mineralOccurrencesResponseHandler;
        this.mineralOccurrenceService = mineralOccurrenceService;
        this.nvclService = nvclService;
        this.gmlToKml = gmlToKml;
    }

    // ------------------------------------------- Property Setters and Getters   
    
    /**
     * Handles the Earth Resource Mine filter queries.
     *
     * @param serviceUrl the url of the service to query
     * @param mineName   the name of the mine to query for
     * @param request    the HTTP client request
     * @return a WFS response converted into KML
     */
    @RequestMapping("/doMineFilter.do")
    public ModelAndView doMineFilter(
            @RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("mineName") String mineName,
            HttpServletRequest request) {

        try {
            String gmlBlob;

            if (mineName.equals(ALL_MINES)) {//get all mines 

                gmlBlob = this.mineralOccurrenceService.getAllMinesGML(serviceUrl);
            }
            else
                gmlBlob = this.mineralOccurrenceService.getMineWithSpecifiedNameGML(serviceUrl, mineName);

            String kmlBlob =  convertToKml(gmlBlob, request, serviceUrl);
            //log.debug(kmlBlob);
            
            //This failure test should be made a little bit more robust
            //And should probably try to extract an error message
            if (kmlBlob == null || kmlBlob.length() == 0) {
            	return makeModelAndViewFailure(ErrorMessages.OPERATION_FAILED);
            } else {
            	return makeModelAndViewKML(kmlBlob, gmlBlob);
            }
        } catch (Exception e) {
            return this.handleExceptionResponse(e);
        }
    }

    
    /**
     * Handles the Earth Resource MineralOccerrence filter queries.
     *
     * @param serviceUrl
     * @param commodityName
     * @param measureType
     * @param minOreAmount
     * @param minOreAmountUOM
     * @param minCommodityAmount
     * @param minCommodityAmountUOM
     * @param cutOffGrade
     * @param cutOffGradeUOM
     * @param request                the HTTP client request
     * 
     * @return a WFS response converted into KML
     */
    @RequestMapping("/doMineralOccurrenceFilter.do")
    public ModelAndView doMineralOccurrenceFilter(
            @RequestParam("serviceUrl")            String serviceUrl,
            @RequestParam("commodityName")         String commodityName,
            @RequestParam("measureType")           String measureType,
            @RequestParam("minOreAmount")          String minOreAmount,
            @RequestParam("minOreAmountUOM")       String minOreAmountUOM,
            @RequestParam("minCommodityAmount")    String minCommodityAmount,
            @RequestParam("minCommodityAmountUOM") String minCommodityAmountUOM,
            @RequestParam("cutOffGrade")           String cutOffGrade,
            @RequestParam("cutOffGradeUOM")        String cutOffGradeUOM,
            HttpServletRequest request) {
        try {

            //get the mineral occurrences
            String mineralOccurrenceResponse 
                = this.mineralOccurrenceService.getMineralOccurrenceGML
                                                      ( serviceUrl,
                                                        commodityName,
                                                        measureType,
                                                        minOreAmount,
                                                        minOreAmountUOM,
                                                        minCommodityAmount,
                                                        minCommodityAmountUOM,
                                                        cutOffGrade,
                                                        cutOffGradeUOM);

            log.debug("mineralOccurrenceResponse");

            // If there are 0 features then send NO_RESULTS message to the user
            if (mineralOccurrencesResponseHandler.getNumberOfFeatures(mineralOccurrenceResponse) == 0)
                return makeModelAndViewFailure(ErrorMessages.NO_RESULTS);

            //if everything is good then return the KML
            return makeModelAndViewKML(convertToKml(mineralOccurrenceResponse, request, serviceUrl), mineralOccurrenceResponse);

        } catch (Exception e) {
            return this.handleExceptionResponse(e);
        }
    }
    
    
    /**
     * Handles Mining Activity filter queries
     * Returns WFS response converted into KML.
     *
     * @param serviceUrl
     * @param mineName
     * @param startDate 
     * @param endDate 
     * @param oreProcessed 
     * @param producedMaterial 
     * @param cutOffGrade 
     * @param production 
     * @param request
     * @return the KML response
     */    
    @RequestMapping("/doMiningActivityFilter.do")
    public ModelAndView doMiningActivityFilter(
            @RequestParam("serviceUrl")       String serviceUrl,
            @RequestParam("mineName")         String mineName,
            @RequestParam("startDate")        String startDate,
            @RequestParam("endDate")          String endDate,
            @RequestParam("oreProcessed")     String oreProcessed,
            @RequestParam("producedMaterial") String producedMaterial,
            @RequestParam("cutOffGrade")      String cutOffGrade,
            @RequestParam("production")       String production,
            HttpServletRequest request) throws IOException, SAXException, XPathExpressionException, ParserConfigurationException 
    {
        try {
            List<Mine> mines;   

            if (mineName.equals(ALL_MINES))
                mines = this.mineralOccurrenceService.getAllMines(serviceUrl);
            else
                mines = this.mineralOccurrenceService.getMineWithSpecifiedName(serviceUrl, mineName);

            //if there are 0 features then send nice message to the user
            if (mines.size() == 0)
                return makeModelAndViewFailure(ErrorMessages.NO_RESULTS);

            // Get the mining activities
            String miningActivityResponse = this.mineralOccurrenceService.getMiningActivityGML(serviceUrl, mines, startDate, endDate, oreProcessed, producedMaterial, cutOffGrade, production);

            // If there are 0 features then send NO_RESULTS message to the user
            if (mineralOccurrencesResponseHandler.getNumberOfFeatures(miningActivityResponse) == 0)
                return makeModelAndViewFailure(ErrorMessages.NO_RESULTS);

            return makeModelAndViewKML(convertToKml(miningActivityResponse, request, serviceUrl), miningActivityResponse);

        } catch (Exception e) {
            return this.handleExceptionResponse(e);
        }
    }

    
    /**
     * Handles the NVCL filter queries.
     *
     * @param serviceUrl the url of the service to query
     * @param mineName   the name of the mine to query for
     * @param request    the HTTP client request
     * @return a WFS response converted into KML
     */
    @RequestMapping("/doNvclFilter.do")
    public ModelAndView doNvclFilter( @RequestParam("serviceUrl") String serviceUrl,
                                      HttpServletRequest request) {
        try {
            String gmlBlob = this.nvclService.getAllBoreholes(serviceUrl);

            String kmlBlob = convertToKml(gmlBlob, request, serviceUrl);
            //log.debug(kmlBlob);
                
            // This failure test should be more robust,
            // it should try to extract an error message
            if (kmlBlob == null || kmlBlob.length() == 0) {
                return makeModelAndViewFailure(ErrorMessages.OPERATION_FAILED);
            } else {
                return makeModelAndViewKML(kmlBlob, gmlBlob);
            }
        } catch (Exception e) {
            return this.handleExceptionResponse(e);
        }
    }    
    
    /**
     * Exception resolver that maps exceptions to views presented to the user
     * @param exception
     * @return ModelAndView object with error message 
     */
    public ModelAndView handleExceptionResponse(Exception e) {

        log.error(e);

        // Service down or host down
        if(e instanceof ConnectException || e instanceof UnknownHostException) {
            return this.makeModelAndViewFailure(ErrorMessages.UNKNOWN_HOST_OR_FAILED_CONNECTION);
        }

        // Timouts
        if(e instanceof ConnectTimeoutException) {
            return this.makeModelAndViewFailure(ErrorMessages.OPERATION_TIMOUT);
        }

        // An error we don't specifically handle or expect
        return makeModelAndViewFailure(ErrorMessages.FILTER_FAILED);
    } 
   

    // ------------------------------------------------------ Protected Methods

    // ------------------------------------------------------ Private Methods
    /**
     * Create a new ModelAndView given a kml block and serialised xml document.
     * @param kmlBlob
     * @param gmlBlob
     * @return ModelAndView JSON response object
     */
    private ModelAndView makeModelAndViewKML(final String kmlBlob, final String gmlBlob) {
        final Map data = new HashMap() {{
            put("kml", kmlBlob);
            put("gml", gmlBlob);
        }};

        ModelMap model = new ModelMap() {{
            put("success", true);
            put("data", data);
        }};

        return new JSONModelAndView(model);
    }
    
    
    /**
     * Create a failure response
     *
     * @param message
     * @return
     */
    private ModelAndView makeModelAndViewFailure(final String message) {
        ModelMap model = new ModelMap() {{
            put("success", false);
            put("msg", message);
        }};

        return new JSONModelAndView(model);
    }
    
    
    /**
     * Assemble a call to convert GeoSciML into kml format 
     * @param geoXML
     * @param httpRequest
     * @param serviceUrl
     */
    private String convertToKml(String geoXML, HttpServletRequest httpRequest, String serviceUrl) {
        InputStream inXSLT = httpRequest.getSession().getServletContext().getResourceAsStream("/WEB-INF/xsl/kml.xsl");
        return gmlToKml.convert(geoXML, inXSLT, serviceUrl);
    }
}

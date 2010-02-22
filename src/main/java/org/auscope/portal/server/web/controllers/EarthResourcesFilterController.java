package org.auscope.portal.server.web.controllers;

import java.io.IOException;
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
import org.auscope.portal.server.web.view.JSONModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.xml.sax.SAXException;

//import net.sf.json.JSONArray;

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
    //private static String ALL_MINES = "All Mines..";        // To DO: Remove
    
    /** Query all mines command */
    private static String ALL_MINES = "";
    
    // ----------------------------------------------------- Instance variables
    
    private MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler;
    private MineralOccurrenceService mineralOccurrenceService;
    private GmlToKml gmlToKml;

    // ----------------------------------------------------------- Constructors
    
    @Autowired
    public EarthResourcesFilterController
        ( MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler,
          MineralOccurrenceService mineralOccurrenceService,
          GmlToKml gmlToKml ) {
        
        this.mineralOccurrencesResponseHandler = mineralOccurrencesResponseHandler;
        this.mineralOccurrenceService = mineralOccurrenceService;
        this.gmlToKml = gmlToKml;
    }

    // ------------------------------------------- Property Setters and Getters   
    
    /**
     * Handles the Earth Resource Mine name filter query.
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
System.out.println("* * * 2a");
        try {
            String gmlBlob;

            if (mineName.equals(ALL_MINES)) {//get all mines 
                System.out.println("* ALL_MINES * 2a");
                gmlBlob = this.mineralOccurrenceService.getAllMinesGML(serviceUrl);
            }
            else
                gmlBlob = this.mineralOccurrenceService.getMineWithSpecifiedNameGML(serviceUrl, mineName);

            String kmlBlob =  gmlToKml.convert(gmlBlob, request);
System.out.println(kmlBlob);
            
            return makeModelAndViewKML(kmlBlob, gmlBlob);
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
     * @param request
     * @return the KML response
     */    
    @RequestMapping("/doMiningActivityFilter.do")
    public ModelAndView doMiningActivityFilter(
            @RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("mineName") String mineName,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("oreProcessed") String oreProcessed,
            @RequestParam("producedMaterial") String producedMaterial,
            @RequestParam("cutOffGrade") String cutOffGrade,
            @RequestParam("production") String production,
            HttpServletRequest request) throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {

        try {
            List<Mine> mines;   

            if (mineName.equals(ALL_MINES))
                mines = this.mineralOccurrenceService.getAllMines(serviceUrl);
            else
                mines = this.mineralOccurrenceService.getMineWithSpecifiedName(serviceUrl, mineName);

            //if there are 0 features then send nice message to the user
            if (mines.size() == 0)
                return makeModelAndViewFailure(ErrorMessages.NO_RESULTS);

            //get the mining activities
            String miningActivityResponse = this.mineralOccurrenceService.getMiningActivityGML(serviceUrl, mines, startDate, endDate, oreProcessed, producedMaterial, cutOffGrade, production);

            //if there are 0 features then send updateCSWRecords nice message to the user
            if (mineralOccurrencesResponseHandler.getNumberOfFeatures(miningActivityResponse) == 0)
                return makeModelAndViewFailure(ErrorMessages.NO_RESULTS);

            //return makeModelAndViewKML(convertToKML(mineResponse, miningActivityResponse));
            return makeModelAndViewKML(gmlToKml.convert(miningActivityResponse, request), miningActivityResponse);

        } catch (Exception e) {
            return this.handleExceptionResponse(e);
        }
    }

    
    /**
     * Depending on the type of exception we get, present the user with a nice meaningful message
     * @param e
     * @return
     */
    public ModelAndView handleExceptionResponse(Exception e) {
        //log the error
        log.error(e);

        //service down or host down
        if(e instanceof ConnectException || e instanceof UnknownHostException) {
            return this.makeModelAndViewFailure(ErrorMessages.UNKNOWN_HOST_OR_FAILED_CONNECTION);
        }

        //timouts
        if(e instanceof ConnectTimeoutException) {
            return this.makeModelAndViewFailure(ErrorMessages.OPERATION_TIMOUT);
        }

        //an error we don't specifically handle of expect
        return makeModelAndViewFailure(ErrorMessages.FILTER_FAILED);
    } 
   

    // ------------------------------------------------------ Protected Methods

    // ------------------------------------------------------ Private Methods
    /**
     * Insert a kml block into a successful JSON response
     * @param kmlBlob
     * @param gmlBlob
     * @return
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
   
}

package org.auscope.portal.server.web.controllers;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.view.JSONModelAndView;
import org.auscope.portal.mineraloccurrence.*;
import org.auscope.portal.server.web.ErrorMessages;
import org.auscope.portal.server.web.service.MineralOccurrenceService;
import org.xml.sax.SAXException;
import org.apache.log4j.Logger;
import org.apache.commons.httpclient.ConnectTimeoutException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;
import java.net.UnknownHostException;
import java.net.ConnectException;

import net.sf.json.JSONArray;

/**
 * User: Mathew Wyatt
 * Date: 20/03/2009
 * Time: 2:26:21 PM
 */

@Controller
public class MineralOccurrencesFilterController {

    private Logger logger = Logger.getLogger(getClass());
    private static String ALL_MINES = "All Mines..";

    private MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler;
    private MineralOccurrenceService mineralOccurrenceService;
    private GmlToKml gmlToKml;

    /**
     * Initialise fields
     *
     * @param mineralOccurrencesResponseHandler
     *                      needed to interperate a MineralOccurrence GML response
     */
    @Autowired
    public MineralOccurrencesFilterController(MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler,
                                              MineralOccurrenceService mineralOccurrenceService,
                                              GmlToKml gmlToKml) {
        this.mineralOccurrencesResponseHandler = mineralOccurrencesResponseHandler;
        this.mineralOccurrenceService = mineralOccurrenceService;
        this.gmlToKml = gmlToKml;
    }

    /**
     * Wildcard search on a collection of services for mine names. Aggregate the responses, then pass them
     * back on the response. The portal will use this to populate a combo box with the filtered mines names.
     *
     * @param serviceUrls
     * @param searchText
     * @return
     */
    /*@RequestMapping("/searchMines.do")
    public ModelAndView searchMines(@RequestParam("serviceUrls") String[] serviceUrls,
                                    @RequestParam("searchText") String searchText,
                                    ModelMap model) {
           
    }*/

    /**
     * Gets all of the mine names from a given service. It then builds a JSON response as follows:
     * <p/>
     * {"success":true,
     * "data":[
     * {"mineDisplayName":"Balh1"},
     * {"mineDisplayName":"Blah2"}
     * ]
     * }
     *
     * @param serviceUrl
     * @param model
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws XPathExpressionException
     * @throws ParserConfigurationException
     */
    @RequestMapping("/getMineNames.do")
    public ModelAndView getMineNames(@RequestParam("serviceUrl") String serviceUrl,
                                     ModelMap model) {

        try {
            //get all of the mines
            Collection<Mine> mines = this.mineralOccurrenceService.getAllMines(serviceUrl);

            //create updateCSWRecords single all mines element to add to the top of the list
            Map mineNameAll = new HashMap() {{
                put("mineDisplayName", ALL_MINES);
            }};

            //create an array to hold the list of mines
            JSONArray recordsArray = new JSONArray();
            recordsArray.add(mineNameAll);

            //iterate through the mine names and add them to the JSON response
            Iterator<Mine> it = mines.iterator();
            while (it.hasNext()) {
                Mine mine = it.next();
                Map<String, String> mineName = new HashMap<String, String>();
                mineName.put("mineDisplayName", mine.getMineNamePreffered());
                recordsArray.add(mineName);
            }

            model.put("data", recordsArray);
            model.put("success", true);

            return new JSONModelAndView(model);
        } catch (Exception e) {
            return this.handleExceptionResponse(e);
        }
    }



    @RequestMapping("/doMineralOccurrenceFilter.do")
    public ModelAndView doMineralOccurrenceFilter(
            @RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("commodityName") String commodityName,
            @RequestParam("commodityGroup") String commodityGroup,
            @RequestParam("measureType") String measureType,
            @RequestParam("minOreAmount") String minOreAmount,
            @RequestParam("minOreAmountUOM") String minOreAmountUOM,
            @RequestParam("minCommodityAmount") String minCommodityAmount,
            @RequestParam("minCommodityAmountUOM") String minCommodityAmountUOM,
            @RequestParam("cutOffGrade") String cutOffGrade,
            @RequestParam("cutOffGradeUOM") String cutOffGradeUOM,
            HttpServletRequest request) {
        try {

            //get the mineral occurrences
            String mineralOccurrenceResponse = this.mineralOccurrenceService.getMineralOccurrenceGML(serviceUrl,
                                                                                        commodityName,
                                                                                        commodityGroup,
                                                                                        measureType,
                                                                                        minOreAmount,
                                                                                        minOreAmountUOM,
                                                                                        minCommodityAmount,
                                                                                        minCommodityAmountUOM,
                                                                                        cutOffGrade,
                                                                                        cutOffGradeUOM);
            //if there are 0 features then send updateCSWRecords nice message to the user
            if (mineralOccurrencesResponseHandler.getNumberOfFeatures(mineralOccurrenceResponse) == 0)
                return makeModelAndViewFailure(ErrorMessages.NO_RESULTS);

            //if everything is good then return the KML
            return makeModelAndViewKML(gmlToKml.convert(mineralOccurrenceResponse, request), mineralOccurrenceResponse);

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
        logger.error(e);

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

package org.auscope.portal.server.web.controllers;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.view.JSONModelAndView;
import org.auscope.portal.server.web.mineraloccurrence.*;
import org.auscope.portal.server.web.HttpServiceCaller;
import org.auscope.portal.server.web.ErrorMessages;
import org.xml.sax.SAXException;
import org.apache.log4j.Logger;
import org.apache.commons.httpclient.HttpClient;

import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

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

    private HttpServiceCaller serviceCaller;
    private MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler;
    private MineralOccurrenceServiceClient mineralOccurrenceServiceClient;
    private GmlToKml gmlToKml;

    /**
     * Initialise fields
     */
    public MineralOccurrencesFilterController() {
        this.serviceCaller = new HttpServiceCaller(new HttpClient());
        this.mineralOccurrencesResponseHandler = new MineralOccurrencesResponseHandler();
        this.mineralOccurrenceServiceClient = new MineralOccurrenceServiceClient(new HttpServiceCaller(new HttpClient()), new MineralOccurrencesResponseHandler());
        this.gmlToKml = new GmlToKml();
    }

    /**
     * Initialise fields
     *
     * @param serviceCaller used to invoke a http service
     * @param mineralOccurrencesResponseHandler
     *                      needed to interperate a MineralOccurrence GML response
     */
    public MineralOccurrencesFilterController(HttpServiceCaller serviceCaller,
                                              MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler,
                                              MineralOccurrenceServiceClient mineralOccurrenceServiceClient,
                                              GmlToKml gmlToKml) {
        this.serviceCaller = serviceCaller;
        this.mineralOccurrencesResponseHandler = mineralOccurrencesResponseHandler;
        this.mineralOccurrenceServiceClient = mineralOccurrenceServiceClient;
        this.gmlToKml = gmlToKml;
    }

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
            Collection<Mine> mines = this.mineralOccurrenceServiceClient.getAllMines(serviceUrl);

            //create a single all mines element to add to the top of the list
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
            logger.error(e);

            //if there is an error then report a nice message to the user
            return this.makeModelAndViewFailure(ErrorMessages.OPERATION_FAILED);
        }
    }

    /**
     * Performs a filter query on a given service, then converts the GML response into KML and returns
     *
     * @param serviceUrl
     * @param mineName
     * @param request
     * @return
     */
    @RequestMapping("/doMineFilter.do")
    public ModelAndView doMineFilter(
            @RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("mineName") String mineName,
            HttpServletRequest request) {
        try {
            String kmlBlob;

            if (mineName.equals(ALL_MINES))//get all mines
                kmlBlob = gmlToKml.convert(this.mineralOccurrenceServiceClient.getAllMinesGML(serviceUrl), request);
            else
                kmlBlob = gmlToKml.convert(this.mineralOccurrenceServiceClient.getMineWithSpecifiedNameGML(serviceUrl, mineName), request);

            return makeModelAndViewKML(kmlBlob);
        } catch (Exception e) {
            logger.error(e);

            //send a nice message
            return this.makeModelAndViewFailure(ErrorMessages.FILTER_FAILED);
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
            String mineralOccurrenceResponse = this.mineralOccurrenceServiceClient.getMineralOccurrenceGML(serviceUrl,
                                                                                        commodityName,
                                                                                        commodityGroup,
                                                                                        measureType,
                                                                                        minOreAmount,
                                                                                        minOreAmountUOM,
                                                                                        minCommodityAmount,
                                                                                        minCommodityAmountUOM,
                                                                                        cutOffGrade,
                                                                                        cutOffGradeUOM);

            //if there are 0 features then send a nice message to the user
            if (mineralOccurrencesResponseHandler.getNumberOfFeatures(mineralOccurrenceResponse) == 0)
                return makeModelAndViewFailure(ErrorMessages.NO_RESULTS);

            //if everything is good then return the KML
            return makeModelAndViewKML(gmlToKml.convert(mineralOccurrenceResponse, request));

        } catch (Exception e) {
            logger.error(e);

            //if it failed then send a message to the user
            return makeModelAndViewFailure(ErrorMessages.FILTER_FAILED);
        }
    }

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
                mines = this.mineralOccurrenceServiceClient.getAllMines(serviceUrl);
            else
                mines = this.mineralOccurrenceServiceClient.getMineWithSpecifiedName(serviceUrl, mineName);

            //if there are 0 features then send a nice message to the user
            if (mines.size() == 0)
                return makeModelAndViewFailure(ErrorMessages.NO_RESULTS);

            //get the mining activities
            String miningActivityResponse = this.mineralOccurrenceServiceClient.getMiningActivityGML(serviceUrl, mines, startDate, endDate, oreProcessed, producedMaterial, cutOffGrade, production);

            //if there are 0 features then send a nice message to the user
            if (mineralOccurrencesResponseHandler.getNumberOfFeatures(miningActivityResponse) == 0)
                return makeModelAndViewFailure(ErrorMessages.NO_RESULTS);

            //return makeModelAndViewKML(convertToKML(mineResponse, miningActivityResponse));
            return makeModelAndViewKML(gmlToKml.convert(miningActivityResponse, request));

        } catch (Exception e) {
            logger.error(e);
            return makeModelAndViewFailure(ErrorMessages.FILTER_FAILED);
        }
    }

    /**
     * Insert a kml block into a successful JSON response
     * @param kmlBlob
     * @return
     */
    private ModelAndView makeModelAndViewKML(final String kmlBlob) {
        final Map data = new HashMap() {{
            put("kml", kmlBlob);
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

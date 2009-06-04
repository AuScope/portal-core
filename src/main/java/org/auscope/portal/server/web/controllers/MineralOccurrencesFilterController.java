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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Iterator;
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
        this.mineralOccurrenceServiceClient = new MineralOccurrenceServiceClient(new HttpServiceCaller(new HttpClient()), new MineralOccurrencesResponseHandler() );
        this.gmlToKml = new GmlToKml();
    }

    /**
     * Initialise fields
     * 
     * @param serviceCaller used to invoke a http service
     * @param mineralOccurrencesResponseHandler needed to interperate a MineralOccurrence GML response
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
     *
     *  {"success":true,
         "data":[
            {"mineDisplayName":"Balh1"},
            {"mineDisplayName":"Blah2"}
            ]
        }
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
            while( it.hasNext() )
            {
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

            if(mineName.equals(ALL_MINES))//get all mines
                kmlBlob = gmlToKml.convert(this.mineralOccurrenceServiceClient.getAllMinesGML(serviceUrl), request);
            else
                kmlBlob = gmlToKml.convert(this.mineralOccurrenceServiceClient.getMineWithSpecifiedNameGML(serviceUrl, mineName), request);

            return makeModelAndViewKML(kmlBlob);
        } catch(Exception e) {
            logger.error(e);
            
            //send a nice message
            return this.makeModelAndViewFailure(ErrorMessages.FILTER_FAILED);
        }
    }

    @RequestMapping("/doMineralOccurrenceFilter.do")
    public ModelAndView doMineralOccurrenceFilter(
            
            @RequestParam("serviceUrl")            String serviceUrl,
            @RequestParam("commodityName")         String commodityName,
            @RequestParam("commodityGroup")        String commodityGroup,
            @RequestParam("measureType")           String measureType,
            @RequestParam("minOreAmount")          String minOreAmount,
            @RequestParam("minOreAmountUOM")       String minOreAmountUOM,
            @RequestParam("minCommodityAmount")    String minCommodityAmount,
            @RequestParam("minCommodityAmountUOM") String minCommodityAmountUOM,
            @RequestParam("cutOffGrade")           String cutOffGrade,
            @RequestParam("cutOffGradeUOM")        String cutOffGradeUOM,
            
            HttpServletRequest request)
    {
        try
        {
            String commodityResponse = doCommodityQuery(serviceUrl,
                                                        commodityGroup,
                                                        commodityName);
            
            String mineralOccurrenceResponse = "";
            
            Collection<Commodity> commodities =
                mineralOccurrencesResponseHandler.getCommodities(commodityResponse);
            
            if( commodities.size() >=1 )
            {
                Collection<String> commodityURIs = new ArrayList<String>();
                Commodity[] commoditiesArr = commodities.toArray(new Commodity[commodities.size()]);
                
                for(int i=0; i<commoditiesArr.length; i++)
                    commodityURIs.add(commoditiesArr[i].getMineralOccurrenceURI());

                mineralOccurrenceResponse = doMineralOccurrenceQuery( serviceUrl,
                                                                      commodityURIs,
                                                                      measureType,
                                                                      minOreAmount,
                                                                      minOreAmountUOM,
                                                                      minCommodityAmount,
                                                                      minCommodityAmountUOM,
                                                                      cutOffGrade,
                                                                      cutOffGradeUOM);
                
                if( mineralOccurrencesResponseHandler.getNumberOfFeatures(mineralOccurrenceResponse).compareTo("0")==0 )
                    return makeModelAndViewFailure("No results matched your query.");
            } else {
                return makeModelAndViewFailure("No results matched your query.");
            }

            return makeModelAndViewKML(gmlToKml.convert(mineralOccurrenceResponse, request));

        } catch(Exception e) {
            logger.error(e);
            return makeModelAndViewFailure("An error occurred when performing this operation. Please try a different filter request.");
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
            //TODO: find a better place for this pre processing of strings!
            startDate = startDate.toUpperCase();
            endDate = endDate.toUpperCase();
            if(mineName.equals(ALL_MINES)) mineName = "";

            String mineResponse = doMineQuery(serviceUrl, mineName);
            String miningActivityResponse = "";

            Collection<Mine> mines = mineralOccurrencesResponseHandler.getMines(mineResponse);

            if( mines.size() >=1 ) {
                //iterate through and build up a string arrray of mine uris
                String[] mineURIs = new String[mines.size()];
                Mine[] minesArr = mines.toArray(new Mine[mines.size()]);
                for(int i=0; i<minesArr.length; i++)
                    mineURIs[i] = minesArr[i].getMineNameURI();


                miningActivityResponse = doMiningActivityQuery( serviceUrl,
                                                                mineURIs,
                                                                startDate,
                                                                endDate,
                                                                oreProcessed,
                                                                producedMaterial,
                                                                cutOffGrade,
                                                                production);
            } else {
                System.out.println("failed");
                return makeModelAndViewFailure("No results matched your query.");
            }

            //return makeModelAndViewKML(convertToKML(mineResponse, miningActivityResponse));
            System.out.println("OK");
            return makeModelAndViewKML(gmlToKml.convert(miningActivityResponse, request));

        } catch(Exception e) {
            System.out.println("failed");
            logger.error(e);
            return makeModelAndViewFailure("An error occurred when prforming this operation. Please try a different filter request.");
        }
    }

    private String doMineQuery(String serviceUrl, String mineName) throws Exception {
        //ogc filter builder for mo:Mine
        MineFilter mineFilter = null;

        //If there is no specified mine name, then that mean Get All Mines, thus we don't need a filter
        if(mineName != null || !mineName.equals(""))
            mineFilter = new MineFilter(mineName);

        //call the service. if the mineFilter is null then we just send an empty filter string 
        return serviceCaller.callGetMethod(serviceCaller.constructWFSGetFeatureMethod(serviceUrl, "mo:Mine", (mineFilter == null ? "" : mineFilter.getFilterString())));
    }

    private String doCommodityQuery(String serviceUrl, String commodityGroup, String commodityName) throws IOException {
        CommodityFilter commodityFilter = new CommodityFilter(commodityGroup, commodityName);

        return null; //TODO: fix
        //return serviceCaller.responseToString(serviceCaller.callHttpUrlPost(serviceUrl, commodityFilter.getFilterString()));
    }

    private String doMiningActivityQuery(String serviceUrl, String[] mineNameURIs, String startDate, String endDate, String oreProcessed, String producedMaterial, String cutOffGrade, String production) throws IOException {

        MiningActivityFilter miningActivityFilter = new MiningActivityFilter(mineNameURIs, startDate, endDate, oreProcessed, producedMaterial, cutOffGrade, production);

        return null; //TODO: fix
        //return serviceCaller.responseToString(serviceCaller.callHttpUrlPost(serviceUrl, miningActivityFilter.getFilterString()));
    }

    private String doMineralOccurrenceQuery(String serviceUrl,
                                            Collection<String> commodityURIs,
                                            String measureType,
                                            String minOreAmount,
                                            String minOreAmountUOM,
                                            String minCommodityAmount,
                                            String minCommodityAmountUOM,
                                            String cutOffGrade,
                                            String cutOffGradeUOM) throws IOException {

        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter(commodityURIs,
                                        measureType,
                                        minOreAmount,
                                        minOreAmountUOM,
                                        minCommodityAmount,
                                        minCommodityAmountUOM,
                                        cutOffGrade,
                                        cutOffGradeUOM);
        return null; //TODO: fix
        /*return serviceCaller.responseToString(
                   serviceCaller.callHttpUrlPost(
                       serviceUrl,
                       mineralOccurrenceFilter.getFilterString()));*/
    }

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

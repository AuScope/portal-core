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
import javax.servlet.http.HttpServletResponse;
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
            return this.handleExceptionResponse(e);
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
                kmlBlob = gmlToKml.convert(this.mineralOccurrenceService.getAllMinesGML(serviceUrl), request);
            else
                kmlBlob = gmlToKml.convert(this.mineralOccurrenceService.getMineWithSpecifiedNameGML(serviceUrl, mineName), request);

            return makeModelAndViewKML(kmlBlob);
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
            //if there are 0 features then send a nice message to the user
            if (mineralOccurrencesResponseHandler.getNumberOfFeatures(mineralOccurrenceResponse) == 0)
                return makeModelAndViewFailure(ErrorMessages.NO_RESULTS);

            //if everything is good then return the KML
            return makeModelAndViewKML(gmlToKml.convert(mineralOccurrenceResponse, request));

        } catch (Exception e) {
            return this.handleExceptionResponse(e);
        }
    }

    @RequestMapping("/doMineralOccurrenceFilterKML.kml")
    public ModelAndView doMineralOccurrenceFilterKML(
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
            HttpServletRequest request,
            ModelMap model) {
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
            //if there are 0 features then send a nice message to the user
            //if (mineralOccurrencesResponseHandler.getNumberOfFeatures(mineralOccurrenceResponse) == 0)
            //    return makeModelAndViewFailure(ErrorMessages.NO_RESULTS);

            //if everything is good then return the KML
            //return makeModelAndViewKML(gmlToKml.convert(mineralOccurrenceResponse, request));
            //response.setContentType("text/xml");

            /*String kml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml xmlns:sa=\"http://www.opengis.net/sampling/1.0\" xmlns:geodesy=\"http://auscope.org.au/geodesy\" xmlns:mo=\"urn:cgi:xmlns:GGIC:MineralOccurrence:1.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xalan=\"http://xml.apache.org/xalan\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gsml=\"urn:cgi:xmlns:CGI:GeoSciML:2.0\"><Document><name>GML Links to KML</name><description>GeoSciML data converted to KML</description><Placemark><name>urn:cgi:feature:GSV:MineralOccurrence:361103</name><description>\n" +
                    "            &lt;/br&gt;&lt;table border=\"3\" cellspacing=\"1\" cellpadding=\"2\" height=\"100%\" bgcolor=\"#EAF0F8\"&gt;\n" +
                    "            &lt;tr&gt;&lt;td&gt;Id&lt;/td&gt;&lt;td&gt;&lt;a href=\"#\" onclick=\"var w=window.open('http://portal.auscope.org/UriUrlConverterClient/sampleUriUrlConverterProxy/?uri=urn:cgi:feature:GSV:MineralOccurrence:361103','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=800');w.focus();return false;\"&gt;urn:cgi:feature:GSV:MineralOccurrence:361103&lt;/a&gt;\n" +
                    "\n" +
                    "            &lt;/td&gt;&lt;/tr&gt;&lt;tr&gt;&lt;td&gt;Type&lt;/td&gt;&lt;td&gt;mineral deposit\n" +
                    "            &lt;/td&gt;&lt;/tr&gt;&lt;tr&gt;&lt;td&gt;Mineral Deposit Group&lt;/td&gt;&lt;td&gt;Unknown\n" +
                    "     &lt;/td&gt;&lt;/tr&gt;&lt;tr&gt;&lt;td&gt;Commodity Description&lt;/td&gt;&lt;td&gt;&lt;a href=\"#\" onclick=\"var w=window.open('http://portal.auscope.org/UriUrlConverterClient/sampleUriUrlConverterProxy/?uri=urn:cgi:feature:GSV:Commodity:361103:AG','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=800');w.focus();return false;\"&gt;urn:cgi:feature:GSV:Commodity:361103:AG&lt;/a&gt;\n" +
                    "\n" +
                    "   \n" +
                    "     &lt;/td&gt;&lt;/tr&gt;&lt;tr&gt;&lt;td&gt;Commodity Description&lt;/td&gt;&lt;td&gt;&lt;a href=\"#\" onclick=\"var w=window.open('http://portal.auscope.org/UriUrlConverterClient/sampleUriUrlConverterProxy/?uri=urn:cgi:feature:GSV:Commodity:361103:AU','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=800');w.focus();return false;\"&gt;urn:cgi:feature:GSV:Commodity:361103:AU&lt;/a&gt;\n" +
                    "   \n" +
                    "     &lt;/td&gt;&lt;/tr&gt;&lt;tr&gt;&lt;td&gt;Commodity Description&lt;/td&gt;&lt;td&gt;&lt;a href=\"#\" onclick=\"var w=window.open('http://portal.auscope.org/UriUrlConverterClient/sampleUriUrlConverterProxy/?uri=urn:cgi:feature:GSV:Commodity:361103:COB','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=800');w.focus();return false;\"&gt;urn:cgi:feature:GSV:Commodity:361103:COB&lt;/a&gt;\n" +
                    "\n" +
                    "   \n" +
                    "     &lt;/td&gt;&lt;/tr&gt;&lt;tr&gt;&lt;td&gt;Commodity Description&lt;/td&gt;&lt;td&gt;&lt;a href=\"#\" onclick=\"var w=window.open('http://portal.auscope.org/UriUrlConverterClient/sampleUriUrlConverterProxy/?uri=urn:cgi:feature:GSV:Commodity:361103:NK','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=800');w.focus();return false;\"&gt;urn:cgi:feature:GSV:Commodity:361103:NK&lt;/a&gt;\n" +
                    "   \n" +
                    "     &lt;/td&gt;&lt;/tr&gt;&lt;tr&gt;&lt;td&gt;Commodity Description&lt;/td&gt;&lt;td&gt;&lt;a href=\"#\" onclick=\"var w=window.open('http://portal.auscope.org/UriUrlConverterClient/sampleUriUrlConverterProxy/?uri=urn:cgi:feature:GSV:Commodity:361103:PB','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=800');w.focus();return false;\"&gt;urn:cgi:feature:GSV:Commodity:361103:PB&lt;/a&gt;\n" +
                    "\n" +
                    "   &lt;/td&gt;&lt;/tr&gt;&lt;/table&gt;\n" +
                    "         </description><Point><Style><IconStyle><Icon><href>http://maps.google.com/mapfiles/kml/paddle/red-blank.png</href></Icon></IconStyle></Style><coordinates>147.52659,-37.6338,0</coordinates></Point></Placemark></Document></kml>";
            */
            String kml = gmlToKml.convert(mineralOccurrenceResponse, request);

            kmls.put(kml.hashCode(), kml);

            model.put("key", kml.hashCode());
            System.out.println("YO DOWG");
            return new JSONModelAndView(model);

            //response.getWriter().write("{key:"+kml.hashCode()+"}");

            //response.getWriter().println(kml);

            //return null;
        } catch (Exception e) {
           return this.handleExceptionResponse(e);
        }
    }

    HashMap<Integer, String> kmls = new HashMap<Integer, String>();

    @RequestMapping("/getKML.kml")
    public void getKML(
            @RequestParam("key") String key,
            HttpServletResponse response) {
        try {
            response.setContentType("text/xml");
            String kml = kmls.get(key);
            //response.getWriter().write(gmlToKml.convert(mineralOccurrenceResponse, request));
            System.out.println(kml.getBytes().length);
            response.getWriter().println(kml);
            System.out.println("YO DOWG");
            //return null;
        } catch (Exception e) {
           // return this.handleExceptionResponse(e);
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
                mines = this.mineralOccurrenceService.getAllMines(serviceUrl);
            else
                mines = this.mineralOccurrenceService.getMineWithSpecifiedName(serviceUrl, mineName);

            //if there are 0 features then send a nice message to the user
            if (mines.size() == 0)
                return makeModelAndViewFailure(ErrorMessages.NO_RESULTS);

            //get the mining activities
            String miningActivityResponse = this.mineralOccurrenceService.getMiningActivityGML(serviceUrl, mines, startDate, endDate, oreProcessed, producedMaterial, cutOffGrade, production);

            //if there are 0 features then send a nice message to the user
            if (mineralOccurrencesResponseHandler.getNumberOfFeatures(miningActivityResponse) == 0)
                return makeModelAndViewFailure(ErrorMessages.NO_RESULTS);

            //return makeModelAndViewKML(convertToKML(mineResponse, miningActivityResponse));
            return makeModelAndViewKML(gmlToKml.convert(miningActivityResponse, request));

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

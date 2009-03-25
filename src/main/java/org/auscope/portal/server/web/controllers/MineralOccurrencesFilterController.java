package org.auscope.portal.server.web.controllers;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.auscope.portal.server.web.view.JSONView;
import org.auscope.portal.server.web.mineraloccurrence.MineFilter;
import org.auscope.portal.server.web.mineraloccurrence.Mine;
import org.auscope.portal.server.web.mineraloccurrence.MineralOccurrencesResponseHandler;
import org.auscope.portal.server.web.mineraloccurrence.MiningActivityFilter;
import org.auscope.portal.server.web.HttpServiceCaller;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.net.HttpURLConnection;

import net.sf.json.JSONArray;

/**
 * User: Mathew Wyatt
 * Date: 20/03/2009
 * Time: 2:26:21 PM
 */

@Controller
public class MineralOccurrencesFilterController {

    private HttpServiceCaller serviceCaller;

    public MineralOccurrencesFilterController() {
        this.serviceCaller = new HttpServiceCaller();
    }

    public MineralOccurrencesFilterController(HttpServiceCaller serviceCaller) {
        this.serviceCaller = serviceCaller;
    }

    @RequestMapping("/getMineNames.do")
    public ModelAndView populateFilterPanel(@RequestParam("serviceUrl") String serviceUrl,
                                            ModelMap model) {
        /*
        The following code will make json look like this
        {"success":true,
            "data":[
                {"mineDisplayName":"Balh1"},
                {"mineDisplayName":"Blah2"}
                ]
         }
         */

        //make mine names list
        Map mineNameAll = new HashMap();
        mineNameAll.put("mineDisplayName", "All Mines..");

        Map mineName0 = new HashMap();
        mineName0.put("mineDisplayName", "Good Hope");

        Map mineName1 = new HashMap();
        mineName1.put("mineDisplayName", "Sons of Freedom Reef");

        JSONArray recordsArray = new JSONArray();
        recordsArray.add(mineNameAll);        
        recordsArray.add(mineName0);
        recordsArray.add(mineName1);

        model.put("success", true);
        model.put("data", recordsArray);

        Map<String, HashMap<String, Comparable>> jsonViewModel = new HashMap<String, HashMap<String, Comparable>>();
        jsonViewModel.put("JSON_OBJECT", model);

        //TODO: query the given service url for all of the mines and get their names, then send that back as json

        return new ModelAndView(new JSONView(), jsonViewModel);
    }

    @RequestMapping("/doMineralOccurrenceFilter.do")
    public ModelAndView doMineralOccurrenceFilter(
            @RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("mineName") String mineName) throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {

        /*String mineResponse = doMineQuery(serviceUrl, mineName);
        Mine mine = (Mine)MineralOccurrencesResponseHandler.getMines(mineResponse).toArray()[0];

        String miningActivityResponse = doMiningActivityQuery(serviceUrl, mine.getMineNameURI());

        return makeModelAndViewSuccess(convertToKML(mineResponse, miningActivityResponse));*/

        return makeModelAndViewSuccess(convertToKML("", ""));
    }

    private String doMineQuery(String serviceUrl, String mineName) throws IOException {
        //URL service = new URL(URLEncoder.encode(serviceUrl + new MineFilter(mineName).getFilterString(), "UTF-8"));

        MineFilter mineFilter = new MineFilter(mineName);

        return serviceCaller.responseToString(serviceCaller.callHttpUrl(serviceUrl, mineFilter.getFilterString()));
    }

    private String doMiningActivityQuery(String serviceUrl, String mineNameURI) throws IOException {
        
        MiningActivityFilter miningActivityFilter = new MiningActivityFilter(mineNameURI, "", "", "", "", "", "");

        return serviceCaller.responseToString(serviceCaller.callHttpUrl(serviceUrl, miningActivityFilter.getFilterString()));
    }

    public String convertToKML(String mineResponse, String miningActivityResponse) {
        //TODO: JAREK OVERWRITE THIS ON YOUR MERGE
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:mo=\"urn:cgi:xmlns:GGIC:MineralOccurrence:1.0\" xmlns:geodesy=\"http://auscope.org.au/geodesy\" xmlns:sa=\"http://www.opengis.net/sampling/1.0\" xmlns:gsml=\"urn:cgi:xmlns:CGI:GeoSciML:2.0\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:wfs=\"http://www.opengis.net/wfs\"><Document><name>GML Links to KML</name><description>GML data converted to KML</description><Placemark><name>urn:cgi:feature:GSV:MiningFeatureOccurrence:362737</name><description>\n" +
                "            &lt;/br&gt;&lt;table border=\"1\" cellspacing=\"1\" width=\"100%\"&gt;\n" +
                "            &lt;tr&gt;&lt;td&gt;Description&lt;/td&gt;&lt;td&gt;\n" +
                "            &lt;/td&gt;&lt;/tr&gt;&lt;tr&gt;&lt;td&gt;Lat Lng (deg)&lt;/td&gt;&lt;td&gt;143.85227 -36.55083            \n" +
                "            &lt;/td&gt;&lt;/tr&gt;&lt;/table&gt;            \n" +
                "         </description><Point><Style><IconStyle><Icon><href>http://maps.google.com/mapfiles/kml/paddle/red-blank.png</href></Icon></IconStyle></Style><coordinates>143.85227,-36.55083,0</coordinates></Point></Placemark><Placemark><name>urn:cgi:feature:GSV:MiningFeatureOccurrence:362738</name><description>\n" +
                "\n" +
                "            &lt;/br&gt;&lt;table border=\"1\" cellspacing=\"1\" width=\"100%\"&gt;\n" +
                "            &lt;tr&gt;&lt;td&gt;Description&lt;/td&gt;&lt;td&gt;\n" +
                "            &lt;/td&gt;&lt;/tr&gt;&lt;tr&gt;&lt;td&gt;Lat Lng (deg)&lt;/td&gt;&lt;td&gt;143.71847 -36.80512            \n" +
                "            &lt;/td&gt;&lt;/tr&gt;&lt;/table&gt;            \n" +
                "         </description><Point><Style><IconStyle><Icon><href>http://maps.google.com/mapfiles/kml/paddle/red-blank.png</href></Icon></IconStyle></Style><coordinates>143.71847,-36.80512,0</coordinates></Point></Placemark></Document></kml>\n" +
                "";
    }

    private ModelAndView makeModelAndViewSuccess(String kmlBlob) {
        HashMap<String, Object> model = new HashMap<String, Object>();
        model.put("success", true);

        //JSONArray dataArray = new JSONArray();
        Map<String, Serializable> data = new HashMap<String, Serializable>();
        data.put("kml", kmlBlob);
        //dataArray.add(data);

        model.put("data", data);

        Map<String, HashMap<String, Object>> jsonViewModel = new HashMap<String, HashMap<String, Object>>();
        jsonViewModel.put("JSON_OBJECT", model);

        return new ModelAndView(new JSONView(), jsonViewModel);
    }

    private ModelAndView makeModelAndViewFail() {
        return null;
    }
}

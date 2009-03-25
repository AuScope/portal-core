package org.auscope.portal.server.web.controllers;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.util.XmlMerge;
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
import java.util.Collection;
import java.util.Iterator;
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
                                            ModelMap model) throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {
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

        String mineResponse = doMineQuery(serviceUrl, ""); // empty mine name to get all mines
        Collection<Mine> mines = MineralOccurrencesResponseHandler.getMines(mineResponse);

        JSONArray recordsArray = new JSONArray();
        recordsArray.add(mineNameAll);

        Iterator<Mine> it = mines.iterator();
        while( it.hasNext() )
        {
            Mine mine = it.next();
            Map<String, String> mineName = new HashMap<String, String>();
            mineName.put("mineDisplayName", mine.getMineNamePreffered());
            recordsArray.add(mineName);
        }

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
            @RequestParam("mineName") String mineName,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("oreProcessed") String oreProcessed,
            @RequestParam("producedMaterial") String producedMaterial,
            @RequestParam("cutOffGrade") String cutOffGrade,
            @RequestParam("production") String production,
            HttpServletRequest request) throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {

        String mineResponse = doMineQuery(serviceUrl, mineName);
        String miningActivityResponse = "";

        Collection<Mine> mines = MineralOccurrencesResponseHandler.getMines(mineResponse);

        if( mines.size() >=1 ) {
            Mine mine = (Mine)mines.toArray()[0];
            miningActivityResponse = doMiningActivityQuery(serviceUrl,
                                                                    mine.getMineNameURI(),
                                                                    startDate,
                                                                    endDate,
                                                                    oreProcessed,
                                                                    producedMaterial,
                                                                    cutOffGrade,
                                                                    production);
            System.out.println(miningActivityResponse);
        } else {
            makeModelAndViewFailure("No results matched your query.");
        }

        //return makeModelAndViewSuccess(convertToKML(mineResponse, miningActivityResponse));

        return makeModelAndViewSuccess(convertToKML(serviceCaller.stringToStream(mineResponse), serviceCaller.stringToStream(miningActivityResponse), request));
    }

    private String doMineQuery(String serviceUrl, String mineName) throws IOException {
        //URL service = new URL(URLEncoder.encode(serviceUrl + new MineFilter(mineName).getFilterString(), "UTF-8"));

        MineFilter mineFilter = new MineFilter(mineName);

        return serviceCaller.responseToString(serviceCaller.callHttpUrl(serviceUrl, mineFilter.getFilterString()));
    }

    private String doMiningActivityQuery(String serviceUrl, String mineNameURI, String startDate, String endDate, String oreProcessed, String producedMaterial, String cutOffGrade, String production) throws IOException {

        MiningActivityFilter miningActivityFilter = new MiningActivityFilter(mineNameURI, startDate, endDate, oreProcessed, producedMaterial, cutOffGrade, production);

        return serviceCaller.responseToString(serviceCaller.callHttpUrl(serviceUrl, miningActivityFilter.getFilterString()));
    }

    public String convertToKML(InputStream is1, InputStream is2, HttpServletRequest request) {
       String out = "";
       InputStream inXSLT = request.getSession().getServletContext().getResourceAsStream("/WEB-INF/xsl/kml.xsl");
       try {
          System.out.println("...convertToKML...");
          out = GmlToKml.convert(XmlMerge.merge(is1, is2), inXSLT);

       } catch (Exception e ) {
          System.out.println ("convertToKML error: ");
          e.printStackTrace();
       }
        return out;
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

    private ModelAndView makeModelAndViewFailure(String message) {
        HashMap<String, Object> model = new HashMap<String, Object>();
        model.put("success", true);
        model.put("msg", message);

        Map<String, HashMap<String, Object>> jsonViewModel = new HashMap<String, HashMap<String, Object>>();
        jsonViewModel.put("JSON_OBJECT", model);

        return new ModelAndView(new JSONView(), jsonViewModel);
    }
}

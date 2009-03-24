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

    @RequestMapping("/getMineNames.do")
    public ModelAndView populateFilterPanel(@RequestParam("serviceUrl") String serviceUrl,
                                            ModelMap model) {
        /*
        The following code will make json look like this
        {"success":true,"data":[{"mineDisplayName":"Blah"},{"mineDisplayName":"Blah2"}]}
         */
        //make mine names list
        Map mineName0 = new HashMap();
        mineName0.put("mineDisplayName", "Good Hope");

        Map mineName1 = new HashMap();
        mineName1.put("mineDisplayName", "Sons of Freedom Reef");

        JSONArray recordsArray = new JSONArray();
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

        String mineResponse = doMineQuery(serviceUrl, mineName);
        Mine mine = (Mine)MineralOccurrencesResponseHandler.getMines(mineResponse).toArray()[0];

        String miningActivityResponse = doMiningActivityQuery(serviceUrl, mine.getMineNameURI());

        return makeModelAndViewSuccess(convertToKML(mineResponse, miningActivityResponse));
    }

    private String doMineQuery(String serviceUrl, String mineName) throws IOException {
        //URL service = new URL(URLEncoder.encode(serviceUrl + new MineFilter(mineName).getFilterString(), "UTF-8"));

        MineFilter mineFilter = new MineFilter(mineName);

        //to make HTTP Post request with HttpURLConnection
        URL url = new URL(serviceUrl);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();

        conn.setRequestMethod("POST");
        conn.setAllowUserInteraction(false); // no user interact [like pop up]
        conn.setDoOutput(true); // want to send
        conn.setRequestProperty( "Content-type", "text/xml" );
        conn.setRequestProperty( "Content-length", Integer.toString(mineFilter.getFilterString().length()));
        OutputStream ost = conn.getOutputStream();
        PrintWriter pw = new PrintWriter(ost);
        pw.print(mineFilter.getFilterString()); // here we "send" our body!
        pw.flush();
        pw.close();

        StringBuffer stringBuffer = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while((line = reader.readLine()) != null) {
            stringBuffer.append(line);
        }

        return stringBuffer.toString();
    }

    private String doMiningActivityQuery(String serviceUrl, String mineNameURI) throws IOException {
        
        MiningActivityFilter mineFilter = new MiningActivityFilter(mineNameURI);

        //to make HTTP Post request with HttpURLConnection
        URL url = new URL(serviceUrl);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();

        conn.setRequestMethod("POST");
        conn.setAllowUserInteraction(false); // no user interact [like pop up]
        conn.setDoOutput(true); // want to send
        conn.setRequestProperty( "Content-type", "text/xml" );
        conn.setRequestProperty( "Content-length", Integer.toString(mineFilter.getFilterString().length()));
        OutputStream ost = conn.getOutputStream();
        PrintWriter pw = new PrintWriter(ost);
        pw.print(mineFilter.getFilterString()); // here we "send" our body!
        pw.flush();
        pw.close();

        StringBuffer stringBuffer = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while((line = reader.readLine()) != null) {
            stringBuffer.append(line);
        }

        return stringBuffer.toString();
    }

    public String convertToKML(String mineResponse, String miningActivityResponse) {
        return "";
    }

    private ModelAndView makeModelAndViewSuccess(String kmlBlob) {
        HashMap<String, Comparable> model = new HashMap<String, Comparable>();
        model.put("success", true);

        JSONArray dataArray = new JSONArray();
        Map<String, Serializable> data = new HashMap<String, Serializable>();
        data.put("kml", kmlBlob);
        dataArray.add(data);

        model.put("data", dataArray);

        Map<String, HashMap<String, Comparable>> jsonViewModel = new HashMap<String, HashMap<String, Comparable>>();
        jsonViewModel.put("JSON_OBJECT", model);

        return new ModelAndView(new JSONView(), jsonViewModel);
    }

    private ModelAndView makeModelAndViewFail() {
        return null;
    }
}

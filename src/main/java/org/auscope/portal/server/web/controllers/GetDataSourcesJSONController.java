package org.auscope.portal.server.web.controllers;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.AbstractView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.ows.Layer;
import org.geotools.data.wms.WebMapServer;
import org.geotools.ows.ServiceException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.net.URL;
import java.io.IOException;
import java.io.Serializable;

import net.sf.json.JSONArray;
import net.sf.json.JSONSerializer;

/**
 * User: Mathew Wyatt
 * Date: 12/01/2009
 * Time: 2:01:42 PM
 */
public class GetDataSourcesJSONController extends AbstractController {
    //logger
    protected final Log logger = LogFactory.getLog(getClass());

    //create some static members to identify each of the themes to be displayed in the portal
    public static final String BOREHOLE = "Borehole";
    public static final String HYPERSPECTRAL = "Hyperspectral";
    public static final String GEOCEHMISTRY = "Geochemistry";
    public static final String MINERAL_OCCURENCES = "Mineral Occurences";
    public static final String GNSS_GPS = "GNSS/GPS";
    public static final String SEISMIC_IMAGING = "Seismic Imaging";

    //some contants to identify themes and insitutions
    public static final String THEME = "THEME:";
    public static final String INSTITUTION = "INSTITUTION:";

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {

        //Ext js sends the tree node id on a request for its children
        String node = request.getParameter("node");

        //a JSON array to handle the structures created
        JSONArray jsonArray = new JSONArray();

        //if we are opening the root node then we want to send back all of the available themes
        if (node.equals("root"))
            jsonArray = getThemes();

        //hyperspectral is a special case because it is MAP data, and has to be categorised futher down into layers
        //differently to the feature services
        else if (node.equals(THEME+HYPERSPECTRAL))
            jsonArray = getSpectraInstitionalProviders();

        //if we have a theme tree node, then go find the institutions providing the data for it
        else if (node.startsWith(THEME))
            jsonArray = getInstitionalProviders(node.replace(THEME, ""));

        //this is an institution being expanded, must be for spectral data, so go get the layers
        else if(node.startsWith(INSTITUTION))
            jsonArray = getHyperspectralLayers(node.replace(INSTITUTION, ""));

        //send it back...
        return this.getJsonModelAndView(jsonArray);
    }

    /**
     * Builds the theme list which will be sent back and displayed to the ExtJS tree in the portal      
     * @return
     */
    public JSONArray getThemes() {
        JSONArray jsonArray = new JSONArray();

        Map<String, Serializable> hyperspectral = new HashMap<String, Serializable>();
        hyperspectral.put("id", THEME+HYPERSPECTRAL);
        hyperspectral.put("text", HYPERSPECTRAL);
        hyperspectral.put("checked", Boolean.FALSE);
        hyperspectral.put("leaf", Boolean.FALSE);
        jsonArray.add(hyperspectral);

        Map<String, Serializable> borholes = new HashMap<String, Serializable>();
        borholes.put("id", THEME+ BOREHOLE);
        borholes.put("text", BOREHOLE);
        borholes.put("checked", Boolean.FALSE);
        borholes.put("leaf", Boolean.FALSE);
        jsonArray.add(borholes);

        Map<String, Serializable> gnnsGPS = new HashMap<String, Serializable>();
        gnnsGPS.put("id", THEME+GNSS_GPS);
        gnnsGPS.put("text", GNSS_GPS);
        gnnsGPS.put("checked", Boolean.FALSE);
        gnnsGPS.put("leaf", Boolean.FALSE);
        //gnnsGPS.put("icon", "img/gnss/gps_stations_on.png");
        jsonArray.add(gnnsGPS);

        Map<String, Serializable> geochemistry = new HashMap<String, Serializable>();
        geochemistry.put("id", THEME+GEOCEHMISTRY);
        geochemistry.put("text", GEOCEHMISTRY);
        geochemistry.put("checked", Boolean.FALSE);
        geochemistry.put("leaf", Boolean.FALSE);
        jsonArray.add(geochemistry);

        Map<String, Serializable> mineralOccurrences = new HashMap<String, Serializable>();
        mineralOccurrences.put("id", THEME+MINERAL_OCCURENCES);
        mineralOccurrences.put("text", MINERAL_OCCURENCES);
        mineralOccurrences.put("checked", Boolean.FALSE);
        mineralOccurrences.put("leaf", Boolean.FALSE);
        jsonArray.add(mineralOccurrences);

        Map<String, Serializable> seismicImaging = new HashMap<String, Serializable>();
        seismicImaging.put("id", THEME+SEISMIC_IMAGING);
        seismicImaging.put("text", SEISMIC_IMAGING);
        seismicImaging.put("checked", Boolean.FALSE);
        seismicImaging.put("leaf", Boolean.FALSE);
        jsonArray.add(seismicImaging);

        //create a model and view and return it
        return jsonArray;
    }

    /**
     * Given a theme, provide a list of intitutions and also the WFS query to get the data
     * from this institution
     * @param theme
     * @return
     */
    private JSONArray getInstitionalProviders(String theme) {
        //TODO: geonetwork query
        //go off to geo network

        //do some magic

        //return some stuff - temp for now
        JSONArray jsonArray = new JSONArray();

        Map<String, Serializable> nvcl = new HashMap<String, Serializable>();
        nvcl.put("id", "nvcl");
        nvcl.put("text", "National Virtual Core Library");
        nvcl.put("checked", Boolean.FALSE);
        nvcl.put("leaf", Boolean.TRUE);
        nvcl.put("icon", "img/nvcl/borehole_on.png");
        nvcl.put("layerType", "wfs");
        nvcl.put("tileOverlay", "");
        nvcl.put("wfsUrl", "http://c3dmm2.ivec.org/geoserver/wms/kml_reflect?layers=topp:HyMap_A_Ferrous_and_MgOH");
        //nvcl.put("wfsUrl", "http://mapgadgets.googlepages.com/cta.kml");

        jsonArray.add(nvcl);

        return jsonArray;
    }

    /**
     * Returns a list of Institutions who are proving hyperpectral map data
     * @return
     */
    private JSONArray getSpectraInstitionalProviders() {
        //TODO: need a geonetowrk query here!

        JSONArray jsonArray = new JSONArray();

        Map<String, Serializable> coe = new HashMap<String, Serializable>();
        coe.put("id", INSTITUTION+"waCoe");
        coe.put("text", "WA Center of Excellence for 3D Mineral Mapping");
        coe.put("checked", Boolean.FALSE);
        coe.put("leaf", Boolean.FALSE);
        jsonArray.add(coe);

        return jsonArray;
    }

    /**
     * Given an institution, grab all of the hyperspectral layers from its WMS's
     * @param institution
     * @return
     */
    private JSONArray getHyperspectralLayers(String institution) {
        //TODO: call geonetwork and get the WMS urls based on the institution...

        String server = "http://c3dmm2.ivec.org/geoserver/gwc/service/wms?";
        String gmapsUrl = "http://c3dmm2.ivec.org/geoserver/gwc/service/gmaps?";

        WebMapServer wms = null;
        try {
            wms = new WebMapServer(new URL(server));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        WMSCapabilities capabilities = wms.getCapabilities();

        JSONArray jsonArray = new JSONArray();

        List<Layer> layers = capabilities.getLayerList();
        for (Layer layer : layers) {
            Map<String, Serializable> layerNode = new HashMap<String, Serializable>();
            layerNode.put("id", layer.getName());
            layerNode.put("text", layer.getName());
            layerNode.put("checked", Boolean.FALSE);
            layerNode.put("leaf", Boolean.TRUE);
            layerNode.put("layerType", "wms");
            layerNode.put("wmsUrl", gmapsUrl);
            layerNode.put("tileOverlay", "");

            jsonArray.add(layerNode);
        }

        return jsonArray;
    }

    /**
     * This method takes a JSONArray and builds a SpringMVC Model and View from it, so it can be returned in the
     * http response to the client.
     * 
     * @param jsonArray
     * @return
     */
    private ModelAndView getJsonModelAndView(JSONArray jsonArray) {
        Map<String, JSONArray> model = new HashMap<String, JSONArray>();
        model.put("JSON_OBJECT", jsonArray);

        return new ModelAndView(new JSONView(), model);
    }
}

/**
 * This class is a JSON spring MVC View class which takes a JSONArray and sends the actual json structure down the
 * wire on the httpResponse
 */
class JSONView extends AbstractView {

    public JSONView() {
        super();
        setContentType("application/json");
    }

    protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType(getContentType());
        response.getWriter().write(JSONSerializer.toJSON(model.get("JSON_OBJECT")).toString());
    }

}

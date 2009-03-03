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
import org.xml.sax.SAXException;
import org.auscope.portal.csw.CSWClient;
import org.auscope.portal.csw.CSWRecord;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.parsers.ParserConfigurationException;
import java.util.*;
import java.net.URL;
import java.io.*;

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

    public static final String HYPERSPECTRAL = "Hyperspectral";

    //public static final String XSLT_PROXY_URL = "http://auscope-portal-dev.arrc.csiro.au/xsltRestProxy?url=";
    public static final String XSLT_PROXY_URL = "/xsltRestProxy?url=";
    public static final String PROXY_URL = "/restproxy?";

    public static final String CSW_URL = "http://auscope-portal.arrc.csiro.au/geonetwork/srv/en/csw";

    //create some identifiers for each of the themes to be displayed in the portal
    public static final String[] THEMES = { "Borehole",
                                            "Geochemistry",
                                            "Mineral Occurences",
                                            "Global Navigation Satellite Systems",
                                            "Geodesy",
                                            "Seismic Imaging",
                                            "Geological Units"};

    //create a map to hold the CSW query contraints for each theme
    public static final Map<String, String> themeContraints = new HashMap<String, String>() {{
        put("Borehole", "<?xml+version=\"1.0\"+encoding=\"UTF-8\"?><Filter+xmlns=\"http://www.opengis.net/ogc\"+xmlns:gml=\"http://www.opengis.net/gml\"><And><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>WFS</Literal></PropertyIsEqualTo><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>gsml:Borehole</Literal></PropertyIsEqualTo></And></Filter>&constraintLanguage=FILTER&constraint_language_version=1.1.0");
        put("Global Navigation Satellite Systems", "<?xml+version=\"1.0\"+encoding=\"UTF-8\"?><Filter+xmlns=\"http://www.opengis.net/ogc\"+xmlns:gml=\"http://www.opengis.net/gml\"><And><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>WFS</Literal></PropertyIsEqualTo><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>GPS</Literal></PropertyIsEqualTo><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>GNSS</Literal></PropertyIsEqualTo></And></Filter>&constraintLanguage=FILTER&constraint_language_version=1.1.0");
        put("Geodesy", "<?xml+version=\"1.0\"+encoding=\"UTF-8\"?><Filter+xmlns=\"http://www.opengis.net/ogc\"+xmlns:gml=\"http://www.opengis.net/gml\"><And><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>WFS</Literal></PropertyIsEqualTo><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>GPS</Literal></PropertyIsEqualTo><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>Geodesy</Literal></PropertyIsEqualTo></And></Filter>&constraintLanguage=FILTER&constraint_language_version=1.1.0");
        put("Geological Units", "<?xml+version=\"1.0\"+encoding=\"UTF-8\"?><Filter+xmlns=\"http://www.opengis.net/ogc\"+xmlns:gml=\"http://www.opengis.net/gml\"><And><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>WFS</Literal></PropertyIsEqualTo><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>gsml:GeologicUnit</Literal></PropertyIsEqualTo></And></Filter>&constraintLanguage=FILTER&constraint_language_version=1.1.0");
    }};

    //create a map to hold the get features query stuff
    public static final Map<String, String> wfsQueryParams = new HashMap<String, String>() {{
        put("Borehole", "request=GetFeature%26typeName=gsml:Borehole");
        put("Global Navigation Satellite Systems", "request=GetFeature%26typeName=sa:SamplingPoint");
        put("Geodesy", "request=GetFeature%26typeName=geodesy:stations");
        put("Geological Units", "request=GetFeature%26typeName=gsml:GeologicUnit");
    }};

    //create a map to hold the get features query stuff
    public static final Map<String, String> icons = new HashMap<String, String>() {{
        put("Borehole", "http://maps.google.com/mapfiles/kml/paddle/blu-blank.png");
        put("Global Navigation Satellite Systems", "http://maps.google.com/mapfiles/kml/paddle/grn-blank.png");
        put("Geodesy", "http://maps.google.com/mapfiles/kml/paddle/wht-blank.png");
        put("Geological Units", "http://maps.google.com/mapfiles/kml/paddle/red-blank.png");
    }};

    //create a map to hold the get features query stuff
    public static final Map<String, String> featureTypes = new HashMap<String, String>() {{
        put("Borehole", "gsml:Borehole");
        put("Global Navigation Satellite Systems", "http://maps.google.com/mapfiles/kml/paddle/grn-blank.png");
        put("Geodesy", "geodesy:stations");
    }};

    //some contants which will be used as prefixes in the tree nde name to identify themes and insitutions
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
        else if (node.equals(THEME + HYPERSPECTRAL))
            jsonArray = getSpectraInstitionalProviders();

            //if we have a theme tree node, then go find the institutions providing the data for it
        else if (node.startsWith(THEME))
            jsonArray = getInstitionalProviders(node.replace(THEME, ""));

            //this is an institution being expanded, must be for spectral data, so go get the layers
        else if (node.startsWith(INSTITUTION))
            jsonArray = getHyperspectralLayers(node.replace(INSTITUTION, ""));

        //send it back...
        return this.getJsonModelAndView(jsonArray);
    }

    /**
     * Builds the theme list which will be sent back and displayed to the ExtJS tree in the portal
     *
     * @return
     */
    public JSONArray getThemes() {
        JSONArray jsonArray = new JSONArray();

        Map<String, Serializable> hyperspectral = new HashMap<String, Serializable>();
        hyperspectral.put("id", THEME + HYPERSPECTRAL);
        hyperspectral.put("text", HYPERSPECTRAL);
        hyperspectral.put("checked", Boolean.FALSE);
        hyperspectral.put("leaf", Boolean.FALSE);
        jsonArray.add(hyperspectral);

        for(String themeName : THEMES) {
            Map<String, Serializable> theme = new HashMap<String, Serializable>();
            theme.put("id", THEME + themeName);
            theme.put("text", themeName);
            theme.put("checked", Boolean.FALSE);
            theme.put("leaf", Boolean.FALSE);
            jsonArray.add(theme);
        }

        //create a model and view and return it
        return jsonArray;
    }

    /**
     * Given a theme, provide a list of intitutions and also the WFS query to get the data
     * from this institution
     *
     * @param theme
     * @return
     */
    private JSONArray getInstitionalProviders(String theme) {
        CSWRecord[] cswRecords;
        try {
            cswRecords = new CSWClient(CSW_URL, themeContraints.get(theme)).getRecordResponse().getCSWRecords();

            JSONArray jsonArray = new JSONArray();
            for(CSWRecord record : cswRecords) {
                String wfsUrl = this.stripUrlAndGetFeatures(record.getServiceUrl());
                String serviceName = record.getServiceName();

                Map<String, Serializable> node = new HashMap<String, Serializable>();
                node.put("id", serviceName); //TODO: serviceID
                node.put("text", serviceName);
                node.put("checked", Boolean.FALSE);
                node.put("leaf", Boolean.TRUE);
                node.put("icon", icons.get(theme));
                node.put("layerType", "wfs");
                node.put("tileOverlay", "");
                node.put("kmlUrl", XSLT_PROXY_URL +wfsUrl+wfsQueryParams.get(theme));
                node.put("wfsUrl", PROXY_URL +wfsUrl+wfsQueryParams.get(theme));
                node.put("featureType", featureTypes.get(theme));
                //node.put("wfsUrl", "http://auscope-portal-dev.arrc.csiro.au/xsltRestProxy?url=http://mapgadgets.googlepages.com/cta.kml");
                //node.put("wfsUrl", "http://mapgadgets.googlepages.com/cta.kml");

                jsonArray.add(node);
            }

            return jsonArray;
        } catch (XPathExpressionException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        } catch (ParserConfigurationException e) {
            logger.error(e);
        } catch (SAXException e) {
            logger.error(e);
        }

        return new JSONArray();
    }

    /**
     * Returns a list of Institutions who are proving hyperpectral map data
     *
     * @return
     */
    private JSONArray getSpectraInstitionalProviders() {
        //TODO: need a geonetowrk query here!

        JSONArray jsonArray = new JSONArray();

        Map<String, Serializable> coe = new HashMap<String, Serializable>();
        coe.put("id", INSTITUTION + "waCoe");
        coe.put("text", "WA Center of Excellence for 3D Mineral Mapping");
        coe.put("checked", Boolean.FALSE);
        coe.put("leaf", Boolean.FALSE);
        jsonArray.add(coe);

        return jsonArray;
    }

    /**
     * Given an institution, grab all of the hyperspectral layers from its WMS's
     *
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

    public String stripUrlAndGetFeatures(String url) {
        String[] strings =  url.split("\\?");
        return strings[0].trim() + "?";
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

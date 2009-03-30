package org.auscope.portal.server.web.controllers;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.AbstractView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.ows.Layer;
import org.geotools.data.wms.WebMapServer;
import org.geotools.ows.ServiceException;
import org.xml.sax.SAXException;
import org.auscope.portal.csw.CSWClient;
import org.auscope.portal.csw.CSWRecord;
import org.auscope.portal.server.web.view.JSONView;

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

    // what is the purpose of that line?
    //public static final String XSLT_PROXY_URL = "http://auscope-portal-dev.arrc.csiro.au/xsltRestProxy?url=";
    public static final String XSLT_PROXY_URL = "/xsltRestProxy?url=";
    public static final String PROXY_URL = "/restproxy?";

    public static final String CSW_URL = "http://auscope-portal.arrc.csiro.au/geonetwork/srv/en/csw";

    public static final String BOREHOLE           = "Borehole";
    public static final String GNSS               = "Global Navigation Satellite Systems";
    public static final String GEODESY            = "Geodesy";
    public static final String GEOLOGIC_UNIT      = "Geologic Unit";
    public static final String MINERAL_OCCURENCES = "Mineral Occurrences";
    public static final String MINING_ACTIVITY = "Mining Activity";
    public static final String MINES = "Mines";
    
    //create some identifiers for each of the themes to be displayed in the portal
    public static final String[] THEMES = { BOREHOLE,
                                            GNSS,
                                            GEODESY,
                                            GEOLOGIC_UNIT,
                                            MINERAL_OCCURENCES,
                                            MINING_ACTIVITY,
                                            MINES};

    //create a map to hold the CSW query contraints for each theme
    public static final Map<String, String> themeContraints = new HashMap<String, String>() {{
        put(BOREHOLE, "<?xml+version=\"1.0\"+encoding=\"UTF-8\"?><Filter+xmlns=\"http://www.opengis.net/ogc\"+xmlns:gml=\"http://www.opengis.net/gml\"><And><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>WFS</Literal></PropertyIsEqualTo><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>gsml:Borehole</Literal></PropertyIsEqualTo></And></Filter>&constraintLanguage=FILTER&constraint_language_version=1.1.0");
        put(GNSS, "<?xml+version=\"1.0\"+encoding=\"UTF-8\"?><Filter+xmlns=\"http://www.opengis.net/ogc\"+xmlns:gml=\"http://www.opengis.net/gml\"><And><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>WFS</Literal></PropertyIsEqualTo><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>GPS</Literal></PropertyIsEqualTo><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>GNSS</Literal></PropertyIsEqualTo></And></Filter>&constraintLanguage=FILTER&constraint_language_version=1.1.0");
        put(GEODESY, "<?xml+version=\"1.0\"+encoding=\"UTF-8\"?><Filter+xmlns=\"http://www.opengis.net/ogc\"+xmlns:gml=\"http://www.opengis.net/gml\"><And><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>WFS</Literal></PropertyIsEqualTo><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>GPS</Literal></PropertyIsEqualTo><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>Geodesy</Literal></PropertyIsEqualTo></And></Filter>&constraintLanguage=FILTER&constraint_language_version=1.1.0");
        put(GEOLOGIC_UNIT, "<?xml+version=\"1.0\"+encoding=\"UTF-8\"?><Filter+xmlns=\"http://www.opengis.net/ogc\"+xmlns:gml=\"http://www.opengis.net/gml\"><And><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>WFS</Literal></PropertyIsEqualTo><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>gsml:GeologicUnit</Literal></PropertyIsEqualTo></And></Filter>&constraintLanguage=FILTER&constraint_language_version=1.1.0");
        put(MINERAL_OCCURENCES, "<?xml+version=\"1.0\"+encoding=\"UTF-8\"?><Filter+xmlns=\"http://www.opengis.net/ogc\"+xmlns:gml=\"http://www.opengis.net/gml\"><And><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>WFS</Literal></PropertyIsEqualTo><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>mo:MineralOccurrence</Literal></PropertyIsEqualTo></And></Filter>&constraintLanguage=FILTER&constraint_language_version=1.1.0");
        put(MINING_ACTIVITY, "<?xml+version=\"1.0\"+encoding=\"UTF-8\"?><Filter+xmlns=\"http://www.opengis.net/ogc\"+xmlns:gml=\"http://www.opengis.net/gml\"><And><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>WFS</Literal></PropertyIsEqualTo><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>mo:MiningActivity</Literal></PropertyIsEqualTo></And></Filter>&constraintLanguage=FILTER&constraint_language_version=1.1.0");
        put(MINES, "<?xml+version=\"1.0\"+encoding=\"UTF-8\"?><Filter+xmlns=\"http://www.opengis.net/ogc\"+xmlns:gml=\"http://www.opengis.net/gml\"><And><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>WFS</Literal></PropertyIsEqualTo><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>mo:Mine</Literal></PropertyIsEqualTo></And></Filter>&constraintLanguage=FILTER&constraint_language_version=1.1.0");
    }};

    //create a map to hold the get features query stuff
    public static final Map<String, String> wfsQueryParams = new HashMap<String, String>() {{
        put(BOREHOLE, "%26request=GetFeature%26typeName=gsml:Borehole");
        put(GNSS, "%26request=GetFeature%26typeName=sa:SamplingPoint");
        put(GEODESY, "%26request=GetFeature%26typeName=geodesy:stations");
        //put(GEOLOGIC_UNIT, "%26request=GetFeature%26typeName=gsml:MappedFeature%26maxFeatures=10");
        put(GEOLOGIC_UNIT, "%26request=GetFeature%26typeName=gsml:ShearDisplacementStructure%26maxFeatures=2");
        //put(MINERAL_OCCURENCES, "%26request=GetFeature%26typeName=mo:MiningFeatureOccurrence%26namespace=xmlns(mo=urn:cgi:xmlns:GGIC:MineralOccurrence:1.0)%26maxFeatures=1000");//outputformat=text%2Fxml%3B+subtype%3Dgml%2F3.1.1%26maxFeatures=200");
        put(MINERAL_OCCURENCES, "");
        put(MINING_ACTIVITY, "");
        put(MINES, "");
    }};

    //create a map to hold the get features query stuff
    public static final Map<String, String> icons = new HashMap<String, String>() {{
        put(BOREHOLE, "http://maps.google.com/mapfiles/kml/paddle/blu-blank.png");
        put(GNSS, "http://maps.google.com/mapfiles/kml/paddle/grn-blank.png");
        put(GEODESY, "http://maps.google.com/mapfiles/kml/paddle/wht-blank.png");
        put(GEOLOGIC_UNIT, "http://maps.google.com/mapfiles/kml/paddle/red-blank.png");
        put(MINERAL_OCCURENCES, "http://maps.google.com/mapfiles/kml/paddle/purple-blank.png");
        put(MINING_ACTIVITY, "http://maps.google.com/mapfiles/kml/paddle/orange-blank.png");
        put(MINES, "http://maps.google.com/mapfiles/kml/paddle/pink-blank.png");
    }};

    //create a map to hold the get features query stuff
    public static final Map<String, String> featureTypes = new HashMap<String, String>() {{
        put(BOREHOLE, "gsml:Borehole");
        put(GNSS, "http://maps.google.com/mapfiles/kml/paddle/grn-blank.png");
        put(GEODESY, "geodesy:stations");
        //put(GEOLOGIC_UNIT, "gsml:GeologicUnit");
        put(GEOLOGIC_UNIT, "gsml:ShearDisplacementStructure");
        put(MINERAL_OCCURENCES, "mo:MineralOccurrence");
        put(MINING_ACTIVITY, "mo:MiningActivity");
        put(MINES, "mo:Mine");
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
        //hyperspectral.put("checked", Boolean.FALSE);
        hyperspectral.put("leaf", Boolean.FALSE);
        jsonArray.add(hyperspectral);

        for(String themeName : THEMES) {
            Map<String, Serializable> theme = new HashMap<String, Serializable>();
            theme.put("id", THEME + themeName);
            theme.put("text", themeName);
            //theme.put("checked", Boolean.FALSE);
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
       /* if(theme.equals("Mineral Occurrences")) {
            //something
            JSONArray jsonArray = new JSONArray();
            Map<String, Serializable> node = new HashMap<String, Serializable>();
            node.put("id", "Mineral Occurrences PIRSA"); //TODO: serviceID
            node.put("text", "Mineral Occurrences PIRSA");
            node.put("checked", Boolean.FALSE);
            node.put("leaf", Boolean.TRUE);
            node.put("icon", icons.get(theme));
            node.put("layerType", "wfs");
            node.put("tileOverlay", "");
            node.put("kmlUrl", XSLT_PROXY_URL +"http://apacsrv1.arrc.csiro.au/deegree-wfs/services?service=WFS%26version=1.1.0%26request=GetFeature%26typename=mo:MiningFeatureOccurrence%26namespace=xmlns(mo=urn:cgi:xmlns:GGIC:MineralOccurrence:1.0)%26maxFeatures=1000");
            node.put("wfsUrl", PROXY_URL +"http://apacsrv1.arrc.csiro.au/deegree-wfs/services?service=WFS%26version=1.1.0%26request=GetFeature%26typename=mo:MiningFeatureOccurrence%26namespace=xmlns(mo=urn:cgi:xmlns:GGIC:MineralOccurrence:1.0)%26maxFeatures=1000");
            jsonArray.add(node);
            return jsonArray;

        } else {*/
            CSWRecord[] cswRecords;
            try {
                cswRecords = new CSWClient(CSW_URL, themeContraints.get(theme)).getRecordResponse().getCSWRecords();

                JSONArray jsonArray = new JSONArray();
                for(CSWRecord record : cswRecords) {
                    String wfsUrl = this.stripUrlAndGetFeatures(record.getServiceUrl());
                    String serviceName = record.getServiceName();

                    Map<String, Serializable> node = new HashMap<String, Serializable>();
                    node.put("id", serviceName+featureTypes.get(theme)); //TODO: serviceID
                    node.put("text", serviceName);
                    node.put("checked", Boolean.FALSE);
                    node.put("leaf", Boolean.TRUE);
                    node.put("icon", icons.get(theme));
                    node.put("layerType", "wfs");
                    node.put("tileOverlay", "");
                    node.put("kmlUrl", wfsUrl+wfsQueryParams.get(theme));
                    node.put("wfsUrl", wfsUrl+wfsQueryParams.get(theme));
                    //node.put("kmlUrl", XSLT_PROXY_URL +"http://apacsrv1.arrc.csiro.au/deegree-wfs/services?service=WFS%26version=1.1.0%26request=GetFeature%26typename=mo:MiningFeatureOccurrence%26namespace=xmlns(mo=urn:cgi:xmlns:GGIC:MineralOccurrence:1.0)%26maxFeatures=7000");
                    //node.put("wfsUrl", PROXY_URL +"http://apacsrv1.arrc.csiro.au/deegree-wfs/services?service=WFS%26version=1.1.0%26request=GetFeature%26typename=mo:MiningFeatureOccurrence%26namespace=xmlns(mo=urn:cgi:xmlns:GGIC:MineralOccurrence:1.0)%26maxFeatures=7000");
                    node.put("filterPanel", "");
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
            } catch (XmlException e) {
        	logger.error(e);
        }

            return new JSONArray();
        //}
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
        //coe.put("checked", Boolean.FALSE);
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

        return url.replace("&", "%26").trim();
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
 
 */


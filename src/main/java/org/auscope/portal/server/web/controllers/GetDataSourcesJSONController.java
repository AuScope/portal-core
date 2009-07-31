package org.auscope.portal.server.web.controllers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.HttpClient;
import org.auscope.portal.csw.CSWClient;
import org.auscope.portal.csw.CSWRecord;
import org.auscope.portal.csw.CSWNamespaceContext;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.web.view.JSONModelAndView;

import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.WebMapServer;
import org.geotools.ows.ServiceException;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.net.URL;
import java.io.*;
import java.util.*;

import net.sf.json.JSONArray;

/**
 * User: Mathew Wyatt
 * Date: 12/01/2009
 * Time: 2:01:42 PM
 */

@Controller
public class GetDataSourcesJSONController {
    //logger
    protected final Log logger = LogFactory.getLog(getClass());
    
    @Autowired
    private PortalPropertyPlaceholderConfigurer hostConfigurer;

    public static final String WEPMAPSERVICE = "Web Map Service Layers";
    
    public static final String XSLT_PROXY_URL = "/xsltRestProxy?url=";
    public static final String PROXY_URL      = "/restproxy?";

    public static final String BOREHOLE           = "Borehole";
    public static final String GNSS               = "Global Navigation Satellite Systems";
    public static final String GEODESY            = "Geodesy";
    public static final String GEOLOGIC_UNIT      = "Geologic Unit";
    public static final String EARTH_RESOURCES    = "Earth Resources";
    public static final String MINERAL_OCCURENCES = "Mineral Occurrences";
    public static final String MINING_ACTIVITY    = "Mining Activity";
    public static final String MINES              = "Mines";
    
    //create some identifiers for each of the themes to be displayed in the portal
    public static final String[] THEMES = { BOREHOLE,
                                            GNSS,
                                            GEODESY,
                                            GEOLOGIC_UNIT,
                                            EARTH_RESOURCES,
                                            MINERAL_OCCURENCES,
                                            MINING_ACTIVITY,
                                            MINES
                                          };

    //create a map to hold the CSW query contraints for each theme
    // TODO can filterAPI be used for CSW as well? 
    public static final Map<String, String> themeContraints = new HashMap<String, String>() {{
        put(BOREHOLE,
            "<?xml+version=\"1.0\"+encoding=\"UTF-8\"?>" +
            "<Filter+xmlns=\"http://www.opengis.net/ogc\"+xmlns:gml=\"http://www.opengis.net/gml\">" +
                "<And>" +
                    "<PropertyIsEqualTo>" +
                        "<PropertyName>keyword</PropertyName>" +
                        "<Literal>WFS</Literal>" +
                    "</PropertyIsEqualTo>" +
                    "<PropertyIsEqualTo>" +
                        "<PropertyName>keyword</PropertyName>" +
                        "<Literal>gsml:Borehole</Literal>" +
                    "</PropertyIsEqualTo>" +
                "</And>" +
            "</Filter>" +
            "&constraintLanguage=FILTER&constraint_language_version=1.1.0");
        
        put(GNSS,
            "<?xml+version=\"1.0\"+encoding=\"UTF-8\"?>" +
            "<Filter+xmlns=\"http://www.opengis.net/ogc\"+xmlns:gml=\"http://www.opengis.net/gml\">" +
                "<And>" +
                    "<PropertyIsEqualTo>" +
                        "<PropertyName>keyword</PropertyName>" +
                        "<Literal>WFS</Literal>" +
                    "</PropertyIsEqualTo>" +
                    "<PropertyIsEqualTo>" +
                        "<PropertyName>keyword</PropertyName>" +
                        "<Literal>GPS</Literal>" +
                    "</PropertyIsEqualTo>" +
                    "<PropertyIsEqualTo>" +
                        "<PropertyName>keyword</PropertyName>" +
                        "<Literal>GNSS</Literal>" +
                    "</PropertyIsEqualTo>" +
                "</And>" +
            "</Filter>" +
            "&constraintLanguage=FILTER&constraint_language_version=1.1.0");
        
        put(GEODESY, "<?xml+version=\"1.0\"+encoding=\"UTF-8\"?><Filter+xmlns=\"http://www.opengis.net/ogc\"+xmlns:gml=\"http://www.opengis.net/gml\"><And><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>WFS</Literal></PropertyIsEqualTo><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>GPS</Literal></PropertyIsEqualTo><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>Geodesy</Literal></PropertyIsEqualTo></And></Filter>&constraintLanguage=FILTER&constraint_language_version=1.1.0");
        
        put(GEOLOGIC_UNIT, "<?xml+version=\"1.0\"+encoding=\"UTF-8\"?><Filter+xmlns=\"http://www.opengis.net/ogc\"+xmlns:gml=\"http://www.opengis.net/gml\"><And><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>WFS</Literal></PropertyIsEqualTo><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>gsml:GeologicUnit</Literal></PropertyIsEqualTo></And></Filter>&constraintLanguage=FILTER&constraint_language_version=1.1.0");
        
        put(MINERAL_OCCURENCES, "<?xml+version=\"1.0\"+encoding=\"UTF-8\"?><Filter+xmlns=\"http://www.opengis.net/ogc\"+xmlns:gml=\"http://www.opengis.net/gml\"><And><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>WFS</Literal></PropertyIsEqualTo><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>er:MineralOccurrence</Literal></PropertyIsEqualTo></And></Filter>&constraintLanguage=FILTER&constraint_language_version=1.1.0");
        
        put(MINING_ACTIVITY, "<?xml+version=\"1.0\"+encoding=\"UTF-8\"?><Filter+xmlns=\"http://www.opengis.net/ogc\"+xmlns:gml=\"http://www.opengis.net/gml\"><And><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>WFS</Literal></PropertyIsEqualTo><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>er:MiningActivity</Literal></PropertyIsEqualTo></And></Filter>&constraintLanguage=FILTER&constraint_language_version=1.1.0");
        
        put(MINES, "<?xml+version=\"1.0\"+encoding=\"UTF-8\"?><Filter+xmlns=\"http://www.opengis.net/ogc\"+xmlns:gml=\"http://www.opengis.net/gml\"><And><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>WFS</Literal></PropertyIsEqualTo><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>er:Mine</Literal></PropertyIsEqualTo></And></Filter>&constraintLanguage=FILTER&constraint_language_version=1.1.0");
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
        put(MINERAL_OCCURENCES, "er:MineralOccurrence");
        put(MINING_ACTIVITY, "er:MiningActivity");
        put(MINES, "er:Mine");
    }};

    //some contants which will be used as prefixes in the tree nde name to identify themes and insitutions
    public static final String THEME = "THEME:";
    public static final String INSTITUTION = "INSTITUTION:";

    @RequestMapping("/dataSources.json")
    public ModelAndView getDataSources(@RequestParam String node) throws Exception {

        //a JSON array to handle the structures created
        JSONArray jsonArray = new JSONArray();

        //if we are opening the root node then we want to send back all of the available themes
        if (node.equals("root"))
            jsonArray = getThemes();
        
        if(node.equals(THEME + EARTH_RESOURCES))
           jsonArray = getEarthResources();
        
        //hyperspectral is a special case because it is MAP data, and has to be categorised futher down into layers
        //differently to the feature services
        else if (node.equals(THEME + WEPMAPSERVICE))
            jsonArray = getSpectraInstitionalProviders();

            //if we have a theme tree node, then go find the institutions providing the data for it
        else if (node.startsWith(THEME))
            jsonArray = getInstitutionalProviders(node.replace(THEME, ""));

        //this is an institution being expanded, must be for spectral data, so go get the layers
        else if (node.startsWith(INSTITUTION))
            jsonArray = getHyperspectralLayers(node.replace(INSTITUTION, ""));

        logger.debug(jsonArray.toString());
        
        //send it back...
        return new JSONModelAndView(jsonArray);
    }

    /**
     * Builds the theme list which will be sent back and displayed to the ExtJS tree in the portal
     *
     * @return
     */
    public JSONArray getThemes() {
        JSONArray jsonArray = new JSONArray();

        Map<String, Serializable> hyperspectral = new HashMap<String, Serializable>();
        hyperspectral.put("id", THEME + WEPMAPSERVICE);
        hyperspectral.put("text", WEPMAPSERVICE);
        //hyperspectral.put("checked", Boolean.FALSE);
        hyperspectral.put("leaf", Boolean.FALSE);
        jsonArray.add(hyperspectral);

        for(String themeName : THEMES) {
           if (!themeName.equals(MINERAL_OCCURENCES) && !themeName.equals(MINING_ACTIVITY) && !themeName.equals(MINES)) { 
              Map<String, Serializable> theme = new HashMap<String, Serializable>();
              theme.put("id", THEME + themeName);
              theme.put("text", themeName);
              //theme.put("checked", Boolean.FALSE);
              theme.put("leaf", Boolean.FALSE);
              jsonArray.add(theme);
           }
        }

        //create a model and view and return it
        return jsonArray;
    }
    
    private JSONArray getEarthResources() {
       JSONArray jsonArray = new JSONArray();

       Map<String, Serializable> minOcc = new HashMap<String, Serializable>();
       minOcc.put("id", THEME + MINERAL_OCCURENCES);
       minOcc.put("text", MINERAL_OCCURENCES);
       //hyperspectral.put("checked", Boolean.FALSE);
       minOcc.put("leaf", Boolean.FALSE);
       jsonArray.add(minOcc);
       
       Map<String, Serializable> miningActivity = new HashMap<String, Serializable>();
       miningActivity.put("id", THEME + MINING_ACTIVITY);
       miningActivity.put("text", MINING_ACTIVITY);
       //hyperspectral.put("checked", Boolean.FALSE);
       miningActivity.put("leaf", Boolean.FALSE);
       jsonArray.add(miningActivity);
       
       Map<String, Serializable> mines = new HashMap<String, Serializable>();
       mines.put("id", THEME + MINES);
       mines.put("text", MINES);
       //hyperspectral.put("checked", Boolean.FALSE);
       mines.put("leaf", Boolean.FALSE);
       jsonArray.add(mines);

       //create a model and view and return it
       return jsonArray;
    }

    /**
     * Given a theme, provide a list of institutions and also the WFS query to get the data
     * from this institution
     *
     * @param theme
     * @return
     */
    private JSONArray getInstitutionalProviders(String theme) {
        CSWRecord[] cswRecords;

        try {
            String cswUrl = hostConfigurer.resolvePlaceholder("HOST.cswservice.url");            
            cswRecords =
                new CSWClient(cswUrl, themeContraints.get(theme))
                    .getRecordResponse().getCSWRecords();
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

                jsonArray.add(node);
            }
            return jsonArray;
        } catch (MissingResourceException e) {
            logger.error(e);
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
        //coe.put("checked", Boolean.FALSE);
        coe.put("leaf", Boolean.FALSE);
        jsonArray.add(coe);

        Map<String, Serializable> gsv = new HashMap<String, Serializable>();
        gsv.put("id", INSTITUTION + "gsv");
        gsv.put("text", "GSV");
        //coe.put("checked", Boolean.FALSE);
        gsv.put("leaf", Boolean.FALSE);
        jsonArray.add(gsv);

        Map<String, Serializable> gswa = new HashMap<String, Serializable>();
        gswa.put("id", INSTITUTION + "gswa");
        gswa.put("text", "GSWA");
        //coe.put("checked", Boolean.FALSE);
        gswa.put("leaf", Boolean.FALSE);
        jsonArray.add(gswa);

        Map<String, Serializable> ga = new HashMap<String, Serializable>();
        ga.put("id", INSTITUTION + "ga");
        ga.put("text", "Geoscience Australia");
        //coe.put("checked", Boolean.FALSE);
        ga.put("leaf", Boolean.FALSE);
        jsonArray.add(ga);

        Map<String, Serializable> gaOutcrop = new HashMap<String, Serializable>();
        gaOutcrop.put("id", INSTITUTION + "gaOutcrop");
        gaOutcrop.put("text", "Geoscience Australia Outcrop");
        //coe.put("checked", Boolean.FALSE);
        gaOutcrop.put("leaf", Boolean.FALSE);
        jsonArray.add(gaOutcrop);

        logger.debug(jsonArray.toString());
        
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
       
        if(institution.equals("waCoe")) {
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
            
            JSONArray jsonArray = new JSONArray();
            
            WMSCapabilities capabilities = wms.getCapabilities();

            List<Layer> layers = capabilities.getLayerList();
            for (Layer layer : layers) {
                Map<String, Serializable> layerNode = new HashMap<String, Serializable>();
                layerNode.put("id", layer.getName());
                layerNode.put("text", layer.getName());
                layerNode.put("checked", Boolean.FALSE);
                layerNode.put("leaf", Boolean.TRUE);
                layerNode.put("layerType", "gmap");
                layerNode.put("wmsUrl", gmapsUrl);
                layerNode.put("tileOverlay", "");

                jsonArray.add(layerNode);
            }

            return jsonArray;
        } else if(institution.equals("gsv")) {
           return getWmsLayers(hostConfigurer.resolvePlaceholder("gsv.wms"));
        } else if(institution.equals("gswa")) {
           return getWmsLayers(hostConfigurer.resolvePlaceholder("gswa.wms"));
        } else if(institution.equals("ga")) {
           return getWmsLayers(hostConfigurer.resolvePlaceholder("geows.wms"));
        } else {
           return getWmsLayers(hostConfigurer.resolvePlaceholder("geows_outcrops.wms"));
        }
        
    }

    /**
     * Given WMS URL, connect to the server and retrieve 
     * metadata info about the server
     * 
     * @param server URL string
     * @return jsonArray 
     */
    private JSONArray getWmsLayers(String server) { 

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

       List<Layer> layers = capabilities.getLayerList().subList(1, capabilities.getLayerList().size());
       for (Layer layer : layers) {
           Map<String, Serializable> layerNode = new HashMap<String, Serializable>();
           layerNode.put("id", layer.getName());
           layerNode.put("text", layer.getTitle());
           layerNode.put("checked", Boolean.FALSE);
           layerNode.put("leaf", Boolean.TRUE);
           layerNode.put("layerType", "wms");
           layerNode.put("wmsUrl", server);
           layerNode.put("tileOverlay", "");

           jsonArray.add(layerNode);
       }

       return jsonArray;
   }

    public String stripUrlAndGetFeatures(String url) {
        return url.replace("&", "%26").trim();
    }

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        /*URL cswQuery = new URL("http://auscope-portal-test.arrc.csiro.au/geonetwork/srv/en/csw?request=GetRecords&service=CSW&resultType=results&namespace=csw:http://www.opengis.net/cat/csw&outputSchema=csw:IsoRecord&constraintLanguage=FILTER&constraint_language_version=1.1.0&maxRecords=100&typeNames=csw:Record");
        BufferedReader responseReader = new BufferedReader(new InputStreamReader(cswQuery.openStream()));

        String inputLine;
        StringBuffer xmlResponse = new StringBuffer();
        while ((inputLine = responseReader.readLine()) != null) {
            xmlResponse.append(inputLine);
        }*/                 

        GetMethod method = new GetMethod("http://auscope-portal-test.arrc.csiro.au/geonetwork/srv/en/csw?");

        //set all of the parameters
        NameValuePair service = new NameValuePair("service", "CSW");
        NameValuePair request = new NameValuePair("request", "GetRecords");
        NameValuePair resultType = new NameValuePair("resultType", "results");
        NameValuePair namespace = new NameValuePair("namespace", "csw:http://www.opengis.net/cat/csw");
        NameValuePair outputSchema = new NameValuePair("outputSchema", "csw:IsoRecord");
        NameValuePair constraintLanguage = new NameValuePair("constraintLanguage", "FILTER");
        NameValuePair version = new NameValuePair("constraint_language_version", "1.1.0");
        NameValuePair maxRecords = new NameValuePair("maxRecords", "100");
        NameValuePair typeNames = new NameValuePair("typeNames", "csw:Record");

        //attach them to the method
        method.setQueryString(new NameValuePair[]{service, version, request, outputSchema, constraintLanguage, maxRecords, typeNames, resultType, namespace});

        new HttpClient().executeMethod(method);

        String xmlResponse = method.getResponseBodyAsString();

        System.out.println(xmlResponse);
        //CSWRecord[] cswRecords = new CSWGetRecordResponse(buildDom(xmlResponse.toString())).getCSWRecords();

        Document records = buildDom(xmlResponse.toString());
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new CSWNamespaceContext());
        String serviceTitleExpression = "/csw:GetRecordsResponse/csw:SearchResults/gmd:MD_Metadata";
        NodeList nodes = (NodeList) xPath.evaluate(serviceTitleExpression, records, XPathConstants.NODESET);

        System.out.println(nodes.getLength());

        for(int i=0; i<nodes.getLength(); i++ ) {
            XPath nodexPath = XPathFactory.newInstance().newXPath();
            nodexPath.setNamespaceContext(new CSWNamespaceContext());

            String dataIdentification = "gmd:identificationInfo/gmd:MD_DataIdentification";
            Node identificationNode = (Node) nodexPath.evaluate(dataIdentification, nodes.item(i), XPathConstants.NODE);

            System.out.println(identificationNode);

            String linkXPath = "gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL";
            String link = (String) nodexPath.evaluate(linkXPath, nodes.item(i), XPathConstants.STRING);

            String protocolXPath= "gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString";
            String protocol = (String) nodexPath.evaluate(protocolXPath, nodes.item(i), XPathConstants.STRING);

            String nameXPath= "gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:name/gco:CharacterString";
            String name = (String) nodexPath.evaluate(nameXPath, nodes.item(i), XPathConstants.STRING);

            String descriptionXPath= "gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:description/gco:CharacterString";
            String description = (String) nodexPath.evaluate(descriptionXPath, nodes.item(i), XPathConstants.STRING);

            if(identificationNode != null ) { // these nodes are data nodes from a WFS i.e. features

                System.out.println("--");
                System.out.println(link);
                System.out.println(protocol);
                System.out.println(name);
                System.out.println(description);
                System.out.println("");

            }
        }
    }

    public static Document buildDom(String xmlString) throws ParserConfigurationException, IOException, SAXException {
        //build the XML dom
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(xmlString.toString()));
        Document doc = builder.parse(inputSource);

        return doc;
    }
}
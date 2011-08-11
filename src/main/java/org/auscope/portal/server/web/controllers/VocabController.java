package org.auscope.portal.server.web.controllers;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import net.sf.json.JSONArray;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.web.SISSVocMethodMaker;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.web.view.JSONModelAndView;
import org.auscope.portal.vocabs.VocabularyServiceResponseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Controller that enables access to vocabulary services.
 */
@Controller
public class VocabController extends BasePortalController {
    protected final Log log = LogFactory.getLog(getClass());

    private HttpServiceCaller httpServiceCaller;
    private VocabularyServiceResponseHandler vocabularyServiceResponseHandler;
    private PortalPropertyPlaceholderConfigurer portalPropertyPlaceholderConfigurer;
    private SISSVocMethodMaker sissVocMethodMaker;

    /**
     * Construct
     * @param
     */
    @Autowired
    public VocabController(HttpServiceCaller httpServiceCaller,
                           VocabularyServiceResponseHandler vocabularyServiceResponseHandler,
                           PortalPropertyPlaceholderConfigurer portalPropertyPlaceholderConfigurer,
                           SISSVocMethodMaker sissVocMethodMaker) {

        this.httpServiceCaller = httpServiceCaller;
        this.vocabularyServiceResponseHandler = vocabularyServiceResponseHandler;
        this.portalPropertyPlaceholderConfigurer = portalPropertyPlaceholderConfigurer;
        this.sissVocMethodMaker = sissVocMethodMaker;
    }

    /**
     * Performs a query to the vocabulary service on behalf of the client and returns a JSON Map
     * success: Set to either true or false
     * data: The raw XML response
     * scopeNote: The scope note element from the response
     * label: The label element from the response
     * @param repository
     * @param label
     * @return
     */
    @RequestMapping("/getScalar.do")
    public ModelAndView getScalarQuery( @RequestParam("repository") final String repository,
                                        @RequestParam("label") final String label) throws Exception {
        String response = "";

        //Attempt to request and parse our response
        try {
            //Do the request
            String url = portalPropertyPlaceholderConfigurer.resolvePlaceholder("HOST.vocabService.url");
            HttpMethodBase method = sissVocMethodMaker.getConceptByLabelMethod(url, repository, label);
            response = httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());

            //Parse the response
            XPath xPath = XPathFactory.newInstance().newXPath();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(response));
            Document doc = builder.parse(inputSource);

            String extractLabelExpression = "/RDF/Concept/prefLabel";
            Node tempNode = (Node)xPath.evaluate(extractLabelExpression, doc, XPathConstants.NODE);
            final String labelString = tempNode != null ? tempNode.getTextContent() : "";

            String extractScopeExpression = "/RDF/Concept/scopeNote";
            tempNode = (Node)xPath.evaluate(extractScopeExpression, doc, XPathConstants.NODE);
            final String scopeNoteString = tempNode != null ? tempNode.getTextContent() : "";

            String extractDefinitionExpression = "/RDF/Concept/definition";
            tempNode = (Node)xPath.evaluate(extractDefinitionExpression, doc, XPathConstants.NODE);
            final String definitionString = tempNode != null ? tempNode.getTextContent() : "";

            return generateJSONResponseMAV(true, createScalarQueryModel(response, scopeNoteString, labelString, definitionString), "");
        } catch (Exception ex) {
            //On error, just return failure JSON (and the response string if any)
            log.error("getVocabQuery ERROR: " + ex.getMessage());

            return generateJSONResponseMAV(true, null, "");
        }
    }

    private ModelMap createScalarQueryModel
            (final String response, final String scopeNote, final String label, final String definition) {
        ModelMap map = new ModelMap();
        map.put("response", response);
        map.put("scopeNote", scopeNote);
        map.put("definition", definition);
        map.put("label", label);

        return map;
    }

    /**
     * Get all GA commodity URNs with prefLabels
     *
     * @param
     */
    @RequestMapping("getAllCommodities.do")
    public ModelAndView getAllCommodities() throws Exception {

        String response = "";
        JSONArray dataItems = new JSONArray();

        //Attempt to request and parse our response
        try {
            //Do the request
            String url = portalPropertyPlaceholderConfigurer.resolvePlaceholder("HOST.vocabService.url");
            HttpMethodBase method = sissVocMethodMaker.getCommodityMethod(url, "commodity_vocab", "urn:cgi:classifierScheme:GA:commodity");
            response = httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());

            // Parse the response
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(response));
            Document doc = builder.parse(inputSource);

            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList exprResult = (NodeList)xPath.evaluate("/sparql/results/result", doc, XPathConstants.NODESET);

            JSONArray tableRow;

            for (int i=0; i < exprResult.getLength(); i++) {

                Element result = (Element)exprResult.item(i);

                tableRow = new JSONArray();

                tableRow.add(result.getElementsByTagName("uri").item(0).getTextContent());
                tableRow.add(result.getElementsByTagName("literal").item(0).getTextContent());

                //add to the list
                dataItems.add(tableRow);
            }

            return new JSONModelAndView(dataItems);

        } catch (Exception ex) {
            //On error, just return failure JSON (and the response string if any)
            log.error("getAllCommodities Exception: " + ex.getMessage());

            return new JSONModelAndView(dataItems);
        }
    }


    /**
     * Get all GA CSW themes with preferred labels
     *
     * @param
     */
    @RequestMapping("getAllCSWThemes.do")
    public ModelAndView getAllCSWThemes() throws Exception {
        List<ModelMap> dataItems = new ArrayList<ModelMap>();

        //TODO: Lookup from vocab instead of using hardcoded values
        try {
            ModelMap map;

            String tab = "";

            map = new ModelMap();
            map.put("urn", "urn:fake:geography");map.put("label","Geography");map.put("indent",0);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:builtenvironment");map.put("label",tab + "Built Environment");map.put("indent",1);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:legislativelimits");map.put("label",tab + "Legislative Limits");map.put("indent",1);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:physgeo");map.put("label",tab + "Physical Geography");map.put("indent",1);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:earthobservations");map.put("label",tab + "Earth Observations");map.put("indent",1);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:socialgeoandeconomics");map.put("label",tab + "Social Geography and Economics");map.put("indent",1);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:geodesy");map.put("label",tab + "Geodesy");map.put("indent",1);
            dataItems.add(map);

            map = new ModelMap();
            map.put("urn", "urn:fake:geology");map.put("label","Geology");map.put("indent",0);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:geounits");map.put("label",tab + "Geological Units");map.put("indent",1);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:events");map.put("label",tab + "Events");map.put("indent",1);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:earthmat");map.put("label",tab + "Earth Materials");map.put("indent",1);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:geomorph");map.put("label",tab + "Geomorphology");map.put("indent",1);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:timescale");map.put("label",tab + "Timescale");map.put("indent",1);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:geologicalstructures");map.put("label",tab + "Geological Structures");map.put("indent",1);
            dataItems.add(map);

            map = new ModelMap();
            map.put("urn", "urn:fake:geophysics");map.put("label",tab + "Geophysics");map.put("indent",0);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:gravimetry");map.put("label",tab + "Gravimetry");map.put("indent",1);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:mag");map.put("label",tab + "Magnetism");map.put("indent",1);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:electrmag");map.put("label",tab + "Electromagnetics");map.put("indent",1);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:sesimology");map.put("label",tab + "Seismology");map.put("indent",1);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:acoustic");map.put("label",tab + "Acoustic");map.put("indent",1);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:radiometrics");map.put("label",tab + "Radiometrics");map.put("indent",1);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:spectra");map.put("label",tab + "Spectra");map.put("indent",1);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:rockprop");map.put("label",tab + "Rock Properties");map.put("indent",1);
            dataItems.add(map);

            map = new ModelMap();
            map.put("urn", "urn:fake:geochemistry");map.put("label","Geochemistry");map.put("indent",0);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:atmo");map.put("label",tab + "Atmospheric Geochemistry");map.put("indent",1);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:hydro");map.put("label",tab + "Hydro-Geochemistry");map.put("indent",1);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:iso");map.put("label",tab + "Isotope Geochemistry");map.put("indent",1);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:ino");map.put("label",tab + "Inorganic Geochemistry");map.put("indent",1);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:org");map.put("label",tab + "Organic Geochemistry");map.put("indent",1);
            dataItems.add(map);

            map = new ModelMap();
            map.put("urn", "urn:fake:resources");map.put("label","Resources");map.put("indent",0);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:energy");map.put("label",tab + "Energy");map.put("indent",1);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:mineralcomm");map.put("label",tab + "Mineral Commodities");map.put("indent",1);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:water");map.put("label",tab + "Water resources");map.put("indent",1);
            dataItems.add(map);

            map = new ModelMap();
            map.put("urn", "urn:fake:biology");map.put("label","Biology");map.put("indent",0);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:pal");map.put("label",tab + "Palaeontology");map.put("indent",1);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:marine");map.put("label",tab + "Marine Ecology");map.put("indent",1);
            dataItems.add(map);
            map = new ModelMap();
            map.put("urn", "urn:fake:terreco");map.put("label",tab + "Terrestrial Ecology");map.put("indent",1);
            dataItems.add(map);

            return generateJSONResponseMAV(true, dataItems, "");
        } catch (Exception ex) {
            //On error, just return failure JSON (and the response string if any)
            log.error("getAllCSWThemes Exception: ", ex);
            return generateJSONResponseMAV(false, null, "");
        }
    }
}

package org.auscope.portal.server.web.controllers;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import net.sf.json.JSONArray;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.domain.vocab.Concept;
import org.auscope.portal.server.domain.vocab.ConceptFactory;
import org.auscope.portal.server.domain.vocab.VocabNamespaceContext;
import org.auscope.portal.server.util.DOMUtil;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.web.SISSVocMethodMaker;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.web.view.JSONModelAndView;
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
    private ConceptFactory conceptFactory;
    private PortalPropertyPlaceholderConfigurer portalPropertyPlaceholderConfigurer;
    private SISSVocMethodMaker sissVocMethodMaker;

    /**
     * Construct
     * @param
     */
    @Autowired
    public VocabController(HttpServiceCaller httpServiceCaller,
                           ConceptFactory conceptFactory,
                           PortalPropertyPlaceholderConfigurer portalPropertyPlaceholderConfigurer,
                           SISSVocMethodMaker sissVocMethodMaker) {

        this.httpServiceCaller = httpServiceCaller;
        this.conceptFactory = conceptFactory;
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

        //Attempt to request and parse our response
        try {
            //Do the request
            String url = portalPropertyPlaceholderConfigurer.resolvePlaceholder("HOST.vocabService.url");
            HttpMethodBase method = sissVocMethodMaker.getConceptByLabelMethod(url, repository, label);
            InputStream responseStream = httpServiceCaller.getMethodResponseAsStream(method, httpServiceCaller.getHttpClient());

            //Parse the response
            Document doc = DOMUtil.buildDomFromStream(responseStream);
            XPathExpression rdfExpression = DOMUtil.compileXPathExpr("rdf:RDF", new VocabNamespaceContext());
            Node response = (Node) rdfExpression.evaluate(doc, XPathConstants.NODE);
            Concept[] concepts = conceptFactory.parseFromRDF(response);

            //Extract our strings
            String labelString = "";
            String scopeNoteString = "";
            String definitionString = "";
            if (concepts != null && concepts.length > 0) {
                labelString = concepts[0].getPreferredLabel();
                scopeNoteString = concepts[0].getDefinition();  //this is for legacy support
                definitionString = concepts[0].getDefinition();
            }

            return generateJSONResponseMAV(true, createScalarQueryModel(scopeNoteString, labelString, definitionString), "");
        } catch (Exception ex) {
            //On error, just return failure JSON (and the response string if any)
            log.error("getVocabQuery ERROR: " + ex.getMessage());

            return generateJSONResponseMAV(true, null, "");
        }
    }

    private ModelMap createScalarQueryModel(final String scopeNote, final String label, final String definition) {
        ModelMap map = new ModelMap();
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
     * Converts a concept and all related sub concepts into a list of model maps that can be sent to the GUI
     * for rendering in some form of simple list
     * @param concept
     * @return
     */
    private List<ModelMap> denormaliseConceptTree(Concept concept, int currentIndent,  List<String> traversedUrns) {
        List<ModelMap> denormalisedConcepts = new ArrayList<ModelMap>();

        //This is to avoid any infinite cycles in our concept 'tree'
        if (traversedUrns.contains(concept.getUrn())) {
            return denormalisedConcepts;
        } else {
            traversedUrns.add(concept.getUrn());
        }

        //Build our map for concept
        ModelMap map = new ModelMap();
        map.put("urn", concept.getUrn());
        map.put("label", concept.getPreferredLabel());
        map.put("indent", currentIndent);
        denormalisedConcepts.add(map);

        //recurse into our children (identified by the skos:narrower relation)
        for (Concept childConcept : concept.getNarrower()) {
            List<ModelMap> denormalisedChildren = denormaliseConceptTree(childConcept, currentIndent + 1, traversedUrns);
            denormalisedConcepts.addAll(denormalisedChildren);
        }

        return denormalisedConcepts;
    }

    /**
     * Get all GA CSW themes with preferred labels
     *
     * @param
     */
    @RequestMapping("getAllCSWThemes.do")
    public ModelAndView getAllCSWThemes() throws Exception {
        //Make our method for querying SISVoc for all GA themes
        String url = portalPropertyPlaceholderConfigurer.resolvePlaceholder("HOST.vocabService.url");
        HttpMethodBase method = sissVocMethodMaker.getConceptByLabelMethod(url, "ga-darwin", "*");

        //Make the request, parse it into a document
        Node response = null;
        try {
            InputStream responseStream = httpServiceCaller.getMethodResponseAsStream(method, httpServiceCaller.getHttpClient());
            Document doc = DOMUtil.buildDomFromStream(responseStream);
            XPathExpression rdfExpression = DOMUtil.compileXPathExpr("rdf:RDF", new VocabNamespaceContext());
            response = (Node) rdfExpression.evaluate(doc, XPathConstants.NODE);
        } catch (Exception ex) {
            log.warn("Error querying SISSVoc service", ex);
            return generateJSONResponseMAV(false);
        }

        //Take our response RDF and parse it into concepts
        Concept[] concepts = null;
        try {
            concepts = conceptFactory.parseFromRDF(response);
        } catch (Exception ex) {
            log.warn("Error parsing SISSVoc response", ex);
            return generateJSONResponseMAV(false);
        }

        //Just to be on the safe side
        if (concepts == null) {
            log.warn("Error parsing SISSVoc response (null response)");
            return generateJSONResponseMAV(false);
        }

        //Simplify our concepts for the GUI
        List<ModelMap> dataItems = new ArrayList<ModelMap>();
        for (Concept concept : concepts) {
            List<ModelMap> denormalisedConcept = denormaliseConceptTree(concept, 0, new ArrayList<String>());
            dataItems.addAll(denormalisedConcept);
        }

        log.debug(String.format("returning a list of %1$d themes", dataItems.size()));;

        return generateJSONResponseMAV(true, dataItems, "");
    }
}

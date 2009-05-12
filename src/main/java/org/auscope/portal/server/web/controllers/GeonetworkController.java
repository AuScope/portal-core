package org.auscope.portal.server.web.controllers;

import org.auscope.portal.csw.CSWGetRecordResponse;
import org.auscope.portal.csw.CSWRecord;
import org.auscope.portal.csw.CSWNamespaceContext;
import org.auscope.portal.server.web.view.JSONView;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.apache.xmlbeans.XmlException;
import org.apache.log4j.Logger;
import org.isotc211.x2005.gmd.MDMetadataDocument;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ui.ModelMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;
import java.net.URL;
import java.io.*;
import java.util.Map;
import java.util.HashMap;

import net.sf.json.JSONArray;

/**
 * User: Mathew Wyatt
 * Date: 08/05/2009
 * Time: 8:58:26 AM
 */
@Controller
public class GeonetworkController {
    private Logger logger = Logger.getLogger(getClass());

    /**
     * Returns a JSON response with a data structure like so
     *
     * [
     * [title: "", description: "", itemId: ""],
     * [title: "", description: "", itemId: ""]
     * ]
     *
     * @return
     */
    @RequestMapping("/getDataSources.do")
    public ModelAndView getDataSources(ModelMap model) {

        //Map<String, Serializable> rootItems = new HashMap<String, Serializable>();
        //model.put("totalCount", 1);

        JSONArray dataItems = new JSONArray();

        /*Map<String, Serializable> itemOne = new HashMap<String, Serializable>();
        itemOne.put("title", "blahTitle");
        itemOne.put("description", "blahDescription");
        itemOne.put("itemId", 1);
*/
        JSONArray tableRow = new JSONArray();
        tableRow.add("Blah Title");
        tableRow.add("Blah Description");

        dataItems.add(tableRow);
        //model.put("items", dataItems);

        model.put("JSON_OBJECT", dataItems);
        return new ModelAndView(new JSONView(), model);
    }

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, XmlException, XPathExpressionException {
        URL cswQuery = new URL("http://auscope-portal.arrc.csiro.au/geonetwork/srv/en/csw?request=GetRecords&service=CSW&resultType=results&namespace=csw:http://www.opengis.net/cat/csw&outputSchema=csw:IsoRecord&constraintLanguage=FILTER&constraint_language_version=1.1.0&maxRecords=63");
        BufferedReader responseReader = new BufferedReader(new InputStreamReader(cswQuery.openStream()));

        String inputLine;
        StringBuffer xmlResponse = new StringBuffer();
        while ((inputLine = responseReader.readLine()) != null) {
            xmlResponse.append(inputLine);
        }

        //CSWRecord[] cswRecords = new CSWGetRecordResponse(buildDom(xmlResponse.toString())).getCSWRecords();

        Document records = buildDom(xmlResponse.toString());
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new CSWNamespaceContext());
        String serviceTitleExpression = "/csw:GetRecordsResponse/csw:SearchResults/gmd:MD_Metadata";
        NodeList nodes = (NodeList) xPath.evaluate(serviceTitleExpression, records, XPathConstants.NODESET);

        for(int i=0; i<nodes.getLength(); i++ ) {
            XPath nodexPath = XPathFactory.newInstance().newXPath();
            nodexPath.setNamespaceContext(new CSWNamespaceContext());

            String dataIdentification = "gmd:identificationInfo/gmd:MD_DataIdentification";
            Node identificationNode = (Node) nodexPath.evaluate(dataIdentification, nodes.item(i), XPathConstants.NODE);

            String linkXPath = "gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL";
            String link = (String) nodexPath.evaluate(linkXPath, nodes.item(i), XPathConstants.STRING);

            String protocolXPath= "gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString";
            String protocol = (String) nodexPath.evaluate(protocolXPath, nodes.item(i), XPathConstants.STRING);

            String nameXPath= "gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:name/gco:CharacterString";
            String name = (String) nodexPath.evaluate(nameXPath, nodes.item(i), XPathConstants.STRING);

            String descriptionXPath= "gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:description/gco:CharacterString";
            String description = (String) nodexPath.evaluate(descriptionXPath, nodes.item(i), XPathConstants.STRING);

            if(identificationNode != null && protocol.contains("WFS")) { // these nodes are data nodes from a WFS i.e. features

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

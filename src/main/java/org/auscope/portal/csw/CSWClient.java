package org.auscope.portal.csw;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.namespace.NamespaceContext;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * User: Mathew Wyatt
 * Date: 11/02/2009
 * Time: 10:41:30 AM
 */
public class CSWClient {
    private String serviceUrl = "";
    private String constraint;

    public CSWClient(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public CSWClient(String serviceUrl, String constraint) {
        this.serviceUrl = serviceUrl;
        this.constraint = constraint;
    }


    public CSWGetRecordResponse getRecordResponse() throws IOException, ParserConfigurationException, SAXException {
        URL cswQuery = buildQueryUrl();
        BufferedReader responseReader = new BufferedReader(new InputStreamReader(cswQuery.openStream()));

        String inputLine;
        StringBuffer xmlResponse = new StringBuffer();
        while ((inputLine = responseReader.readLine()) != null) {
            xmlResponse.append(inputLine);
        }

        return new CSWGetRecordResponse(buildDom(xmlResponse.toString()));
    }

    private URL buildQueryUrl() throws MalformedURLException {
        return new URL(serviceUrl+"?request=GetRecords&service=CSW&version=2.0.2&resultType=results&namespace=csw:http://www.opengis.net/cat/csw/2.0.2&outputSchema=csw:IsoRecord&constraint=" + constraint);
    }

    private Document buildDom(String xmlString) throws ParserConfigurationException, IOException, SAXException {
        //build the XML dom
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(xmlString.toString()));
        Document doc = builder.parse(inputSource);

        return doc;
    }

    public static void main(String[] args) throws IOException, XPathExpressionException, ParserConfigurationException, SAXException {

        //call the geonetwork and query
        URL cswQuery = new URL("http://auscope-portal.arrc.csiro.au/geonetwork/srv/en/csw?request=GetRecords&service=CSW&version=2.0.2&resultType=results&namespace=csw:http://www.opengis.net/cat/csw/2.0.2&outputSchema=csw:IsoRecord&constraint=<?xml+version=\"1.0\"+encoding=\"UTF-8\"?><Filter+xmlns=\"http://www.opengis.net/ogc\"+xmlns:gml=\"http://www.opengis.net/gml\"><And><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>WFS</Literal></PropertyIsEqualTo><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>gsml:Borehole</Literal></PropertyIsEqualTo></And></Filter>&constraintLanguage=FILTER&constraint_language_version=1.1.0");
        BufferedReader responseReader = new BufferedReader(new InputStreamReader(cswQuery.openStream()));

        //pull the XMl response into a string
        String inputLine;
        StringBuffer xmlResponse = new StringBuffer();
        while ((inputLine = responseReader.readLine()) != null) {
            xmlResponse.append(inputLine);
            System.out.println(inputLine);
        }

        //build the XML dom
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(xmlResponse.toString()));
        org.w3c.dom.Document doc = builder.parse(inputSource);

        /*Create an XPath instances and set up the namespaces. If you don't set the namespaces the query will
        return nothing */
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new NamespaceContext() {
            Map<String, String> map = new HashMap<String, String>() {{
                put("gmd", "http://www.isotc211.org/2005/gmd");
                put("srv", "http://www.isotc211.org/2005/srv");
                put("csw", "http://www.opengis.net/cat/csw/2.0.2");
                put("gco", "http://www.isotc211.org/2005/gco");
            }};

            public String getNamespaceURI(String s) {
                return map.get(s);
            }

            public String getPrefix(String s) {
                return null;
            }

            public Iterator getPrefixes(String s) {
                return null;
            }
        });

        //this expression gets the service titles
        String serviceTitleExpression = "/csw:GetRecordsResponse/csw:SearchResults/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:title";
        NodeList nodes = (NodeList) xPath.evaluate(serviceTitleExpression, doc, XPathConstants.NODESET);

        for (int i = 0; i < nodes.getLength(); i++) {
            System.out.println(nodes.item(i).getTextContent());
        }

        //this expression gets the service get capabilities URL
        String serviceUrleExpression = "/csw:GetRecordsResponse/csw:SearchResults/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage";
        nodes = (NodeList) xPath.evaluate(serviceUrleExpression, doc, XPathConstants.NODESET);

        for (int i = 0; i < nodes.getLength(); i++) {
            System.out.println(nodes.item(i).getTextContent());
        }
    }
}

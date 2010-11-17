package org.auscope.portal.mineraloccurrence;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.domain.ows.OWSExceptionParser;
import org.springframework.stereotype.Repository;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * User: Mathew Wyatt
 * Date: 23/03/2009
 * Time: 4:53:18 PM
 */
@Repository
public class MineralOccurrencesResponseHandler {
	protected final Log log = LogFactory.getLog(getClass());

    public List<Mine> getMines(String mineResponse) throws Exception {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document mineDocument = builder.parse(new ByteArrayInputStream(mineResponse.getBytes("UTF-8")));

        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());

        //Do some rudimentary error testing
        OWSExceptionParser.checkForExceptionResponse(mineDocument);

        XPathExpression expr = xPath.compile("/wfs:FeatureCollection/gml:featureMember/er:MiningFeatureOccurrence/er:specification/er:Mine | /wfs:FeatureCollection/gml:featureMembers/er:MiningFeatureOccurrence/er:specification/er:Mine");
        NodeList mineNodes = (NodeList)expr.evaluate(mineDocument, XPathConstants.NODESET);
        ArrayList<Mine> mines = new ArrayList<Mine>();

        for(int i=0; i < mineNodes.getLength(); i++) {
            mines.add(new Mine(mineNodes.item(i)));
        }
        return mines;
    }

    public Collection<Commodity> getCommodities(String commodityResponse) throws Exception {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document commodityDocument = builder.parse(new ByteArrayInputStream(commodityResponse.getBytes("UTF-8")));

        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());

        //Do some rudimentary error testing
        OWSExceptionParser.checkForExceptionResponse(commodityDocument);

        XPathExpression expr = xPath.compile("//er:Commodity");
        NodeList commodityNodes = (NodeList)expr.evaluate(commodityDocument, XPathConstants.NODESET);
        ArrayList<Commodity> commodities = new ArrayList<Commodity>();

        for(int i=0; i < commodityNodes.getLength(); i++) {
            commodities.add(new Commodity(commodityNodes.item(i)));
        }

        return commodities;
    }

    public int getNumberOfFeatures(String mineralOccurrenceResponse) throws Exception {

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document mineralOccurrenceDocument = builder.parse(new ByteArrayInputStream(mineralOccurrenceResponse.getBytes("UTF-8")));

        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());

        //Do some rudimentary error testing
        OWSExceptionParser.checkForExceptionResponse(mineralOccurrenceDocument);

        try {
            XPathExpression expr = xPath.compile("/wfs:FeatureCollection");
            Node result = (Node)expr.evaluate(mineralOccurrenceDocument, XPathConstants.NODE);
            return Integer.parseInt(result.getAttributes().getNamedItem("numberOfFeatures").getTextContent());
        } catch (Exception e) {
        	return 0;

        }
    }
}

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
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.domain.ows.OWSExceptionParser;
import org.springframework.stereotype.Repository;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handler for typed responses from mineral occurrences WFS queries.
 * Id: $$
 */
@Repository
public class MineralOccurrencesResponseHandler {

    /** The log. */
    protected final Log log = LogFactory.getLog(getClass());

    /**
     * Gets the mines.
     *
     * @param mineResponse the mine response
     * @return the mines
     * @throws Exception the exception
     */
    public List<Mine> getMines(String mineResponse) throws Exception {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document mineDocument = builder.parse(new ByteArrayInputStream(mineResponse.getBytes("UTF-8")));

        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());

        //Do some rudimentary error testing
        OWSExceptionParser.checkForExceptionResponse(mineDocument);

        XPathExpression expr = xPath.compile("/wfs:FeatureCollection/gml:featureMember/er:MiningFeatureOccurrence/er:specification/er:Mine | /wfs:FeatureCollection/gml:featureMembers/er:MiningFeatureOccurrence/er:specification/er:Mine");
        NodeList mineNodes = (NodeList) expr.evaluate(mineDocument, XPathConstants.NODESET);
        ArrayList<Mine> mines = new ArrayList<Mine>();

        for (int i = 0; i < mineNodes.getLength(); i++) {
            mines.add(new Mine(mineNodes.item(i)));
        }
        return mines;
    }

    /**
     * Gets the commodities.
     *
     * @param commodityResponse the commodity response
     * @return the commodities
     * @throws Exception the exception
     */
    public Collection<Commodity> getCommodities(String commodityResponse) throws Exception {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document commodityDocument = builder.parse(new ByteArrayInputStream(commodityResponse.getBytes("UTF-8")));

        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());

        //Do some rudimentary error testing
        OWSExceptionParser.checkForExceptionResponse(commodityDocument);

        XPathExpression expr = xPath.compile("//er:Commodity");
        NodeList commodityNodes = (NodeList) expr.evaluate(commodityDocument, XPathConstants.NODESET);
        ArrayList<Commodity> commodities = new ArrayList<Commodity>();

        for (int i = 0; i < commodityNodes.getLength(); i++) {
            commodities.add(new Commodity(commodityNodes.item(i)));
        }

        return commodities;
    }

    /**
     * Gets the number of features - returns 0 unless it found something.
     *
     * @param mineralOccurrenceResponse the mineral occurrence response
     * @return the number of features
     * @throws Exception the exception
     */
    public int getNumberOfFeatures(String mineralOccurrenceResponse) throws Exception {
        int numberOfFeatures = 0;

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document mineralOccurrenceDocument = builder.parse(new ByteArrayInputStream(mineralOccurrenceResponse.getBytes("UTF-8")));

        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());

        //Do some rudimentary error testing
        OWSExceptionParser.checkForExceptionResponse(mineralOccurrenceDocument);

        try {
            XPathExpression expr = xPath.compile("/wfs:FeatureCollection/@numberOfFeatures");
            Node result = (Node) expr.evaluate(mineralOccurrenceDocument, XPathConstants.NODE);
            numberOfFeatures = Integer.parseInt(result.getTextContent());
        }  catch (XPathExpressionException  e) {
            log.debug("unable to compile xpath:" + e.getMessage());
        } catch (DOMException e) {
            log.debug("unable to evaluate xpath:" + e.getMessage());
        } catch (NumberFormatException e) {
            log.debug("unable to convert result to number:" + e.getMessage());
        }

        return numberOfFeatures;
    }
}

package org.auscope.portal.mineraloccurrence;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import junit.framework.Assert;

import org.auscope.portal.Util;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * ERML mine test harness.
 *
 * @version $Id$
 *
 * User: Mathew Wyatt
 * Date: 24/03/2009
 * Time: 9:01:48 AM
 */
public class TestMine {
    /** The Document  */
    private static final String MINEDOCUMENT = "src/test/resources/mineNode.xml";

    @Test
    public void testGetPrefferedName() throws XPathExpressionException, ParserConfigurationException, UnsupportedEncodingException, SAXException, IOException {

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document mineDocument = builder.parse(new ByteArrayInputStream(Util.loadXML(MINEDOCUMENT).getBytes("UTF-8")));
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());

        XPathExpression expr = xPath.compile("/er:Mine");
        Node mineNode = (Node)expr.evaluate(mineDocument, XPathConstants.NODE);
        Mine mine = new Mine(mineNode);

        Assert.assertEquals("Preffered mine name is Good Hope", "Good Hope", mine.getMineNamePreffered());
    }

    @Test
    public void testGetURI() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException, IOException {

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document mineDocument = builder.parse(new ByteArrayInputStream(Util.loadXML(MINEDOCUMENT).getBytes("UTF-8")));
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());

        XPathExpression expr = xPath.compile("/er:Mine");
        Node mineNode = (Node) expr.evaluate(mineDocument, XPathConstants.NODE);
        Mine mine = new Mine(mineNode);

        Assert.assertEquals("URI should be urn:cgi:feature:GSV:Mine:361068", "urn:cgi:feature:GSV:Mine:361068", mine.getMineNameURI());
    }

}

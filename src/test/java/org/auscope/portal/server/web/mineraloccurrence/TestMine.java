package org.auscope.portal.server.web.mineraloccurrence;

import org.junit.Before;
import org.junit.Test;
import org.junit.BeforeClass;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import javax.xml.xpath.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.*;

import junit.framework.Assert;

/**
 * User: Mathew Wyatt
 * Date: 24/03/2009
 * Time: 9:01:48 AM
 */
public class TestMine {

    private static Mine mine;
    private static XPath xPath;

    @BeforeClass
    public static void setup() throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {

        File mineContents = new File("src/test/resources/mineNode.xml");
        BufferedReader reader = new BufferedReader( new FileReader(mineContents) );
        StringBuffer mineXML = new StringBuffer();

        String str;
        while ((str = reader.readLine()) != null) {
            mineXML.append(str);
        }
        reader.close();

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document mineDocument = builder.parse(new ByteArrayInputStream(mineXML.toString().getBytes("UTF-8")));

        XPathFactory factory = XPathFactory.newInstance();
        xPath = factory.newXPath();
        xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());

        XPathExpression expr = xPath.compile("/mo:Mine");
        Node mineNode = (Node)expr.evaluate(mineDocument, XPathConstants.NODE);
        mine = new Mine(mineNode);

    }

    @Test
    public void testGetPrefferedName() throws XPathExpressionException {
        Assert.assertEquals("Preffered mine name is Good Hope", "Good Hope", mine.getMineNamePreffered());
    }

    @Test
    public void testGetURI() throws XPathExpressionException {
        Assert.assertEquals("URI should be urn:cgi:feature:GSV:Mine:361068", "urn:cgi:feature:GSV:Mine:361068", mine.getMineNameURI());
    }

}

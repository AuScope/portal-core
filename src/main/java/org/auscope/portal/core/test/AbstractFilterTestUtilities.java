package org.auscope.portal.core.test;

import java.io.IOException;
import java.util.Arrays;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.auscope.portal.core.services.namespaces.OGCNamespaceContext;
import org.auscope.portal.core.util.DOMUtil;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Contains a number of unit test utility methods that are useful for testing ogc:Filters
 * 
 * @author vot002
 *
 */
public abstract class AbstractFilterTestUtilities {

    /**
     * Wraps a string with <test> elements (ogc namespace aware) Returns the parsed document
     * 
     * @param xmlString
     * @return
     * @throws SAXException 
     * @throws IOException 
     * @throws ParserConfigurationException 
     */
    public static Document parsefilterStringXML(String xmlString) throws ParserConfigurationException, IOException, SAXException {
        NamespaceContext nsc = new OGCNamespaceContext();
        xmlString = String.format("<test xmlns:ogc=\"%1$s\">%2$s</test>", nsc.getNamespaceURI("ogc"), xmlString);

        return DOMUtil.buildDomFromString(xmlString);
    }

    /**
     * Runs an xpath query (which should return a nodeset) against doc and tests the number of items in the nodeset
     * 
     * @param doc
     * @param xPathQuery
     * @param expectedCount
     * @throws XPathExpressionException 
     */
    public static void runNodeSetValueCheck(Document doc, String xPathQuery, int expectedCount) throws XPathExpressionException {
        runNodeSetValueCheck(doc, xPathQuery, null, expectedCount);
    }

    /**
     * Runs an xpath query (which should return a nodeset) against doc and tests that each response object is a member from validValues
     * 
     * @param doc
     * @param xPathQuery
     * @param validValues
     * @throws XPathExpressionException 
     */
    public static void runNodeSetValueCheck(Document doc, String xPathQuery, String[] validValues) throws XPathExpressionException  {
        runNodeSetValueCheck(doc, xPathQuery, validValues, -1);
    }

    /**
     * Runs an xpath query (which should return a nodeset) against doc and tests that each response object is a member from validValues and there is a specified
     * number of elements returned
     * 
     * @param doc
     * @param xPathQuery
     * @param validValues
     * @throws XPathExpressionException 
     */
    public static void runNodeSetValueCheck(Document doc, String xPathQuery, String[] validValues, int expectedCount) throws XPathExpressionException
             {
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new OGCNamespaceContext());

        NodeList tempList = (NodeList) xPath.evaluate(xPathQuery, doc, XPathConstants.NODESET);
        Assert.assertNotNull(tempList);

        if (expectedCount >= 0)
            Assert.assertTrue(
                    String.format("Count of expression invalid. Expected %1$s Got %2$s", expectedCount,
                            tempList.getLength()), tempList.getLength() == expectedCount);

        if (validValues != null) {
            for (int i = 0; i < tempList.getLength(); i++) {

                String text = tempList.item(i).getTextContent();

                boolean foundValidValue = false;
                for (int j = 0; !foundValidValue && j < validValues.length; j++) {

                    foundValidValue = validValues[j].equals(text);
                }

                Assert.assertTrue(String.format("Failed to find '%1$s' in '%2$s'", text, Arrays.toString(validValues)),
                        foundValidValue);
            }
        }
    }
}

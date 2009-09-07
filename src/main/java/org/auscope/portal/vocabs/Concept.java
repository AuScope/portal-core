package org.auscope.portal.vocabs;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;

/**
 * User: Michael Stegherr
 * Date: 07/09/2009
 * Time: 2:02:06 PM
 */
public class Concept {

    private Node conceptNode;
    private XPath xPath;

    public Concept(Node conceptNode) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        this.conceptNode = conceptNode;

        XPathFactory factory = XPathFactory.newInstance();
        xPath = factory.newXPath();
        xPath.setNamespaceContext(new VocabularyServiceNamespaceContext());
    }

    public String getPreferredLabel() {
        try {
            XPathExpression expr = xPath.compile("skos:prefLabel");
            Node result = (Node)expr.evaluate(conceptNode, XPathConstants.NODE);
            return result.getTextContent();
        } catch (Exception e) {
            return "";
        }
    }

    public String getSchemeUrn() {
        try {
            XPathExpression expr = xPath.compile("skos:inScheme ");
            Node result = (Node)expr.evaluate(conceptNode, XPathConstants.NODE);
            return result.getAttributes().getNamedItem("rdf:resource").getTextContent();
        } catch (Exception e) {
            return "";
        }
    }
}

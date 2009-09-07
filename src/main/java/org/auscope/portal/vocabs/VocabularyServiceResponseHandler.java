package org.auscope.portal.vocabs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.springframework.stereotype.Repository;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * User: Michael Stegherr
 * Date: 07/09/2009
 * Time: 1:40:18 PM
 */
@Repository
public class VocabularyServiceResponseHandler {

    public List<Concept> getConcepts(String vocabServiceResponse) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document rdfDocument = builder.parse(new ByteArrayInputStream(vocabServiceResponse.getBytes("UTF-8")));

        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        xPath.setNamespaceContext(new VocabularyServiceNamespaceContext());

        XPathExpression expr = xPath.compile("/rdf:RDF/skos:Concept");
        NodeList conceptNodes = (NodeList)expr.evaluate(rdfDocument, XPathConstants.NODESET);
        ArrayList<Concept> concepts = new ArrayList<Concept>();

        for(int i=0; i < conceptNodes.getLength(); i++) {
            concepts.add(new Concept(conceptNodes.item(i)));
        }

        return concepts;
    }
}

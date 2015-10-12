package org.auscope.portal.core.util;

import java.io.InputStream;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.auscope.portal.core.services.responses.ows.OWSExceptionParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class DOMResponseUtil {

    public static int getNumberOfFeatures(InputStream gsmlResponse, NamespaceContext namespace) throws Exception {

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(gsmlResponse);

        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(namespace);

        //Do some rudimentary error testing
        OWSExceptionParser.checkForExceptionResponse(doc);

        try {
            XPathExpression expr = xPath.compile("/wfs:FeatureCollection");
            Node result = (Node) expr.evaluate(doc, XPathConstants.NODE);
            return Integer.parseInt(result.getAttributes().getNamedItem("numberOfFeatures").getTextContent());
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            gsmlResponse.close();
        }

    }

}

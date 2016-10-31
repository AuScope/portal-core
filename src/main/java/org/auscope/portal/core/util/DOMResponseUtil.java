package org.auscope.portal.core.util;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.auscope.portal.core.services.responses.ows.OWSException;
import org.auscope.portal.core.services.responses.ows.OWSExceptionParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class DOMResponseUtil {

    public static int getNumberOfFeatures(InputStream gsmlResponse, NamespaceContext namespace) throws IOException, OWSException {

        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true); // never forget this!
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(gsmlResponse);

            XPath xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(namespace);

            // Do some rudimentary error testing
            OWSExceptionParser.checkForExceptionResponse(doc);

            XPathExpression expr = xPath.compile("/wfs:FeatureCollection");
            Node result = (Node) expr.evaluate(doc, XPathConstants.NODE);
            return Integer.parseInt(result.getAttributes().getNamedItem("numberOfFeatures").getTextContent());
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException(e.getMessage(), e);
        } catch (XPathExpressionException e) {
            throw new OWSException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(gsmlResponse);
        }

    }

}

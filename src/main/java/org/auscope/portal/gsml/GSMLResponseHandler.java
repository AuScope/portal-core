package org.auscope.portal.gsml;

import java.io.ByteArrayInputStream;

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

@Repository
public class GSMLResponseHandler {

    protected final Log log = LogFactory.getLog(getClass());

    public int getNumberOfFeatures(String gsmlResponse) throws Exception {

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document yilgarnDoc = builder.parse(new ByteArrayInputStream(gsmlResponse.getBytes("UTF-8")));

        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        xPath.setNamespaceContext(new YilgarnNamespaceContext());

        //Do some rudimentary error testing
        OWSExceptionParser.checkForExceptionResponse(yilgarnDoc);

        try {
            XPathExpression expr = xPath.compile("/wfs:FeatureCollection");
            Node result = (Node)expr.evaluate(yilgarnDoc, XPathConstants.NODE);
            return Integer.parseInt(result.getAttributes().getNamedItem("numberOfFeatures").getTextContent());
        } catch (Exception e) {
            return 0;

        }
    }


}

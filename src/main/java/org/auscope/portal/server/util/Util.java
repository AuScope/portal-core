package org.auscope.portal.server.util;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.springframework.stereotype.Repository;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.StringReader;
import java.io.IOException;

/**
 * User: Mathew Wyatt
 * Date: 20/08/2009
 * Time: 4:42:13 PM
 */
@Repository
public class Util {
    public Document buildDomFromString(String xmlString) throws ParserConfigurationException, IOException, SAXException {
        //build the XML dom
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(xmlString.toString()));
        Document doc = builder.parse(inputSource);

        return doc;
    }
}

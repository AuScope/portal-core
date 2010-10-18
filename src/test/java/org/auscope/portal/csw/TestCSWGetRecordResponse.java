package org.auscope.portal.csw;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class TestCSWGetRecordResponse {

    private CSWGetRecordResponse recordResponse;

    @Before
    public void setup() throws ParserConfigurationException, SAXException, IOException {

        // load CSW record response document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc =
            builder.parse( "src/test/resources/cswRecordResponse.xml" );

        this.recordResponse = new CSWGetRecordResponse(doc);

    }

    @Test
    public void testGetCSWRecords() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

        CSWRecord[] recs = this.recordResponse.getCSWRecords();

        Assert.assertEquals(15, recs.length);
    }

}

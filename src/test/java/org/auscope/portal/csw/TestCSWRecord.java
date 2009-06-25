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

public class TestCSWRecord {

    private CSWRecord[] records;
    
    @Before
    public void setup() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

        // load CSW record response document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc =
            builder.parse( "src/test/resources/cswRecordResponse.xml" );
        
        CSWGetRecordResponse recResponse = new CSWGetRecordResponse(doc);        
        this.records = recResponse.getCSWRecords();
    }

    @Test
    public void testGetServiceName() throws XPathExpressionException {
        
        Assert.assertEquals(
                "NVCL Borehole WFS",
                this.records[0].getServiceName());
        
        Assert.assertEquals(
                "Alpha GeoServer PIRSA MinOcc",
                this.records[2].getServiceName());
    }

    @Test
    public void testGetServiceUrl() throws XPathExpressionException {
        
        Assert.assertEquals(
                "http://www.gsv-tb.dpi.vic.gov.au/AuScope-GeoSciML/services?service=WFS&version=1.1.0",
                this.records[4].getServiceUrl());
        
        Assert.assertEquals(
                "http://www.gsv-tb.dpi.vic.gov.au/AuScope-MineralOccurrence/services?",
                this.records[7].getServiceUrl());
    }

}

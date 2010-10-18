package org.auscope.portal.csw;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.auscope.portal.csw.CSWOnlineResource.OnlineResourceType;
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
                "GSV GeologicUnit WFS",
                this.records[0].getServiceName());

        Assert.assertEquals(
                "PIRSA EarthResource GeoServer WFS",
                this.records[2].getServiceName());
    }

    @Test
    public void testGetServiceUrl() throws XPathExpressionException {

        CSWOnlineResource[] resources = this.records[4].getOnlineResourcesByType(OnlineResourceType.WFS);
        Assert.assertEquals(1, resources.length);
        Assert.assertEquals(
                "http://auscope-services-test.arrc.csiro.au/deegree-wfs/services?",
                resources[0].getLinkage().toString());

        resources = this.records[7].getOnlineResourcesByType(OnlineResourceType.WFS);
        Assert.assertEquals(1, resources.length);
        Assert.assertEquals(
                "http://auscope-services-test.arrc.csiro.au:80/geodesy/wfs?",
                resources[0].getLinkage().toString());
    }

    @Test
    public void testMultipleOnlineResources() throws Exception {
        CSWOnlineResource[] resources = this.records[14].getOnlineResources();
        Assert.assertEquals(2, resources.length);

        resources = this.records[14].getOnlineResourcesByType(OnlineResourceType.WCS);
        Assert.assertEquals(1, resources.length);
        Assert.assertEquals("http://apacsrv6/thredds/wcs/galeon/ocean.nc", resources[0].getLinkage().toString());

        resources = this.records[14].getOnlineResourcesByType(OnlineResourceType.WMS);
        Assert.assertEquals(1, resources.length);
        Assert.assertEquals("http://apacsrv6/thredds/wms/galeon/ocean.nc", resources[0].getLinkage().toString());

        resources = this.records[14].getOnlineResourcesByType();
        Assert.assertEquals(0, resources.length);
        resources = this.records[14].getOnlineResourcesByType(OnlineResourceType.WCS, OnlineResourceType.WMS);
        Assert.assertEquals(2, resources.length);
        resources = this.records[14].getOnlineResourcesByType(OnlineResourceType.WCS, OnlineResourceType.WMS, OnlineResourceType.WFS);
        Assert.assertEquals(2, resources.length);
        resources = this.records[14].getOnlineResourcesByType(OnlineResourceType.Unsupported);
        Assert.assertEquals(0, resources.length);

        Assert.assertTrue(this.records[14].containsAnyOnlineResource(OnlineResourceType.WCS));
        Assert.assertTrue(this.records[14].containsAnyOnlineResource(OnlineResourceType.WMS));
        Assert.assertTrue(this.records[14].containsAnyOnlineResource(OnlineResourceType.WCS, OnlineResourceType.WMS));
        Assert.assertTrue(this.records[14].containsAnyOnlineResource(OnlineResourceType.WCS, OnlineResourceType.WMS, OnlineResourceType.WFS));
        Assert.assertFalse(this.records[14].containsAnyOnlineResource(OnlineResourceType.WFS));
        Assert.assertFalse(this.records[14].containsAnyOnlineResource(OnlineResourceType.WFS, OnlineResourceType.Unsupported));
    }

    @Test
    public void testGeographicBoundingBoxParsing() throws Exception {
    	CSWGeographicElement[] geoEls = this.records[0].getCSWGeographicElements();

    	Assert.assertNotNull(geoEls);
    	Assert.assertEquals(1, geoEls.length);
    	Assert.assertTrue(geoEls[0] instanceof CSWGeographicBoundingBox);

    	CSWGeographicBoundingBox bbox = (CSWGeographicBoundingBox)geoEls[0];

    	Assert.assertEquals(145.00, bbox.getEastBoundLongitude(), 0.001);
    	Assert.assertEquals(143.00, bbox.getWestBoundLongitude(), 0.001);
    	Assert.assertEquals(-35.00, bbox.getNorthBoundLatitude(), 0.001);
    	Assert.assertEquals(-39.00, bbox.getSouthBoundLatitude(), 0.001);
    }

}

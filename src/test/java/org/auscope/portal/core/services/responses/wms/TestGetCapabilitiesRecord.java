package org.auscope.portal.core.services.responses.wms;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Unit tests for GetCapabilitiesRecord.
 *
 * @author Josh Vote
 *
 */
public class TestGetCapabilitiesRecord extends PortalTestClass {
    /**
     * Precision used in the xml file.
     */
    static final double PRECISION = 0.01;

    /**
     * Tests that all relevant information is parsed from the specified WMS GetCapabilities document.
     * @throws IOException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     * @throws XPathExpressionException 
     */
    @Test
    public void testParseWMSDocument() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        //Build our record
        try (final InputStream xmlStream = ResourceUtil
                .loadResourceAsStream("org/auscope/portal/core/test/responses/wms/wmsGetCapabilities.xml")) {
            final GetCapabilitiesRecord rec = new GetCapabilitiesRecord_1_1_1(xmlStream);

            // Test the overall data
            Assert.assertNotNull(rec);
            Assert.assertEquals("wms", rec.getServiceType());
            Assert.assertEquals("Contact Org", rec.getOrganisation());
            Assert.assertEquals("http://my.server/getmap/wms", rec.getMapUrl());
            Assert.assertArrayEquals(new String[] { "image/bmp", "image/jpeg",
                    "image/tiff", }, rec.getGetMapFormats());
            Assert.assertArrayEquals(new String[] { "CRS:84", "EPSG:4326",
                    "EPSG:4283", }, rec.getLayerSRS());

            // Test all child layers
            Assert.assertNotNull(rec.getLayers());
            Assert.assertEquals(5, rec.getLayers().size());

            // Get layer 1 (an 'empty' layer)
            GetCapabilitiesWMSLayerRecord wmsRec = rec.getLayers().get(0);
            Assert.assertEquals("", wmsRec.getName());

            // Test layer 2
            wmsRec = rec.getLayers().get(1);
            Assert.assertEquals("name1", wmsRec.getName());
            Assert.assertEquals("abstract1", wmsRec.getAbstract());
            Assert.assertEquals("title1", wmsRec.getTitle());
            Assert.assertArrayEquals(new String[] { "EPSG:4326", },
                    wmsRec.getChildLayerSRS());
            CSWGeographicBoundingBox bbox = wmsRec.getBoundingBox();
            Assert.assertNotNull(bbox);
            Assert.assertEquals(10.0, bbox.getWestBoundLongitude(), PRECISION);
            Assert.assertEquals(20.0, bbox.getEastBoundLongitude(), PRECISION);
            Assert.assertEquals(30.0, bbox.getSouthBoundLatitude(), PRECISION);
            Assert.assertEquals(40.0, bbox.getNorthBoundLatitude(), PRECISION);

            // Test layer 3
            wmsRec = rec.getLayers().get(2);
            Assert.assertEquals("name2", wmsRec.getName());
            Assert.assertEquals("abstract2", wmsRec.getAbstract());
            Assert.assertEquals("title2", wmsRec.getTitle());
            Assert.assertArrayEquals(new String[] { "EPSG:4283", },
                    wmsRec.getChildLayerSRS());
            bbox = wmsRec.getBoundingBox();
            Assert.assertNotNull(bbox);
            Assert.assertEquals(11.0, bbox.getWestBoundLongitude(), PRECISION);
            Assert.assertEquals(22.0, bbox.getEastBoundLongitude(), PRECISION);
            Assert.assertEquals(33.0, bbox.getSouthBoundLatitude(), PRECISION);
            Assert.assertEquals(44.0, bbox.getNorthBoundLatitude(), PRECISION);

            // Get layer 4 (an 'empty' layer)
            wmsRec = rec.getLayers().get(0);
            Assert.assertEquals("", wmsRec.getName());

            // Test layer 5
            wmsRec = rec.getLayers().get(4);
            Assert.assertEquals("name3", wmsRec.getName());
            Assert.assertEquals("abstract3", wmsRec.getAbstract());
            Assert.assertEquals("title3", wmsRec.getTitle());
            Assert.assertArrayEquals(new String[0], wmsRec.getChildLayerSRS());
            bbox = wmsRec.getBoundingBox();
            Assert.assertNotNull(bbox);
            Assert.assertEquals(1.0, bbox.getWestBoundLongitude(), PRECISION);
            Assert.assertEquals(2.0, bbox.getEastBoundLongitude(), PRECISION);
            Assert.assertEquals(3.0, bbox.getSouthBoundLatitude(), PRECISION);
            Assert.assertEquals(4.0, bbox.getNorthBoundLatitude(), PRECISION);
        }
    }

    @Test
    public void testParseWMS_1_3_0() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        //Build our record
        try (final InputStream xmlStream = ResourceUtil
                .loadResourceAsStream(
                        "org/auscope/portal/core/test/responses/wms/GetCapabilitiesControllerWMSResponse_1_3_0.xml")) {
            final GetCapabilitiesRecord rec = new GetCapabilitiesRecord_1_3_0(xmlStream);

            Assert.assertNotNull(rec);
            Assert.assertEquals("wms", rec.getServiceType());
            Assert.assertEquals("Test Organization", rec.getOrganisation());
            Assert.assertEquals("http://localhost:8080/geoserver/ows?SERVICE=WMS", rec.getMapUrl());
            Assert.assertArrayEquals(new String[] { "image/png",
                    "application/atom+xml",
                    "application/pdf",
                    "application/vnd.google-earth.kml+xml",
                    "application/vnd.google-earth.kml+xml;mode=networklink",
                    "application/vnd.google-earth.kmz",
                    "image/geotiff",
                    "image/geotiff8",
                    "image/gif",
                    "image/jpeg",
                    "image/png8",
                    "image/svg+xml",
                    "image/tiff",
                    "image/tiff8" }, rec.getGetMapFormats());
            Assert.assertEquals(4717, rec.getLayerSRS().length);

            // Test all child layers
            Assert.assertNotNull(rec.getLayers());
            Assert.assertEquals(2, rec.getLayers().size());

            // Get layer 1 (an 'empty' layer)
            GetCapabilitiesWMSLayerRecord wmsRec = rec.getLayers().get(0);
            Assert.assertEquals("", wmsRec.getName());

            // Test layer 2
            wmsRec = rec.getLayers().get(1);
            Assert.assertEquals("gsml:MappedFeature", wmsRec.getName());
            Assert.assertEquals("abstract about MappedFeature", wmsRec.getAbstract());
            Assert.assertEquals("MappedFeature", wmsRec.getTitle());
            Assert.assertArrayEquals(new String[] { "EPSG:4326", "CRS:84" },
                    wmsRec.getChildLayerSRS());
            final CSWGeographicBoundingBox bbox = wmsRec.getBoundingBox();
            Assert.assertNotNull(bbox);
            Assert.assertEquals(-180.00, bbox.getWestBoundLongitude(), PRECISION);
            Assert.assertEquals(180.0, bbox.getEastBoundLongitude(), PRECISION);
            Assert.assertEquals(-90.0, bbox.getSouthBoundLatitude(), PRECISION);
            Assert.assertEquals(90.0, bbox.getNorthBoundLatitude(), PRECISION);

            // Get layer 4 (an 'empty' layer)
            wmsRec = rec.getLayers().get(0);
            Assert.assertEquals("", wmsRec.getName());
        }
    }
}

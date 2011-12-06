package org.auscope.portal.server.domain.ows;

import java.io.InputStream;

import org.auscope.portal.PortalTestClass;
import org.auscope.portal.csw.record.CSWGeographicBoundingBox;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for GetCapabilitiesRecord.
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
     * @throws Exception Test Failure
     */
    @Test
    public void testParseWMSDocument() throws Exception {
        //Build our record
        InputStream xmlStream = this.getClass().getResourceAsStream("/wmsGetCapabilities.xml");
        GetCapabilitiesRecord rec = new GetCapabilitiesRecord(xmlStream);

        //Test the overall data
        Assert.assertNotNull(rec);
        Assert.assertEquals("wms", rec.getServiceType());
        Assert.assertEquals("Contact Org", rec.getOrganisation());
        Assert.assertEquals("http://my.server/getmap/wms", rec.getMapUrl());
        Assert.assertArrayEquals(new String[] {"CRS:84", "EPSG:4326",
                "EPSG:4283", }, rec.getLayerSRS());

        //Test all child layers
        Assert.assertNotNull(rec.getLayers());
        Assert.assertEquals(3, rec.getLayers().size());

        //Test layer 1
        GetCapabilitiesWMSLayerRecord wmsRec = rec.getLayers().get(0);
        Assert.assertEquals("name1", wmsRec.getName());
        Assert.assertEquals("abstract1", wmsRec.getAbstract());
        Assert.assertEquals("title1", wmsRec.getTitle());
        Assert.assertArrayEquals(new String[] {"EPSG:4326", },
                wmsRec.getChildLayerSRS());
        CSWGeographicBoundingBox bbox = wmsRec.getBoundingBox();
        Assert.assertNotNull(bbox);
        Assert.assertEquals(10.0, bbox.getWestBoundLongitude(), PRECISION);
        Assert.assertEquals(20.0, bbox.getEastBoundLongitude(), PRECISION);
        Assert.assertEquals(30.0, bbox.getSouthBoundLatitude(), PRECISION);
        Assert.assertEquals(40.0, bbox.getNorthBoundLatitude(), PRECISION);

        //Test layer 2
        wmsRec = rec.getLayers().get(1);
        Assert.assertEquals("name2", wmsRec.getName());
        Assert.assertEquals("abstract2", wmsRec.getAbstract());
        Assert.assertEquals("title2", wmsRec.getTitle());
        Assert.assertArrayEquals(new String[] {"EPSG:4283", },
                wmsRec.getChildLayerSRS());
        bbox = wmsRec.getBoundingBox();
        Assert.assertNotNull(bbox);
        Assert.assertEquals(11.0, bbox.getWestBoundLongitude(), PRECISION);
        Assert.assertEquals(22.0, bbox.getEastBoundLongitude(), PRECISION);
        Assert.assertEquals(33.0, bbox.getSouthBoundLatitude(), PRECISION);
        Assert.assertEquals(44.0, bbox.getNorthBoundLatitude(), PRECISION);

        //Test layer 3
        wmsRec = rec.getLayers().get(2);
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

package org.auscope.portal.core.services.responses.wcs;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.auscope.portal.core.services.responses.ows.OWSException;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.auscope.portal.core.util.DOMUtil;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class TestDescribeCoverageRecord extends PortalTestClass {

    @Test
    public void parseTest1() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException, DOMException, OWSException, ParseException {
        final String xmlString = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/wcs/DescribeCoverageResponse1.xml");
        final Document doc = DOMUtil.buildDomFromString(xmlString);

        //Check the parsed response contains everything we want
        DescribeCoverageRecord[] records = DescribeCoverageRecord.parseRecords(doc);

        Assert.assertNotNull(records);
        Assert.assertEquals(1, records.length);
        Assert.assertNotNull(records[0]);

        DescribeCoverageRecord record = records[0];

        Assert.assertEquals("label1", record.getLabel());
        Assert.assertEquals("name1", record.getName());
        Assert.assertEquals("description1", record.getDescription());

        Assert.assertEquals(0, record.getNativeCRSs().length);

        RangeSet rangeSet = record.getRangeSet();
        Assert.assertNotNull(rangeSet);

        Assert.assertEquals("rslabel1", rangeSet.getLabel());
        Assert.assertEquals("rsname1", rangeSet.getName());
        Assert.assertEquals("rsdescription1", rangeSet.getDescription());

        Assert.assertEquals(1, rangeSet.getAxisDescriptions().length);
        AxisDescription axisDesc = rangeSet.getAxisDescriptions()[0];

        Assert.assertEquals("adlabel1", axisDesc.getLabel());
        Assert.assertEquals("adname1", axisDesc.getName());
        Assert.assertNull(axisDesc.getDescription());

        Assert.assertEquals(27, axisDesc.getValues().length);
        for (ValueEnumType type : axisDesc.getValues()) {
            Assert.assertEquals("singleValue", type.getType());

            SingleValue sv = (SingleValue) type;

            Double.parseDouble(sv.getValue());
        }

        //This will need to be updated if we add support for more spatial domains
        Assert.assertNotNull(record.getSpatialDomain());
        Assert.assertEquals(2, record.getSpatialDomain().getEnvelopes().length);
        for (SimpleEnvelope env : record.getSpatialDomain().getEnvelopes()) {
            Assert.assertEquals(358.875, env.getEastBoundLongitude(), 0.000001);
            Assert.assertEquals(-89.4375, env.getSouthBoundLatitude(), 0.000001);
            Assert.assertEquals(0.0, env.getWestBoundLongitude(), 0.000001);
            Assert.assertEquals(89.4375, env.getNorthBoundLatitude(), 0.000001);
        }

        Assert.assertArrayEquals(new String[] {"none"}, record.getSupportedInterpolations());

        Assert.assertArrayEquals(new String[] {"GeoTIFF", "GeoTIFF_Float", "NetCDF3"}, record.getSupportedFormats());

        Assert.assertArrayEquals(new String[] {"OGC:CRS84"}, record.getSupportedRequestCRSs());
        Assert.assertArrayEquals(new String[] {"EPSG:4326"}, record.getSupportedResponseCRSs());

        Assert.assertNotNull(record.getSpatialDomain());
        Assert.assertEquals(2, record.getSpatialDomain().getEnvelopes().length);

        Assert.assertEquals(2, record.getTemporalDomain().length);
        for (TemporalDomain temporalDom : record.getTemporalDomain()) {
            Assert.assertEquals("timePosition", temporalDom.getType());
        }

        RectifiedGrid rg = record.getSpatialDomain().getRectifiedGrid();
        Assert.assertNotNull(rg);
        Assert.assertEquals("OGC:CRS84", rg.getSrsName());
        Assert.assertEquals(3, rg.getDimension());
        Assert.assertArrayEquals(new String[] {"x", "y", "z"}, rg.getAxisNames());
        Assert.assertArrayEquals(new double[] {0.0, -89.4375, 100.0}, rg.getOrigin(), 0.001);
        Assert.assertArrayEquals(new double[] {1.125, 0.0, 0.0}, rg.getOffsetVectors()[0], 0.001);
        Assert.assertArrayEquals(new double[] {0.0, 1.125, 0.0}, rg.getOffsetVectors()[1], 0.001);
        Assert.assertArrayEquals(new double[] {0.0, 0.0, 33.333334115835335}, rg.getOffsetVectors()[2], 0.001);
        Assert.assertArrayEquals(new int[] {0, 0, 0}, rg.getEnvelopeLowValues());
        Assert.assertArrayEquals(new int[] {319, 159, 26}, rg.getEnvelopeHighValues());

        SimpleTimePosition tp0 = (SimpleTimePosition) record.getTemporalDomain()[0];
        SimpleTimePosition tp1 = (SimpleTimePosition) record.getTemporalDomain()[1];

        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.set(2005, 4, 10, 12, 34, 56);
        Assert.assertEquals(cal.getTime().toString(), tp0.getTimePosition().toString());
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.set(2006, 4, 10, 12, 34, 56);
        Assert.assertEquals(cal.getTime().toString(), tp1.getTimePosition().toString());

    }

    @Test
    public void parseTest2() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException, DOMException, OWSException, ParseException  {
        final String xmlString = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/wcs/DescribeCoverageResponse2.xml");
        final Document doc = DOMUtil.buildDomFromString(xmlString);

        //Check the parsed response contains everything we want
        DescribeCoverageRecord[] records = DescribeCoverageRecord.parseRecords(doc);

        Assert.assertNotNull(records);
        Assert.assertEquals(1, records.length);
        Assert.assertNotNull(records[0]);

        DescribeCoverageRecord record = records[0];

        Assert.assertEquals("label2", record.getLabel());
        Assert.assertEquals("name2", record.getName());
        Assert.assertEquals("description2", record.getDescription());

        Assert.assertArrayEquals(new String[] {"EPSG:32661", "EPSG:4326"}, record.getNativeCRSs());

        RangeSet rangeSet = record.getRangeSet();
        Assert.assertNotNull(rangeSet);

        Assert.assertEquals("rslabel2", rangeSet.getLabel());
        Assert.assertEquals("rsname2", rangeSet.getName());
        Assert.assertEquals("rsdescription2", rangeSet.getDescription());

        Assert.assertEquals(0, rangeSet.getAxisDescriptions().length);

        //This will need to be updated if we add support for more spatial domains
        Assert.assertNotNull(record.getSpatialDomain());
        Assert.assertEquals(2, record.getSpatialDomain().getEnvelopes().length);

        SimpleEnvelope env0 = record.getSpatialDomain().getEnvelopes()[0];
        Assert.assertEquals(179.982, env0.getEastBoundLongitude(), 0.000001);
        Assert.assertEquals(63.041, env0.getSouthBoundLatitude(), 0.000001);
        Assert.assertEquals(-179.123, env0.getWestBoundLongitude(), 0.000001);
        Assert.assertEquals(82.415, env0.getNorthBoundLatitude(), 0.000001);

        SimpleEnvelope env1 = record.getSpatialDomain().getEnvelopes()[1];
        Assert.assertEquals(2173789.735, env1.getEastBoundLongitude(), 0.00001);
        Assert.assertEquals(-1151631.237, env1.getSouthBoundLatitude(), 0.00001);
        Assert.assertEquals(-825267.555, env1.getWestBoundLongitude(), 0.00001);
        Assert.assertEquals(2041572.863, env1.getNorthBoundLatitude(), 0.00001);

        Assert.assertArrayEquals(new String[] {"nearest neighbor", "bilinear"}, record.getSupportedInterpolations());

        Assert.assertArrayEquals(new String[] {"GeoTIFFFloat32"}, record.getSupportedFormats());

        Assert.assertArrayEquals(new String[] {"EPSG:32661", "EPSG:4326", "EPSG:3408", "EPSG:3410"},
                record.getSupportedRequestCRSs());
        Assert.assertArrayEquals(new String[] {"EPSG:32661", "EPSG:4326", "EPSG:3408", "EPSG:3410"},
                record.getSupportedResponseCRSs());

        Assert.assertNull(record.getTemporalDomain());
    }

    @Test
    public void parseTest3() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException, DOMException, OWSException, ParseException {
        final String xmlString = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/wcs/DescribeCoverageResponse3.xml");
        final Document doc = DOMUtil.buildDomFromString(xmlString);

        //Check the parsed response contains everything we want
        DescribeCoverageRecord[] records = DescribeCoverageRecord.parseRecords(doc);

        Assert.assertNotNull(records);
        Assert.assertEquals(1, records.length);
        Assert.assertNotNull(records[0]);

        DescribeCoverageRecord record = records[0];

        SpatialDomain sd = record.getSpatialDomain();
        Assert.assertNotNull(sd);
        Assert.assertEquals(1, record.getSpatialDomain().getEnvelopes().length);
        SimpleEnvelope env = record.getSpatialDomain().getEnvelopes()[0];
        Assert.assertEquals(153.62049699996703, env.getEastBoundLongitude(), 0.00001);
        Assert.assertEquals(-43.69864800000154, env.getSouthBoundLatitude(), 0.00001);
        Assert.assertEquals(112.87212699999998, env.getWestBoundLongitude(), 0.00001);
        Assert.assertEquals(-8.991703000000008, env.getNorthBoundLatitude(), 0.00001);

    }
}

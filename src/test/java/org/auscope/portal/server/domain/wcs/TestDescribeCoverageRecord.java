package org.auscope.portal.server.domain.wcs;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

public class TestDescribeCoverageRecord {



    @Test
    public void parseTest1() throws Exception {
        final String xmlString = org.auscope.portal.Util.loadXML("src/test/resources/DescribeCoverageResponse1.xml");

        //Check the parsed response contains everything we want
        DescribeCoverageRecord[] records = DescribeCoverageRecord.parseRecords(xmlString);

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

            SingleValue sv = (SingleValue) type;;

            Double.parseDouble(sv.getValue());
        }

        //This will need to be updated if we add support for more spatial domains
        Assert.assertNotNull(record.getSpatialDomain());
        Assert.assertEquals(2, record.getSpatialDomain().length);
        for (SpatialDomain spatialDom : record.getSpatialDomain()) {
            SimpleEnvelope env = (SimpleEnvelope) spatialDom;

            Assert.assertEquals(0.0,env.getEastBoundLongitude(), 0.000001);
            Assert.assertEquals(-89.4375,env.getSouthBoundLatitude(), 0.000001);
            Assert.assertEquals(358.875,env.getWestBoundLongitude(), 0.000001);
            Assert.assertEquals(89.4375,env.getNorthBoundLatitude(), 0.000001);
        }

        Assert.assertArrayEquals(new String[] {"none"}, record.getSupportedInterpolations());

        Assert.assertArrayEquals(new String[] {"GeoTIFF", "GeoTIFF_Float", "NetCDF3"}, record.getSupportedFormats());

        Assert.assertArrayEquals(new String[] {"OGC:CRS84"}, record.getSupportedRequestCRSs());
        Assert.assertArrayEquals(new String[] {"EPSG:4326"}, record.getSupportedResponseCRSs());

        Assert.assertNotNull(record.getSpatialDomain());
        Assert.assertEquals(2, record.getSpatialDomain().length);

        Assert.assertEquals(2, record.getTemporalDomain().length);
        for (TemporalDomain temporalDom : record.getTemporalDomain()) {
            Assert.assertEquals("timePosition", temporalDom.getType());
        }

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
    public void parseTest2() throws Exception {
        final String xmlString = org.auscope.portal.Util.loadXML("src/test/resources/DescribeCoverageResponse2.xml");

        //Check the parsed response contains everything we want
        DescribeCoverageRecord[] records = DescribeCoverageRecord.parseRecords(xmlString);

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
        Assert.assertEquals(2, record.getSpatialDomain().length);

        SimpleEnvelope env0 = (SimpleEnvelope) record.getSpatialDomain()[0];
        Assert.assertEquals(-179.123,env0.getEastBoundLongitude(), 0.000001);
        Assert.assertEquals(63.041,env0.getSouthBoundLatitude(), 0.000001);
        Assert.assertEquals(179.982,env0.getWestBoundLongitude(), 0.000001);
        Assert.assertEquals(82.415,env0.getNorthBoundLatitude(), 0.000001);

        SimpleEnvelope env1 = (SimpleEnvelope) record.getSpatialDomain()[1];
        Assert.assertEquals(-825267.555,env1.getEastBoundLongitude(), 0.00001);
        Assert.assertEquals(-1151631.237,env1.getSouthBoundLatitude(), 0.00001);
        Assert.assertEquals(2173789.735,env1.getWestBoundLongitude(), 0.00001);
        Assert.assertEquals(2041572.863,env1.getNorthBoundLatitude(), 0.00001);

        Assert.assertArrayEquals(new String[] {"nearest neighbor", "bilinear"}, record.getSupportedInterpolations());

        Assert.assertArrayEquals(new String[] {"GeoTIFFFloat32"}, record.getSupportedFormats());

        Assert.assertArrayEquals(new String[] {"EPSG:32661", "EPSG:4326", "EPSG:3408", "EPSG:3410"}, record.getSupportedRequestCRSs());
        Assert.assertArrayEquals(new String[] {"EPSG:32661", "EPSG:4326", "EPSG:3408", "EPSG:3410"}, record.getSupportedResponseCRSs());

        Assert.assertNull(record.getTemporalDomain());
    }
}

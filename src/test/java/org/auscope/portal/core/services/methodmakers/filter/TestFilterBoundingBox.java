package org.auscope.portal.core.services.methodmakers.filter;

import org.auscope.portal.core.server.OgcServiceProviderType;
import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for FilterBoundingBox
 * 
 * @author Josh Vote
 *
 */
public class TestFilterBoundingBox extends PortalTestClass {

    private static void assertBBoxEquals(FilterBoundingBox expected, FilterBoundingBox actual, double delta) {
        if (expected == null || actual == null) {
            return;
        }
        Assert.assertEquals(expected.getBboxSrs(), actual.getBboxSrs());
        Assert.assertEquals(expected.getLowerCornerPoints().length, actual.getLowerCornerPoints().length);
        Assert.assertEquals(expected.getUpperCornerPoints().length, actual.getUpperCornerPoints().length);

        for (int i = 0; i < expected.getLowerCornerPoints().length; i++) {
            Assert.assertEquals(expected.getLowerCornerPoints()[i], actual.getLowerCornerPoints()[i], delta);
        }
        for (int i = 0; i < expected.getUpperCornerPoints().length; i++) {
            Assert.assertEquals(expected.getUpperCornerPoints()[i], actual.getUpperCornerPoints()[i], delta);
        }
    }

    /**
     * Tests parsing a BBox string in a variety of accepted formats
     */
    @Test
    public void testParseBBoxString() {
        FilterBoundingBox expected = new FilterBoundingBox("EPSG:4326", new double[] {110, -47}, new double[] {160, -3});
        String bbox1Json = "{\"crs\":\"EPSG:4326\",\"eastBoundLongitude\":160,\"westBoundLongitude\":110,\"southBoundLatitude\":-47,\"northBoundLatitude\":-3}";
        String bbox2Json = "{\"bboxSrs\":\"EPSG:4326\",\"lowerCornerPoints\":[110,-47],\"upperCornerPoints\":[160,-3]}";

        FilterBoundingBox bbox1 = FilterBoundingBox.attemptParseFromJSON(bbox1Json, OgcServiceProviderType.GeoServer);
        FilterBoundingBox bbox2 = FilterBoundingBox.attemptParseFromJSON(bbox2Json, OgcServiceProviderType.GeoServer);

        assertBBoxEquals(expected, bbox1, 0.001);
        assertBBoxEquals(expected, bbox2, 0.001);
    }

    /**
     * Tests parsing an invalid BBox string results in an error
     */
    @Test
    public void testParseBBoxError() {
        String errorJson = "{\"crs\":\"EPSG:4326\",\"dne\":160,\"westBoundLongitude\":110,\"southBoundLatitude\":-47,\"northBoundLatitude\":-3}";
        Assert.assertNull(FilterBoundingBox.attemptParseFromJSON(errorJson, OgcServiceProviderType.GeoServer));
    }

    /**
     * Tests parsing a BBox string that wraps around anti meridian
     */
    @Test
    public void testWrapAround() {
        FilterBoundingBox expected = new FilterBoundingBox("EPSG:4326", new double[] {160, -47}, new double[] {190, -3});
        String bbox1Json = "{\"crs\":\"EPSG:4326\",\"eastBoundLongitude\":-170,\"westBoundLongitude\":160,\"southBoundLatitude\":-47,\"northBoundLatitude\":-3}";

        FilterBoundingBox bbox1 = FilterBoundingBox.attemptParseFromJSON(bbox1Json, OgcServiceProviderType.GeoServer);

        assertBBoxEquals(expected, bbox1, 0.001);
    }
    
    private StringBuffer json = new StringBuffer();
    private double north = 20.0;
    private double south = -20.0;
    private double east = 40.0;
    private double west = 2.0;
    
    @Before
    public void init() {
        json.append("{");
        json.append("'crs':'EPSG:4326',");
        json.append("'northBoundLatitude':'"+north+"',");
        json.append("'southBoundLatitude':'"+south+"',");
        json.append("'eastBoundLongitude':'"+east+"',");
        json.append("'westBoundLongitude':'"+west+"'");
        json.append("}");
    }
    
    @Test
    public void testAttemptParseFromJSONArcGis() {
        FilterBoundingBox fbb =  FilterBoundingBox.attemptParseFromJSON(json.toString(), OgcServiceProviderType.ArcGis);
        Assert.assertEquals(fbb.getLowerCornerPoints()[0], south, 0.001);
        Assert.assertEquals(fbb.getLowerCornerPoints()[1], west, 0.001);
        Assert.assertEquals(fbb.getUpperCornerPoints()[0],north, 0.001);
        Assert.assertEquals(fbb.getUpperCornerPoints()[1],east, 0.001);
    }
    @Test
    public void testAttemptParseFromJSONGeoserver() {
        FilterBoundingBox fbb =  FilterBoundingBox.attemptParseFromJSON(json.toString(), OgcServiceProviderType.GeoServer);
        Assert.assertEquals(fbb.getLowerCornerPoints()[0], west, 0.001);
        Assert.assertEquals(fbb.getLowerCornerPoints()[1], south, 0.001);
        Assert.assertEquals(fbb.getUpperCornerPoints()[0], east, 0.001);
        Assert.assertEquals(fbb.getUpperCornerPoints()[1], north, 0.001);
    }

    /**
     * Test populating lowerCorner and upperCorner from latitude and longitude locations for Geoserver
     */
    @Test
    public void testParseFromValuesForGeoserver() {
    	FilterBoundingBox bbox1 = FilterBoundingBox.parseFromValues("EPSG:4326", north, south, east, west, OgcServiceProviderType.GeoServer);
    	/*
     * Geoserver 2.15 (https://docs.geoserver.org/maintain/en/user/services/wfs/basics.html#axis-ordering) implements
     * 	    axis order as longitude/latitude (x/y) for EPSG:xxxx;
     * 					  longitude/latitude (x/y) for http://www.opengis.net/gml/srs/epsg.xml#xxxx;
     * 					  latitude/longitude (y/x) for urn:x-ogc:def:crs:EPSG:xxxx. 
     * Geoserver 2.16 (https://docs.geoserver.org/stable/en/user/services/wfs/axis_order.html#wfs-basics-axis) and 
     *    2.17 (https://docs.geoserver.org/latest/en/user/services/wfs/axis_order.html#wfs-1-1-axis-order) implements
     * 		axis order as longitude/latitude (x/y) for EPSG:4326;
     * 				  	  longitude/latitude (x/y) for http://www.opengis.net/gml/srs/epsg.xml#xxxx;
     * 				 	  latitude/longitude (y/x) for urn:x-ogc:def:crs:EPSG:xxxx;
     * 				 	  latitude/longitude (y/x) for urn:ogc:def:crs:EPSG:4326. 
    	 */
        Assert.assertEquals(bbox1.getLowerCornerPoints()[0], west, 0.001);
        Assert.assertEquals(bbox1.getLowerCornerPoints()[1], south, 0.001);
        Assert.assertEquals(bbox1.getUpperCornerPoints()[0], east, 0.001);
        Assert.assertEquals(bbox1.getUpperCornerPoints()[1], north, 0.001);
        
    	
        bbox1 = FilterBoundingBox.parseFromValues("ogc:def:crs:EPSG:4326", north, south, east, west, OgcServiceProviderType.GeoServer);
        Assert.assertEquals(bbox1.getLowerCornerPoints()[0], south, 0.001);
        Assert.assertEquals(bbox1.getLowerCornerPoints()[1], west, 0.001);
        Assert.assertEquals(bbox1.getUpperCornerPoints()[0], north, 0.001);
        Assert.assertEquals(bbox1.getUpperCornerPoints()[1], east, 0.001);
    	
    }
}

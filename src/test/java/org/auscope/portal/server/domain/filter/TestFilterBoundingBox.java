package org.auscope.portal.server.domain.filter;

import junit.framework.Assert;

import org.auscope.portal.PortalTestClass;
import org.junit.Test;

/**
 * Unit tests for FilterBoundingBox
 * @author Josh Vote
 *
 */
public class TestFilterBoundingBox extends PortalTestClass {

    private void assertBBoxEquals(FilterBoundingBox expected, FilterBoundingBox actual, double delta) {
        if (expected == null && actual == null) {
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
     * @throws Exception
     */
    @Test
    public void testParseBBoxString() throws Exception {
        FilterBoundingBox expected = new FilterBoundingBox("EPSG:4326", new double[] {110, -47}, new double[] {160, -3});
        String bbox1Json = "{\"crs\":\"EPSG:4326\",\"eastBoundLongitude\":160,\"westBoundLongitude\":110,\"southBoundLatitude\":-47,\"northBoundLatitude\":-3}";
        String bbox2Json = "{\"bboxSrs\":\"EPSG:4326\",\"lowerCornerPoints\":[110,-47],\"upperCornerPoints\":[160,-3]}";

        FilterBoundingBox bbox1 = FilterBoundingBox.attemptParseFromJSON(bbox1Json);
        FilterBoundingBox bbox2 = FilterBoundingBox.attemptParseFromJSON(bbox2Json);

        assertBBoxEquals(expected, bbox1, 0.001);
        assertBBoxEquals(expected, bbox2, 0.001);
    }

    /**
     * Tests parsing an invalid BBox string results in an error
     * @throws Exception
     */
    @Test
    public void testParseBBoxError() throws Exception {
        String errorJson = "{\"crs\":\"EPSG:4326\",\"dne\":160,\"westBoundLongitude\":110,\"southBoundLatitude\":-47,\"northBoundLatitude\":-3}";
        Assert.assertNull(FilterBoundingBox.attemptParseFromJSON(errorJson));
    }

    /**
     * Tests parsing a BBox string that wraps around anti meridian
     * @throws Exception
     */
    @Test
    public void testWrapAround() throws Exception {
        FilterBoundingBox expected = new FilterBoundingBox("EPSG:4326", new double[] {160, -47}, new double[] {350, -3});
        String bbox1Json = "{\"crs\":\"EPSG:4326\",\"eastBoundLongitude\":-170,\"westBoundLongitude\":160,\"southBoundLatitude\":-47,\"northBoundLatitude\":-3}";

        FilterBoundingBox bbox1 = FilterBoundingBox.attemptParseFromJSON(bbox1Json);

        assertBBoxEquals(expected, bbox1, 0.001);
    }
}

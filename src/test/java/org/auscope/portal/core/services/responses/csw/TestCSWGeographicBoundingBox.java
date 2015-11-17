package org.auscope.portal.core.services.responses.csw;

import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Test;

public class TestCSWGeographicBoundingBox extends PortalTestClass {
    @Test
    public void testIntersects() {
        //Simple intersection cases
        CSWGeographicBoundingBox bbox1 = new CSWGeographicBoundingBox(10, 20, 10, 20);
        CSWGeographicBoundingBox bbox2 = new CSWGeographicBoundingBox(0, 5, 0, 5);
        CSWGeographicBoundingBox bbox3 = new CSWGeographicBoundingBox(11, 19, 0, 5);
        CSWGeographicBoundingBox bbox4 = new CSWGeographicBoundingBox(0, 5, 11, 19);
        CSWGeographicBoundingBox bbox5 = new CSWGeographicBoundingBox(12, 55, 11, 19);

        Assert.assertFalse(bbox1.intersects(bbox2));
        Assert.assertFalse(bbox2.intersects(bbox1));

        Assert.assertFalse(bbox1.intersects(bbox3));
        Assert.assertFalse(bbox3.intersects(bbox1));

        Assert.assertFalse(bbox1.intersects(bbox4));
        Assert.assertFalse(bbox4.intersects(bbox1));

        Assert.assertTrue(bbox1.intersects(bbox5));
        Assert.assertTrue(bbox5.intersects(bbox1));

        //Using international date line
        CSWGeographicBoundingBox dateLineBbox = new CSWGeographicBoundingBox(170, -170, 10, 20);
        CSWGeographicBoundingBox rightSideBbox = new CSWGeographicBoundingBox(-175, -170, 10, 20);
        CSWGeographicBoundingBox leftSideBbox = new CSWGeographicBoundingBox(170, 175, 10, 20);
        CSWGeographicBoundingBox dateLineBbox2 = new CSWGeographicBoundingBox(160, -175, 15, 25);

        Assert.assertFalse(dateLineBbox.intersects(bbox1));
        Assert.assertFalse(bbox1.intersects(dateLineBbox));

        Assert.assertTrue(dateLineBbox.intersects(rightSideBbox));
        Assert.assertTrue(rightSideBbox.intersects(dateLineBbox));

        Assert.assertTrue(dateLineBbox.intersects(leftSideBbox));
        Assert.assertTrue(leftSideBbox.intersects(dateLineBbox));

        Assert.assertFalse(rightSideBbox.intersects(leftSideBbox));
        Assert.assertFalse(leftSideBbox.intersects(rightSideBbox));

        Assert.assertTrue(dateLineBbox.intersects(dateLineBbox2));
        Assert.assertTrue(dateLineBbox2.intersects(dateLineBbox));
    }
}

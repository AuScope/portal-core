package org.auscope.portal.gsml;

import org.auscope.portal.PortalTestClass;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for YilgarnGeochemistryFilter
 */
public class TestYilgarnGeochemistryFilter extends PortalTestClass {
    /**
     * Ensures that specifying a null name will not cause any NPE's
     */
    @Test
    public void testNullName() {
        YilgarnGeochemistryFilter filter = new YilgarnGeochemistryFilter(null);
        FilterBoundingBox bbox = new FilterBoundingBox("srs", new double[] {1,2}, new double[] {3,4});

        Assert.assertNotNull(filter.getFilterStringAllRecords());
        Assert.assertNotNull(filter.getFilterStringBoundingBox(bbox));
    }
}

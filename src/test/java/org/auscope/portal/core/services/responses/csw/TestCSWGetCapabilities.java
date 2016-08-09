package org.auscope.portal.core.services.responses.csw;

import java.io.IOException;

import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestCSWGetCapabilities extends PortalTestClass {
    private CSWGetCapabilities getCapabilities;

    @Before
    public void setUp() throws IOException {
        // load CSW record response document
        this.getCapabilities = new CSWGetCapabilities(
                ResourceUtil.loadResourceAsStream("org/auscope/portal/core/test/responses/csw/cswGetCapabilities.xml"));
    }

    @Test
    public void testGetTitle() {
        Assert.assertTrue(this.getCapabilities.getTitle().equals("Mineral Resources Tasmania"));
    }

}

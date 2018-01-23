package org.auscope.portal.core.services.responses.csw;

import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class TestCSWGetDomainResponse extends PortalTestClass {
    private CSWGetDomainResponse getDomainResponse;

    @Before
    public void setup() throws IOException {
        this.getDomainResponse = new CSWGetDomainResponse(
                ResourceUtil.loadResourceAsStream("org/auscope/portal/core/test/responses/csw/cswDomainResponse.xml"));
    }

    @Test
    public void testGetPropertyName() {
        Assert.assertTrue(this.getDomainResponse.getPropertyName().equals("Subject"));
    }

    @Test
    public void testGetDomainValues() {
        Assert.assertEquals(2, this.getDomainResponse.getDomainValues().size());
    }
}

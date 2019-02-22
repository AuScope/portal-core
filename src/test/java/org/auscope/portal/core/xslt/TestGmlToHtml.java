package org.auscope.portal.core.xslt;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.auscope.portal.core.xslt.GmlToHtml;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestGmlToHtml extends PortalTestClass {

    private GmlToHtml gmlToHtml;

    @Before
    public void setup() {
        this.gmlToHtml = new GmlToHtml();
    }

    /**
     * Ensures the transformation occurs with no errors
     */
    @Test
    public void testNoErrors() throws Exception {
        final String wfs = ResourceUtil.loadResourceAsString("org/auscope/portal/core/erml/mine/mineGetFeatureResponse.xml");
        final String serviceUrl = "http://example.org/wfs";

        final String response = gmlToHtml.convert(wfs, serviceUrl);
        Assert.assertNotNull(response);
        Assert.assertFalse(response.isEmpty());
    }

    //VT: this code is useful for debugging the XSLT engine that portal uses as results varied when I use xmlspy
    //    @Test
    //    public void test() throws Exception {
    //
    //        final InputStream wfs = new FileInputStream(new File("C:\\VPrograms\\test.xml"));
    //
    //        final String response = gmlToHtml.convert(wfs, "http://auscope-services-test.arrc.csiro.au/gsnsw-earthresource/wfs");
    //        System.out.println(response);
    //    }
}

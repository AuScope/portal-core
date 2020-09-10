package org.auscope.portal.core.xslt;

import org.auscope.portal.core.services.namespaces.ErmlNamespaceContext;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.ResourceUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class TestGmlToHtml extends PortalTestClass {

    private GmlToHtml gmlToHtml;

    @Before
    public void setup() {
        this.gmlToHtml = new GmlToHtml();
        // Give it some request context for GmlToHtml convert()
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    /**
     * Ensures the transformation occurs with no errors
     */
    @Test
    public void testNoErrors() throws Exception {
        final String wfs = ResourceUtil.loadResourceAsString("org/auscope/portal/core/erml/mine/mineGetFeatureResponse.xml");
        final String baseUrl = "https://portal.org/api";

        final String response = gmlToHtml.convert(wfs, new ErmlNamespaceContext(), baseUrl);
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

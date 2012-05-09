package org.auscope.portal.server.util;

import java.io.InputStream;

import javax.servlet.ServletContext;

import org.auscope.portal.PortalTestClass;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for GmlToHtml
 * @author Josh Vote
 *
 */
public class TestGmlToHtml extends PortalTestClass {

    private ServletContext mockServletContext = context.mock(ServletContext.class);
    private PortalXSLTTransformer mockTransformer = context.mock(PortalXSLTTransformer.class);
    private InputStream mockInputStream = context.mock(InputStream.class);
    private GmlToHtml gmlToHtml;

    @Before
    public void setup() {
        gmlToHtml = new GmlToHtml(mockTransformer, mockServletContext);
    }

    /**
     * Tests that the transform is setup correctly
     */
    @Test
    public void testTransform() {
        final String inputXml = "<input/>";
        final String outputXml = "<output/>";
        final String serviceUrl = "http://service/wfs";


        context.checking(new Expectations() {{
            oneOf(mockServletContext).getResourceAsStream(GmlToHtml.GML_TO_HTML_XSLT);will(returnValue(mockInputStream));
            oneOf(mockTransformer).convert(with(equal(inputXml)), with(same(mockInputStream)), with(aProperty("serviceURL", serviceUrl)));will(returnValue(outputXml));
        }});

        final String result = gmlToHtml.convert(inputXml, serviceUrl);
        Assert.assertEquals(outputXml, result);
    }

    /**
     * Tests that the transform returns "" on failure
     */
    @Test
    public void testFileDNE() {
        final String inputXml = "<input/>";
        final String outputXml = "";
        final String serviceUrl = "http://service/wfs";


        context.checking(new Expectations() {{
            oneOf(mockServletContext).getResourceAsStream(GmlToHtml.GML_TO_HTML_XSLT);will(returnValue(null));
        }});

        final String result = gmlToHtml.convert(inputXml, serviceUrl);
        Assert.assertEquals(outputXml, result);
    }
}

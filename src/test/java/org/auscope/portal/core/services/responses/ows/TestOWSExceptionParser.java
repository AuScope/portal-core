package org.auscope.portal.core.services.responses.ows;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.auscope.portal.core.util.DOMUtil;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class TestOWSExceptionParser extends PortalTestClass {

    private static void check(String path) throws IOException, ParserConfigurationException, SAXException, OWSException  {
        final String xmlString = ResourceUtil.loadResourceAsString(path);

        Document doc = DOMUtil.buildDomFromString(xmlString);

        //Test both the string and document versions
        OWSExceptionParser.checkForExceptionResponse(xmlString);
        OWSExceptionParser.checkForExceptionResponse(doc);
    }

    @Test(expected = OWSException.class)
    public void testThrowException1() throws IOException, ParserConfigurationException, SAXException, OWSException  {
        check("org/auscope/portal/core/test/responses/ows/OWSExceptionSample1.xml");
    }

    @Test
    public void testDontThrowException() throws IOException, ParserConfigurationException, SAXException, OWSException  {
        check("org/auscope/portal/core/test/responses/wcs/DescribeCoverageResponse1.xml");
        check("org/auscope/portal/core/test/responses/wcs/DescribeCoverageResponse2.xml");
        check("org/auscope/portal/core/test/responses/wfs/GetWFSFeatureCount.xml");
        check("org/auscope/portal/core/test/responses/wfs/EmptyWFSResponse.xml");
    }
}

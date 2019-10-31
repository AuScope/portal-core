package org.auscope.portal.core.services.responses.wcs;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.auscope.portal.core.services.responses.ows.OWSException;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;


public class TestGetCapabilities extends PortalTestClass {

    @Test
    public void parseTest1() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException, DOMException, OWSException, ParseException {
        final InputStream xmlStream = ResourceUtil
                .loadResourceAsStream("org/auscope/portal/core/test/responses/wcs/GetCapabilitiesResponse1.xml");
        GetCapabilitiesRecord_1_0_0 getCapRecord = new GetCapabilitiesRecord_1_0_0(xmlStream);
        
        Map<String, String> capabilities = getCapRecord.getCapabilities();
        CoverageOfferingBrief[] coverageOfferings = getCapRecord.getCoverageOfferingBriefs();

        Assert.assertNotNull(capabilities);
        Assert.assertEquals(3, capabilities.size());
        Assert.assertNotNull(coverageOfferings);
        Assert.assertEquals(3, coverageOfferings.length);
        
        CoverageOfferingBrief cob = coverageOfferings[0];
        Assert.assertEquals("coverage_offering_1", cob.getName());
        Assert.assertEquals("This is coverage offering 1", cob.getDescription());
        Assert.assertEquals("Coverage Offering 1", cob.getLabel());
        
        SimpleEnvelope envelope = cob.getLonLatEnvelope();
        Assert.assertNotNull(envelope);
        Assert.assertEquals("urn:ogc:def:crs:OGC:1.3:CRS84", envelope.getSrsName());
        Assert.assertEquals(180.0, envelope.getEastBoundLongitude(), 0.000001);
        Assert.assertEquals(-180.0, envelope.getWestBoundLongitude(), 0.000001);
        Assert.assertEquals(90.0, envelope.getNorthBoundLatitude(), 0.000001);
        Assert.assertEquals(-90.0, envelope.getSouthBoundLatitude(), 0.000001);
        
        SimpleTimePosition[] timePositions = cob.getTimePositions();
        Assert.assertNotNull(timePositions);
        Assert.assertEquals(2, timePositions.length);
        SimpleTimePosition tp0 = timePositions[0];
        SimpleTimePosition tp1 = timePositions[1];

        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.set(1986, 7, 15, 0, 0, 0);
        Assert.assertEquals(cal.getTime().toString(), tp0.getTimePosition().toString());        
        cal.set(2019, 8, 16, 0, 0, 0);
        Assert.assertEquals(cal.getTime().toString(), tp1.getTimePosition().toString());
    }

}

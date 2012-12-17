package org.auscope.portal.core.services.responses.csw;

import java.util.List;

import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.auscope.portal.core.util.DOMUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for CSWGetRecordResponse
 * @author Josh Vote
 *
 */
public class TestCSWGetRecordResponse extends PortalTestClass  {

    private CSWGetRecordResponse recordResponse;
    private CSWServiceItem origin;

    /**
     * INitialise our recordResponse from the test resource cswRecordResponse.xml
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // load CSW record response document
        Document doc = DOMUtil.buildDomFromStream(ResourceUtil.loadResourceAsStream("org/auscope/portal/core/test/responses/csw/cswRecordResponse.xml"));

        this.origin = new CSWServiceItem("id", "http://test.com", "http://test.com?uuid=%1$s", "title");

        this.recordResponse = new CSWGetRecordResponse(this.origin, doc);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testGetCSWRecords() throws Exception {

        List<CSWRecord> recs = this.recordResponse.getRecords();

        Assert.assertNotNull(recs);
        Assert.assertEquals(15, recs.size());
        Assert.assertEquals(15, recordResponse.getRecordsReturned());
        Assert.assertEquals(30, recordResponse.getRecordsMatched());
        Assert.assertEquals(16, recordResponse.getNextRecord());

        for (int i = 0; i < recs.size(); i++) {
            String expectedInfoUrl = String.format(this.origin.getRecordInformationUrl(), recs.get(i).getFileIdentifier());
            Assert.assertEquals(expectedInfoUrl, recs.get(i).getRecordInfoUrl());
        }
    }

}

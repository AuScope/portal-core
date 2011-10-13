package org.auscope.portal.csw;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.auscope.portal.csw.record.CSWRecord;
import org.auscope.portal.server.web.service.CSWServiceItem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for CSWGetRecordResponse
 * @author Josh Vote
 *
 */
public class TestCSWGetRecordResponse {

    private CSWGetRecordResponse recordResponse;
    private CSWServiceItem origin;

    /**
     * INitialise our recordResponse from the test resource cswRecordResponse.xml
     * @throws Exception
     */
    @Before
    public void setup() throws Exception {

        // load CSW record response document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc =
            builder.parse( "src/test/resources/cswRecordResponse.xml" );
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

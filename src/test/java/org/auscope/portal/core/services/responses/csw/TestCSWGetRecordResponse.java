package org.auscope.portal.core.services.responses.csw;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.auscope.portal.core.util.DOMUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Unit tests for CSWGetRecordResponse
 * 
 * @author Josh Vote
 *
 */
public class TestCSWGetRecordResponse extends PortalTestClass {

    private CSWGetRecordResponse recordResponse;
    private CSWServiceItem origin;

    /**
     * INitialise our recordResponse from the test resource cswRecordResponse.xml
     * @throws SAXException 
     * @throws IOException 
     * @throws ParserConfigurationException 
     * @throws XPathExpressionException 
     */
    @Before
    public void setUp() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        // load CSW record response document
        Document doc = DOMUtil.buildDomFromStream(ResourceUtil
                .loadResourceAsStream("org/auscope/portal/core/test/responses/csw/cswRecordResponse.xml"));

        this.origin = new CSWServiceItem("id", "http://test.com", "http://test.com?uuid=%1$s", "title");

        this.recordResponse = new CSWGetRecordResponse(this.origin, doc);
    }

    /**
     */
    @Test
    public void testGetCSWRecords() {
        String parentFileId = "7c0dcd76-60bd-46d8-ade1-5e83362bb44a";
        List<CSWRecord> recs = this.recordResponse.getRecords();

        Assert.assertNotNull(recs);
        Assert.assertEquals(15, recs.size());
        Assert.assertEquals(15, recordResponse.getRecordsReturned());
        Assert.assertEquals(30, recordResponse.getRecordsMatched());
        Assert.assertEquals(16, recordResponse.getNextRecord());

        for (int i = 0; i < recs.size(); i++) {
            String expectedInfoUrl = String.format(this.origin.getRecordInformationUrl(), recs.get(i)
                    .getFileIdentifier());
            Assert.assertEquals(expectedInfoUrl, recs.get(i).getRecordInfoUrl());

            //Assertion that tests parent child relationship in recordset returned from the parser  
            String fileId = recs.get(i).getFileIdentifier();
            if (fileId.equals(parentFileId)) {
                Assert.assertTrue(recs.get(i).hasChildRecords());
                Assert.assertEquals(2, recs.get(i).getChildRecords().length);
            } else {
                Assert.assertFalse(recs.get(i).hasChildRecords());
            }
        }
    }

}
package org.auscope.portal.csw;

import java.net.MalformedURLException;
import java.net.URL;

import org.auscope.portal.csw.record.CSWOnlineResource;
import org.auscope.portal.csw.record.CSWOnlineResourceImpl;
import org.auscope.portal.csw.record.CSWRecord;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for CSWRecord
 * @author Josh Vote
 *
 */
public class TestCSWRecord {

    /**
     * Tests that containsKeyword returns valid results
     */
    @Test
    public void testContainsKeyword() {
        CSWRecord record = new CSWRecord("serviceName", "fileId", "http://record.info", "Abstract", null, null);

        final String[] descriptiveKeywords = new String[] {"keyword1", "keyworda", "keywordX", null, "", "keyword$"};
        record.setDescriptiveKeywords(descriptiveKeywords);

        for (String kw : descriptiveKeywords) {
            Assert.assertTrue(record.containsKeyword(kw));
        }
    }

    @Test
    public void testContainsAnyOnlineResource() throws MalformedURLException {
        final CSWOnlineResource[] emptyOnlineResources = new CSWOnlineResource[] {};
        final CSWOnlineResource[] nullOnlineResources = null;
        final CSWOnlineResource[] fullOnlineResources = new CSWOnlineResource[] {
                new CSWOnlineResourceImpl(new URL("http://example.com"), "wfs", "or1", "or1"),
                new CSWOnlineResourceImpl(new URL("http://example.com"), "wfs", "or2", "or2"),
                new CSWOnlineResourceImpl(new URL("http://example.com"), "wms", "or3", "or3"),
                null,
                new CSWOnlineResourceImpl(new URL("http://example.com"), "unknown", "or4", "or4"),
        };
        CSWRecord record = new CSWRecord("serviceName", "fileId", "http://record.info", "Abstract", null, null);

        record.setOnlineResources(emptyOnlineResources);
        Assert.assertFalse(record.containsAnyOnlineResource());
        Assert.assertFalse(record.containsAnyOnlineResource(CSWOnlineResource.OnlineResourceType.WCS));
        Assert.assertFalse(record.containsAnyOnlineResource(CSWOnlineResource.OnlineResourceType.WFS));
        Assert.assertFalse(record.containsAnyOnlineResource(CSWOnlineResource.OnlineResourceType.WMS));
        Assert.assertFalse(record.containsAnyOnlineResource(CSWOnlineResource.OnlineResourceType.WMS, CSWOnlineResource.OnlineResourceType.WFS, CSWOnlineResource.OnlineResourceType.WCS));

        record.setOnlineResources(nullOnlineResources);
        Assert.assertFalse(record.containsAnyOnlineResource());
        Assert.assertFalse(record.containsAnyOnlineResource(CSWOnlineResource.OnlineResourceType.WCS));
        Assert.assertFalse(record.containsAnyOnlineResource(CSWOnlineResource.OnlineResourceType.WFS));
        Assert.assertFalse(record.containsAnyOnlineResource(CSWOnlineResource.OnlineResourceType.WMS));
        Assert.assertFalse(record.containsAnyOnlineResource(CSWOnlineResource.OnlineResourceType.WMS, CSWOnlineResource.OnlineResourceType.WFS, CSWOnlineResource.OnlineResourceType.WCS));

        record.setOnlineResources(fullOnlineResources);
        Assert.assertFalse(record.containsAnyOnlineResource());
        Assert.assertFalse(record.containsAnyOnlineResource(CSWOnlineResource.OnlineResourceType.WCS));
        Assert.assertTrue(record.containsAnyOnlineResource(CSWOnlineResource.OnlineResourceType.WFS));
        Assert.assertTrue(record.containsAnyOnlineResource(CSWOnlineResource.OnlineResourceType.WMS));
        Assert.assertTrue(record.containsAnyOnlineResource(CSWOnlineResource.OnlineResourceType.WMS, CSWOnlineResource.OnlineResourceType.WFS, CSWOnlineResource.OnlineResourceType.WCS));
    }

}

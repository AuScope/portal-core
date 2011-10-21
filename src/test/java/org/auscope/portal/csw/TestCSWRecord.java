package org.auscope.portal.csw;

import java.net.MalformedURLException;
import java.net.URL;

import org.auscope.portal.csw.record.AbstractCSWOnlineResource;
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
        final AbstractCSWOnlineResource[] emptyOnlineResources = new AbstractCSWOnlineResource[] {};
        final AbstractCSWOnlineResource[] nullOnlineResources = null;
        final AbstractCSWOnlineResource[] fullOnlineResources = new AbstractCSWOnlineResource[] {
                new CSWOnlineResourceImpl(new URL("http://example.com"), "wfs", "or1", "or1"),
                new CSWOnlineResourceImpl(new URL("http://example.com"), "wfs", "or2", "or2"),
                new CSWOnlineResourceImpl(new URL("http://example.com"), "wms", "or3", "or3"),
                null,
                new CSWOnlineResourceImpl(new URL("http://example.com"), "unknown", "or4", "or4"),
        };
        CSWRecord record = new CSWRecord("serviceName", "fileId", "http://record.info", "Abstract", null, null);

        record.setOnlineResources(emptyOnlineResources);
        Assert.assertFalse(record.containsAnyOnlineResource());
        Assert.assertFalse(record.containsAnyOnlineResource(AbstractCSWOnlineResource.OnlineResourceType.WCS));
        Assert.assertFalse(record.containsAnyOnlineResource(AbstractCSWOnlineResource.OnlineResourceType.WFS));
        Assert.assertFalse(record.containsAnyOnlineResource(AbstractCSWOnlineResource.OnlineResourceType.WMS));
        Assert.assertFalse(record.containsAnyOnlineResource(AbstractCSWOnlineResource.OnlineResourceType.WMS, AbstractCSWOnlineResource.OnlineResourceType.WFS, AbstractCSWOnlineResource.OnlineResourceType.WCS));

        record.setOnlineResources(nullOnlineResources);
        Assert.assertFalse(record.containsAnyOnlineResource());
        Assert.assertFalse(record.containsAnyOnlineResource(AbstractCSWOnlineResource.OnlineResourceType.WCS));
        Assert.assertFalse(record.containsAnyOnlineResource(AbstractCSWOnlineResource.OnlineResourceType.WFS));
        Assert.assertFalse(record.containsAnyOnlineResource(AbstractCSWOnlineResource.OnlineResourceType.WMS));
        Assert.assertFalse(record.containsAnyOnlineResource(AbstractCSWOnlineResource.OnlineResourceType.WMS, AbstractCSWOnlineResource.OnlineResourceType.WFS, AbstractCSWOnlineResource.OnlineResourceType.WCS));

        record.setOnlineResources(fullOnlineResources);
        Assert.assertFalse(record.containsAnyOnlineResource());
        Assert.assertFalse(record.containsAnyOnlineResource(AbstractCSWOnlineResource.OnlineResourceType.WCS));
        Assert.assertTrue(record.containsAnyOnlineResource(AbstractCSWOnlineResource.OnlineResourceType.WFS));
        Assert.assertTrue(record.containsAnyOnlineResource(AbstractCSWOnlineResource.OnlineResourceType.WMS));
        Assert.assertTrue(record.containsAnyOnlineResource(AbstractCSWOnlineResource.OnlineResourceType.WMS, AbstractCSWOnlineResource.OnlineResourceType.WFS, AbstractCSWOnlineResource.OnlineResourceType.WCS));
    }

}

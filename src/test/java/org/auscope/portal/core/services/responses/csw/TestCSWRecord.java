package org.auscope.portal.core.services.responses.csw;

import java.net.MalformedURLException;
import java.net.URL;

import org.auscope.portal.core.services.csw.CSWRecordsHostFilter;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource.OnlineResourceType;
import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for CSWRecord
 * 
 * @author Josh Vote
 *
 */
public class TestCSWRecord extends PortalTestClass {

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
        Assert.assertFalse(record.containsAnyOnlineResource(AbstractCSWOnlineResource.OnlineResourceType.WMS,
                AbstractCSWOnlineResource.OnlineResourceType.WFS, AbstractCSWOnlineResource.OnlineResourceType.WCS));

        record.setOnlineResources(nullOnlineResources);
        Assert.assertFalse(record.containsAnyOnlineResource());
        Assert.assertFalse(record.containsAnyOnlineResource(AbstractCSWOnlineResource.OnlineResourceType.WCS));
        Assert.assertFalse(record.containsAnyOnlineResource(AbstractCSWOnlineResource.OnlineResourceType.WFS));
        Assert.assertFalse(record.containsAnyOnlineResource(AbstractCSWOnlineResource.OnlineResourceType.WMS));
        Assert.assertFalse(record.containsAnyOnlineResource(AbstractCSWOnlineResource.OnlineResourceType.WMS,
                AbstractCSWOnlineResource.OnlineResourceType.WFS, AbstractCSWOnlineResource.OnlineResourceType.WCS));

        record.setOnlineResources(fullOnlineResources);
        Assert.assertFalse(record.containsAnyOnlineResource());
        Assert.assertFalse(record.containsAnyOnlineResource(AbstractCSWOnlineResource.OnlineResourceType.WCS));
        Assert.assertTrue(record.containsAnyOnlineResource(AbstractCSWOnlineResource.OnlineResourceType.WFS));
        Assert.assertTrue(record.containsAnyOnlineResource(AbstractCSWOnlineResource.OnlineResourceType.WMS));
        Assert.assertTrue(record.containsAnyOnlineResource(AbstractCSWOnlineResource.OnlineResourceType.WMS,
                AbstractCSWOnlineResource.OnlineResourceType.WFS, AbstractCSWOnlineResource.OnlineResourceType.WCS));
    }

    @Test
    public void testGetOnlineResourcesByType() throws MalformedURLException {
        final AbstractCSWOnlineResource[] fullOnlineResources = new AbstractCSWOnlineResource[] {
                new CSWOnlineResourceImpl(new URL("http://example.com/test2"), "wfs", "or1", "or1"),
                new CSWOnlineResourceImpl(new URL("http://example2.com/test3"), "wfs", "or2", "or2"),
                new CSWOnlineResourceImpl(new URL("http://example2.com/test4"), "wms", "or3", "or3"),
                new CSWOnlineResourceImpl(new URL("http://example2.com/test4"), "wms", "or4", "or4"),
                null,
                new CSWOnlineResourceImpl(new URL("http://example.com"), "unknown", "or4", "or4"),
        };
        CSWRecord record = new CSWRecord("serviceName", "fileId", "http://record.info", "Abstract", null, null);
        record.setOnlineResources(fullOnlineResources);

        AbstractCSWOnlineResource[] result = record.getOnlineResourcesByType(OnlineResourceType.WFS);
        Assert.assertEquals(2, result.length);

        result = record
                .getOnlineResourcesByType(new CSWRecordsHostFilter("http://example.com"), OnlineResourceType.WFS);
        Assert.assertEquals(1, result.length);

        result = record.getOnlineResourcesByType(new CSWRecordsHostFilter("http://example2.com"),
                OnlineResourceType.WMS);
        Assert.assertEquals(2, result.length);

        result = record.getOnlineResourcesByType(new CSWRecordsHostFilter("http://example2.com"),
                OnlineResourceType.WFS);
        Assert.assertEquals(1, result.length);
    }

}

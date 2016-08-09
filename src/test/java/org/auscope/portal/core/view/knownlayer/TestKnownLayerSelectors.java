package org.auscope.portal.core.view.knownlayer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.auscope.portal.core.services.responses.csw.CSWOnlineResourceImpl;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.view.knownlayer.KnownLayerSelector.RelationType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the various KnownLayerSelector implementations
 * 
 * @author Josh Vote
 */
public class TestKnownLayerSelectors extends PortalTestClass {

    private List<CSWRecord> recordList;

    /**
     * Loads record list with a set of 5 example CSW records
     * @throws MalformedURLException 
     */
    @Before
    public void setupRecordList() throws MalformedURLException {
        recordList = new ArrayList<>();

        //WWW:LINK-1.0-http--link
        //OGC:WFS-1.0.0-http-get-feature
        //OGC:WMS-1.1.1-http-get-map

        //The first record is a simple WFS
        CSWOnlineResourceImpl[] resources = new CSWOnlineResourceImpl[] {
                new CSWOnlineResourceImpl(new URL("http://test.url1/wfs"), "OGC:WFS-1.0.0-http-get-feature",
                        "type:name1", "")
        };
        CSWRecord rec = new CSWRecord("name1", "id1", "", "", resources, null);
        recordList.add(rec);

        //The second record is the same as the first but with a different URL
        resources = new CSWOnlineResourceImpl[] {
                new CSWOnlineResourceImpl(new URL("http://test.url2/wfs"), "OGC:WFS-1.0.0-http-get-feature",
                        "type:name1", "")
        };
        rec = new CSWRecord("name2", "id2", "", "", resources, null);
        recordList.add(rec);

        //The third record is a WMS (same name as above)
        resources = new CSWOnlineResourceImpl[] {
                new CSWOnlineResourceImpl(new URL("http://test.url3/wms"), "OGC:WMS-1.1.1-http-get-map", "type:name1",
                        "")
        };
        rec = new CSWRecord("name3", "id3", "", "", resources, null);
        rec.setDescriptiveKeywords(new String[] {"WMS", "Keyword"});
        recordList.add(rec);

        //The fourth record is a WWW link
        resources = new CSWOnlineResourceImpl[] {
                new CSWOnlineResourceImpl(new URL("http://test.url4"), "WWW:LINK-1.0-http--link", "Web Link Name", "")
        };
        rec = new CSWRecord("name4", "id4", "", "", resources, null);
        rec.setDescriptiveKeywords(new String[] {"keyword", "Report", "anotherKeyword"});
        recordList.add(rec);

        //The fifth record is another WFS with a different type name (but same URL as rec 1)
        resources = new CSWOnlineResourceImpl[] {
                new CSWOnlineResourceImpl(new URL("http://test.url1/wfs"), "OGC:WFS-1.0.0-http-get-feature",
                        "type:name2", "")
        };
        rec = new CSWRecord("name5", "id5", "", "", resources, null);
        recordList.add(rec);

        //The sixth record is a WMS (new type name, same URL as #3)
        resources = new CSWOnlineResourceImpl[] {
                new CSWOnlineResourceImpl(new URL("http://test.url3/wms"), "OGC:WMS-1.1.1-http-get-map", "type:name2",
                        "")
        };
        rec = new CSWRecord("name6", "id6", "", "", resources, null);
        recordList.add(rec);
    }

    /**
     * Asserts the keyword selector correctly picks records based on descriptive keywords
     */
    @Test
    public void testCSWKeywordSelector() {
        CSWRecordSelector selector = new CSWRecordSelector();
        selector.setDescriptiveKeyword("Report");

        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(0)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(1)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(2)));
        Assert.assertEquals(RelationType.Belongs, selector.isRelatedRecord(recordList.get(3)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(4)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(5)));
    }

    /**
     * Asserts the keyword selector correctly picks records based on descriptive keywords
     */
    @Test
    public void testCSWIdSelector() {
        CSWRecordSelector selector = new CSWRecordSelector();
        selector.setRecordId("id4");

        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(0)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(1)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(2)));
        Assert.assertEquals(RelationType.Belongs, selector.isRelatedRecord(recordList.get(3)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(4)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(5)));
    }

    /**
     * Asserts the WMS selector correctly picks records based on layer names
     */
    @Test
    public void testWMSSelector() {
        WMSSelector selector = new WMSSelector("type:name1");
        selector.setRelatedLayerNames(new String[] {"type:name2"});

        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(0)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(1)));
        Assert.assertEquals(RelationType.Belongs, selector.isRelatedRecord(recordList.get(2)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(3)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(4)));
        Assert.assertEquals(RelationType.Related, selector.isRelatedRecord(recordList.get(5)));
    }

    /**
     * Asserts the WFS selector correctly picks records based on type names
     */
    @Test
    public void testWFSSelector() {
        WFSSelector selector = new WFSSelector("type:name1");

        Assert.assertEquals(RelationType.Belongs, selector.isRelatedRecord(recordList.get(0)));
        Assert.assertEquals(RelationType.Belongs, selector.isRelatedRecord(recordList.get(1)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(2)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(3)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(4)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(5)));
    }

    /**
     * Asserts the WFS selector correctly picks records based on type names + related types
     */
    @Test
    public void testWFSSelectorWithRelations() {
        WFSSelector selector = new WFSSelector("type:name1");
        selector.setRelatedFeatureTypeNames(new String[] {"type:name2"});

        Assert.assertEquals(RelationType.Belongs, selector.isRelatedRecord(recordList.get(0)));
        Assert.assertEquals(RelationType.Belongs, selector.isRelatedRecord(recordList.get(1)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(2)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(3)));
        Assert.assertEquals(RelationType.Related, selector.isRelatedRecord(recordList.get(4)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(5)));
    }

    /**
     * Asserts the WFS selector correctly picks records based on type names + URL restrictions
     */
    @Test
    public void testWFSSelectorURLRestrictions() {
        WFSSelector selector = new WFSSelector("type:name1", new String[] {"http://test.url2/wfs"}, false);

        Assert.assertEquals(RelationType.Belongs, selector.isRelatedRecord(recordList.get(0)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(1)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(2)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(3)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(4)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(5)));

        selector = new WFSSelector("type:name1", new String[] {"http://test.url2/wfs"}, true);

        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(0)));
        Assert.assertEquals(RelationType.Belongs, selector.isRelatedRecord(recordList.get(1)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(2)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(3)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(4)));
        Assert.assertEquals(RelationType.NotRelated, selector.isRelatedRecord(recordList.get(5)));
    }

}

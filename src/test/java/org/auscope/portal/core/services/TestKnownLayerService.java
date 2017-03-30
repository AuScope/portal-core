package org.auscope.portal.core.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.view.knownlayer.KnownLayer;
import org.auscope.portal.core.view.knownlayer.KnownLayerAndRecords;
import org.auscope.portal.core.view.knownlayer.KnownLayerGrouping;
import org.auscope.portal.core.view.knownlayer.KnownLayerSelector;
import org.auscope.portal.core.view.knownlayer.KnownLayerSelector.RelationType;
import org.jmock.Expectations;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for KnownLayerService
 * 
 * @author Josh Vote
 *
 */
public class TestKnownLayerService extends PortalTestClass {

    /**
     * This fake class is used to test aspects of the KnownLayerService where the sought behaviour requires a descendant of KnownLayer.
     * 
     * @author bro879
     *
     */
    class FakeKnownLayerChild extends KnownLayer {
        private static final long serialVersionUID = -8469962130513009451L;

        public FakeKnownLayerChild(String id, KnownLayerSelector knownLayerSelector) {
            super(id, knownLayerSelector);
        }
    }

    private ArrayList<CSWRecord> cswRecordList; //list of 3 mock CSWRecord objects
    private KnownLayerSelector mockSelector1;
    private KnownLayerSelector mockSelector2;
    private KnownLayerSelector mockSelector3;
    @SuppressWarnings("rawtypes")
    private ArrayList mockKnownLayerList;
    private CSWCacheService mockCacheService;
    private KnownLayerService knownLayerService;

    /**
     * Build our mock layer list
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Before
    public void setup() {
        mockSelector1 = context.mock(KnownLayerSelector.class, "knownLayerSelector1");
        mockSelector2 = context.mock(KnownLayerSelector.class, "knownLayerSelector2");
        mockSelector3 = context.mock(KnownLayerSelector.class, "knownLayerSelector3");

        mockKnownLayerList = new ArrayList();
        mockKnownLayerList.add(new KnownLayer("id1", mockSelector1));
        mockKnownLayerList.add(new KnownLayer("id2", mockSelector2));
        mockKnownLayerList.add(new FakeKnownLayerChild("id3", mockSelector3));

        cswRecordList = new ArrayList<>();
        cswRecordList.add(context.mock(CSWRecord.class, "mockRecord1"));
        cswRecordList.add(context.mock(CSWRecord.class, "mockRecord2"));
        cswRecordList.add(context.mock(CSWRecord.class, "mockRecord3"));

        context.checking(new Expectations() {
            {
                allowing(cswRecordList.get(0)).getFileIdentifier();
                will(returnValue("id1"));
                allowing(cswRecordList.get(1)).getFileIdentifier();
                will(returnValue("id2"));
                allowing(cswRecordList.get(2)).getFileIdentifier();
                will(returnValue("id3"));

                allowing(cswRecordList.get(0)).getOnlineResources();
                will(returnValue(null));
                allowing(cswRecordList.get(1)).getOnlineResources();
                will(returnValue(null));
                allowing(cswRecordList.get(2)).getOnlineResources();
                will(returnValue(null));
            }
        });

        mockCacheService = context.mock(CSWCacheService.class);
        knownLayerService = new KnownLayerService(mockKnownLayerList, mockCacheService);
    }

    @After
    public void tearDown() {
        cswRecordList = null;
        mockSelector1 = null;
        mockSelector2 = null;
        mockSelector3 = null;
        mockKnownLayerList = null;
        mockCacheService = null;
        knownLayerService = null;
    }

    /**
     * Asserts that every element in actual is also in expected and that they both have the same number of elements
     *
     * The ordering of either list is NOT taken into account
     */
    private static void assertListContentsSame(List<?> expected, List<?> actual) {
        Assert.assertEquals(expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            boolean match = false;
            Object comparison = expected.get(i);

            for (int j = 0; j < actual.size() && !match; j++) {
                match = comparison == actual.get(j); //reference comparison
            }

            Assert.assertTrue(String.format("No match found for %1$s", comparison), match);
        }
    }

    /**
     * Tests that the basic grouping works
     */
    @Test
    public void testBasicGrouping() {
        context.checking(new Expectations() {
            {
                oneOf(mockCacheService).getRecordCache();
                will(returnValue(cswRecordList));

                oneOf(mockSelector1).isRelatedRecord(cswRecordList.get(0));
                will(returnValue(RelationType.Belongs));
                oneOf(mockSelector1).isRelatedRecord(cswRecordList.get(1));
                will(returnValue(RelationType.NotRelated));
                oneOf(mockSelector1).isRelatedRecord(cswRecordList.get(2));
                will(returnValue(RelationType.NotRelated));

                oneOf(mockSelector2).isRelatedRecord(cswRecordList.get(0));
                will(returnValue(RelationType.Related));
                oneOf(mockSelector2).isRelatedRecord(cswRecordList.get(1));
                will(returnValue(RelationType.Related));
                oneOf(mockSelector2).isRelatedRecord(cswRecordList.get(2));
                will(returnValue(RelationType.NotRelated));

                oneOf(mockSelector3).isRelatedRecord(cswRecordList.get(0));
                will(returnValue(RelationType.NotRelated));
                oneOf(mockSelector3).isRelatedRecord(cswRecordList.get(1));
                will(returnValue(RelationType.NotRelated));
                oneOf(mockSelector3).isRelatedRecord(cswRecordList.get(2));
                will(returnValue(RelationType.NotRelated));

                allowing(cswRecordList.get(0)).hasNamedOnlineResources();
                will(returnValue(true));
                allowing(cswRecordList.get(1)).hasNamedOnlineResources();
                will(returnValue(true));
                allowing(cswRecordList.get(2)).hasNamedOnlineResources();
                will(returnValue(true));
            }
        });

        KnownLayerGrouping grouping = knownLayerService.groupKnownLayerRecords();
        Assert.assertNotNull(grouping);
        Assert.assertNotNull(grouping.getKnownLayers());
        Assert.assertNotNull(grouping.getOriginalRecordSet());
        Assert.assertNotNull(grouping.getUnmappedRecords());

        //Interrogate our response
        List<KnownLayerAndRecords> groups = grouping.getKnownLayers();

        // Only the actual KnownLayers should be returned; not any derived types.
        Assert.assertEquals(2, groups.size());

        //Results of mockSelector1
        Assert.assertSame(mockKnownLayerList.get(0), groups.get(0).getKnownLayer());
        assertListContentsSame(Arrays.asList(cswRecordList.get(0)), groups.get(0).getBelongingRecords());
        assertListContentsSame(Arrays.asList(), groups.get(0).getRelatedRecords());

        //Results of mockSelector2
        Assert.assertSame(mockKnownLayerList.get(1), groups.get(1).getKnownLayer());
        assertListContentsSame(Arrays.asList(), groups.get(1).getBelongingRecords());
        assertListContentsSame(Arrays.asList(cswRecordList.get(0), cswRecordList.get(1)), groups.get(1)
                .getRelatedRecords());

        assertListContentsSame(cswRecordList, grouping.getOriginalRecordSet());
        assertListContentsSame(Arrays.asList(cswRecordList.get(2)), grouping.getUnmappedRecords());
    }

    @Test
    public void groupKnownLayerRecords_FakeKnownLayerChild_ReturnsOneFakeKnownLayer() {
        // Arrange
        context.checking(new Expectations() {
            {
                oneOf(mockCacheService).getRecordCache();
                will(returnValue(cswRecordList));

                oneOf(mockSelector1).isRelatedRecord(cswRecordList.get(0));
                will(returnValue(RelationType.NotRelated));
                oneOf(mockSelector1).isRelatedRecord(cswRecordList.get(1));
                will(returnValue(RelationType.NotRelated));
                oneOf(mockSelector1).isRelatedRecord(cswRecordList.get(2));
                will(returnValue(RelationType.NotRelated));

                oneOf(mockSelector2).isRelatedRecord(cswRecordList.get(0));
                will(returnValue(RelationType.NotRelated));
                oneOf(mockSelector2).isRelatedRecord(cswRecordList.get(1));
                will(returnValue(RelationType.NotRelated));
                oneOf(mockSelector2).isRelatedRecord(cswRecordList.get(2));
                will(returnValue(RelationType.NotRelated));

                oneOf(mockSelector3).isRelatedRecord(cswRecordList.get(0));
                will(returnValue(RelationType.Belongs));
                oneOf(mockSelector3).isRelatedRecord(cswRecordList.get(1));
                will(returnValue(RelationType.NotRelated));
                oneOf(mockSelector3).isRelatedRecord(cswRecordList.get(2));
                will(returnValue(RelationType.NotRelated));

                allowing(cswRecordList.get(0)).hasNamedOnlineResources();
                will(returnValue(true));
                allowing(cswRecordList.get(1)).hasNamedOnlineResources();
                will(returnValue(true));
                allowing(cswRecordList.get(2)).hasNamedOnlineResources();
                will(returnValue(true));
            }
        });

        // Act
        List<KnownLayerAndRecords> groups =
                knownLayerService.groupKnownLayerRecords(FakeKnownLayerChild.class).getKnownLayers();

        // Assert
        Assert.assertEquals(1, groups.size());
        Assert.assertTrue(groups.get(0).getKnownLayer() instanceof FakeKnownLayerChild);
    }
    
    @Test
    public void groupKnownLayerRecords_avoids_metadatacsw_records() {
        context.checking(new Expectations() {
            {
                oneOf(mockCacheService).getRecordCache();
                will(returnValue(cswRecordList));

                oneOf(mockSelector1).isRelatedRecord(cswRecordList.get(0));
                will(returnValue(RelationType.NotRelated));
                oneOf(mockSelector1).isRelatedRecord(cswRecordList.get(1));
                will(returnValue(RelationType.NotRelated));
                oneOf(mockSelector1).isRelatedRecord(cswRecordList.get(2));
                will(returnValue(RelationType.NotRelated));

                oneOf(mockSelector2).isRelatedRecord(cswRecordList.get(0));
                will(returnValue(RelationType.NotRelated));
                oneOf(mockSelector2).isRelatedRecord(cswRecordList.get(1));
                will(returnValue(RelationType.NotRelated));
                oneOf(mockSelector2).isRelatedRecord(cswRecordList.get(2));
                will(returnValue(RelationType.NotRelated));

                oneOf(mockSelector3).isRelatedRecord(cswRecordList.get(0));
                will(returnValue(RelationType.NotRelated));
                oneOf(mockSelector3).isRelatedRecord(cswRecordList.get(1));
                will(returnValue(RelationType.NotRelated));
                oneOf(mockSelector3).isRelatedRecord(cswRecordList.get(2));
                will(returnValue(RelationType.NotRelated));

                allowing(cswRecordList.get(0)).hasNamedOnlineResources();
                will(returnValue(false));
                allowing(cswRecordList.get(0)).hasGeographicElements();
                will(returnValue(false));
                allowing(cswRecordList.get(1)).hasNamedOnlineResources();
                will(returnValue(true));
                allowing(cswRecordList.get(1)).hasGeographicElements();
                will(returnValue(false));
                allowing(cswRecordList.get(2)).hasNamedOnlineResources();
                will(returnValue(false));
                allowing(cswRecordList.get(2)).hasGeographicElements();
                will(returnValue(true));
            }
        });

        // Start execution
        KnownLayerGrouping grouping = knownLayerService.groupKnownLayerRecords();

        // Test the results
        Assert.assertNotNull(grouping);
        Assert.assertNotNull(grouping.getKnownLayers());
        Assert.assertNotNull(grouping.getOriginalRecordSet());
        Assert.assertNotNull(grouping.getUnmappedRecords());

        List<KnownLayerAndRecords> groups = grouping.getKnownLayers();
        Assert.assertEquals(2, groups.size());
        Assert.assertEquals(0, groups.get(0).getBelongingRecords().size());
        Assert.assertEquals(0, groups.get(0).getRelatedRecords().size());
        Assert.assertEquals(0, groups.get(1).getBelongingRecords().size());
        Assert.assertEquals(0, groups.get(1).getRelatedRecords().size());

        List<CSWRecord> unmappedCSWList = grouping.getUnmappedRecords();
        Assert.assertEquals(2, unmappedCSWList.size());

        assertListContentsSame(cswRecordList, grouping.getOriginalRecordSet());
        Assert.assertEquals(cswRecordList.get(1), unmappedCSWList.get(0));
        Assert.assertEquals(cswRecordList.get(2), unmappedCSWList.get(1));
    }
}

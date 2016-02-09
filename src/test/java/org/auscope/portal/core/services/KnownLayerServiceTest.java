/**
 * 
 */
package org.auscope.portal.core.services;

import java.util.ArrayList;
import java.util.List;

import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.view.knownlayer.KnownLayer;
import org.auscope.portal.core.view.knownlayer.SelectorsMode;
import org.auscope.portal.core.view.knownlayer.WMSSelector;
import org.auscope.portal.core.view.knownlayer.WMSSelectors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * Tests for the ordering of the known layers in the UI - should match the order in the configuration.
 * @author u86990
 */
public class KnownLayerServiceTest {
    private KnownLayerService knownLayerService;

    private List<CSWRecord> cswRecordListToAddTo = new ArrayList<>();
    // private CSWRecord cswRecord;
    private KnownLayer knownLayer;
    private WMSSelectors wmsSelectors;
    private String layerName1 = "layerName1";
    private String layerName2 = "layerName2";
    private String layerName3 = "layerName3";
    private List<String> layerNames;

    @Before
    public void init() {
        layerName1 = "layerName1";
        layerName2 = "layerName2";
        layerName3 = "layerName3";
        layerNames = Lists.newArrayList(layerName1, layerName2, layerName3);
        wmsSelectors = new WMSSelectors(SelectorsMode.AND, layerNames);
    }

    /**
     * @param knownLayerId
     * @return new test KnownLayerService
     */
    private KnownLayerService buildKnownLayerServiceWithWMSSelectorsAndKnownLayerIdOf(String knownLayerId) {
        knownLayer = new KnownLayer(knownLayerId, wmsSelectors);

        knownLayerService = new KnownLayerService(Lists.newArrayList(knownLayer), null);
        return knownLayerService;
    }

    private KnownLayerService buildKnownLayerServiceWithTheseWMSSelectorItemsAndKnownLayerIdOf(String knownLayerId,
            WMSSelector... wMSSelectors) {
        List<KnownLayer> knownLayersListLocal = new ArrayList<>();

        for (WMSSelector selector : wMSSelectors) {
            KnownLayer knownLayerLocal = new KnownLayer(knownLayerId, selector);
            knownLayersListLocal.add(knownLayerLocal);
        }
        knownLayerService = new KnownLayerService(knownLayersListLocal, null);
        knownLayer = knownLayersListLocal.get(0);

        return knownLayerService;
    }

    /**
     * Test method for
     * {@link org.auscope.portal.core.services.KnownLayerService#addToListConsiderWMSSelectors(java.util.List, org.auscope.portal.core.services.responses.csw.CSWRecord, org.auscope.portal.core.view.knownlayer.KnownLayer)}
     * 
     * This should be an exception as the knownLayer contains a WMSSelectors instanceof KnownLayer 
     * but the cswRecord.getServiceName ('unknownLayerName') is not one of the layerNames in the knownLayer
     * 
     * @throws PortalServiceException
     */
    @Test(expected = PortalServiceException.class)
    public void testAddToListConsiderWMSSelectorsDoesntContainRecordName() throws PortalServiceException {
        CSWRecord cswRecord = new CSWRecord("serviceName", "idA", "infoUrlA", "dataIdAbstractA", null, null, "unknownLayerName");

        knownLayerService = buildKnownLayerServiceWithWMSSelectorsAndKnownLayerIdOf("thisDoesntExistInWMSSelectors");
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord, knownLayer);
        Assert.assertNotEquals(0, cswRecordListToAddTo.size());
    }

    /**
     * This should work as the knownLayer contains a WMSSelectors instanceof KnownLayer 
     * and the cswRecord.getServiceName (layerName1) is one of the layerNames in the knownLayer
     */
    @Test
    public void testAddToListConsiderWMSSelectorsContainsRecordName() throws PortalServiceException {
        CSWRecord cswRecord = new CSWRecord("serviceName", "idA", "infoUrlA", "dataIdAbstractA", null, null, "layerName1");

        knownLayerService = buildKnownLayerServiceWithWMSSelectorsAndKnownLayerIdOf("someid");
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord, knownLayer);

        int requiredSize = ((WMSSelectors) knownLayer.getKnownLayerSelector()).getWmsSelectors().size();
        int nullsAfterIndex = 0;

        Assert.assertEquals(requiredSize, cswRecordListToAddTo.size());
        checkCorrectSize(cswRecordListToAddTo, nullsAfterIndex);
    }

    /**
     * @param cswRecordListToAddTo2
     * @param nullsAfterIndex
     */
    private void checkCorrectSize(List<CSWRecord> list, int nullsAfterIndex) {
        if (nullsAfterIndex >= list.size()) {
            Assert.assertTrue(true);
        }
        Assert.assertEquals(null, cswRecordListToAddTo.get(nullsAfterIndex + 1));
    }

    /**
     * This will work as the knownLayer contains a WMSSelectors instanceof KnownLayer and the cswRecord.getServiceName (layerName1, layerName2) are the
     * layerNames in the knownLayer
     */
    @Test
    public void testAddToListConsiderWMSSelectorsContainsRecordNameTwoRecords() throws PortalServiceException {
        CSWRecord cswRecord1 = new CSWRecord("serviceName", "idA", "infoUrlA", "dataIdAbstractA", null, null, layerName1);
        CSWRecord cswRecord2 = new CSWRecord("serviceName", "idA", "infoUrlA", "dataIdAbstractA", null, null, layerName2);

        knownLayerService = buildKnownLayerServiceWithWMSSelectorsAndKnownLayerIdOf("someid");
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord1, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord2, knownLayer);

        int requiredSize = ((WMSSelectors) knownLayer.getKnownLayerSelector()).getWmsSelectors().size();
        int nullsAfterIndex = 1;

        Assert.assertEquals(requiredSize, cswRecordListToAddTo.size());
        checkCorrectSize(cswRecordListToAddTo, nullsAfterIndex);
    }

    /**
     * This will work as the knownLayer contains a WMSSelectors instanceof KnownLayer and the cswRecord.getServiceName (layerName1, layerName2) are the
     * layerNames in the knownLayer. One is sent twice but shouldn't be included the second time.
     */
    @Test
    public void testAddToListConsiderWMSSelectorsContainsRecordNameTwoRecordsOneDoubled()
            throws PortalServiceException {
        CSWRecord cswRecord1 = new CSWRecord("serviceName", "idA", "infoUrlA", "dataIdAbstractA", null, null, layerName1);
        CSWRecord cswRecord2 = new CSWRecord("serviceName", "idA", "infoUrlA", "dataIdAbstractA", null, null, layerName2);

        knownLayerService = buildKnownLayerServiceWithWMSSelectorsAndKnownLayerIdOf("someid");
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord1, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord2, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord2, knownLayer);

        int requiredSize = ((WMSSelectors) knownLayer.getKnownLayerSelector()).getWmsSelectors().size();
        int nullsAfterIndex = 1;

        Assert.assertEquals(requiredSize, cswRecordListToAddTo.size());
        checkCorrectSize(cswRecordListToAddTo, nullsAfterIndex);

        // System.out.println("cswRecordListToAddTo: " + cswRecordListToAddTo);
    }

    /**
     * Add all This will work as the knownLayer contains a WMSSelectors instanceof KnownLayer and the cswRecord.getServiceName (layerName1, layerName2, ..3)
     * are the layerNames in the knownLayer. Two are sent twice but shouldn't be included the second time.
     */
    @Test
    public void testAddToListConsiderWMSSelectorsContainsRecordNameAllRecords()
            throws PortalServiceException {
        CSWRecord cswRecord1 = new CSWRecord("serviceName", "idA", "infoUrlA", "dataIdAbstractA", null, null, layerName1);
        CSWRecord cswRecord2 = new CSWRecord("serviceName", "idA", "infoUrlA", "dataIdAbstractA", null, null, layerName2);
        CSWRecord cswRecord3 = new CSWRecord("serviceName", "idA", "infoUrlA", "dataIdAbstractA", null, null, layerName3);

        knownLayerService = buildKnownLayerServiceWithWMSSelectorsAndKnownLayerIdOf("someid");
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord1, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord2, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord2, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord3, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord3, knownLayer);
        Assert.assertEquals(3, cswRecordListToAddTo.size());
    }

    /**
     * Add all - in order they are expected This will work as the knownLayer contains a WMSSelectors instanceof KnownLayer and the cswRecord.getServiceName
     * (layerName1, layerName2, ..3) are the layerNames in the knownLayer. Two are sent twice but shouldn't be included the second time.
     */
    @Test
    public void testAddToListConsiderWMSSelectorsContainsRecordNameAllRecordsCheckOrder01()
            throws PortalServiceException {
        CSWRecord cswRecord1 = new CSWRecord("serviceName", "idA", "infoUrlA", "dataIdAbstractA", null, null, layerName1);
        CSWRecord cswRecord2 = new CSWRecord("serviceName", "idA", "infoUrlA", "dataIdAbstractA", null, null, layerName2);
        CSWRecord cswRecord3 = new CSWRecord("serviceName", "idA", "infoUrlA", "dataIdAbstractA", null, null, layerName3);

        knownLayerService = buildKnownLayerServiceWithWMSSelectorsAndKnownLayerIdOf("someid");
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord1, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord2, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord2, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord3, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord3, knownLayer);
        Assert.assertEquals(3, cswRecordListToAddTo.size());
        Assert.assertEquals(layerName1, cswRecordListToAddTo.get(0).getLayerName());
        Assert.assertEquals(layerName2, cswRecordListToAddTo.get(1).getLayerName());
        Assert.assertEquals(layerName3, cswRecordListToAddTo.get(2).getLayerName());
    }

    /**
     * Add all - NOT in order they are expected This will work as the knownLayer contains a WMSSelectors instanceof KnownLayer and the
     * cswRecord.getServiceName (layerName1, layerName2, ..3) are the layerNames in the knownLayer. Two are sent twice but shouldn't be included the second
     * time.
     */
    @Test
    public void testAddToListConsiderWMSSelectorsContainsRecordNameAllRecordsCheckOrder02()
            throws PortalServiceException {
        CSWRecord cswRecord1 = new CSWRecord("serviceName", "idA", "infoUrlA", "dataIdAbstractA", null, null, layerName1);
        CSWRecord cswRecord2 = new CSWRecord("serviceName", "idA", "infoUrlA", "dataIdAbstractA", null, null, layerName2);
        CSWRecord cswRecord3 = new CSWRecord("serviceName", "idA", "infoUrlA", "dataIdAbstractA", null, null, layerName3);

        knownLayerService = buildKnownLayerServiceWithWMSSelectorsAndKnownLayerIdOf("someid");
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord2, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord2, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord3, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord1, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord3, knownLayer);
        Assert.assertEquals(3, cswRecordListToAddTo.size());
        Assert.assertEquals(layerName1, cswRecordListToAddTo.get(0).getLayerName());
        Assert.assertEquals(layerName2, cswRecordListToAddTo.get(1).getLayerName());
        Assert.assertEquals(layerName3, cswRecordListToAddTo.get(2).getLayerName());
    }

    /**
     * Add all - NOT in order they are expected This will work as the knownLayer contains a WMSSelectors instanceof KnownLayer and the
     * cswRecord.getServiceName (layerName1, layerName2, ..3) are the layerNames in the knownLayer. Two are sent twice but shouldn't be included the second
     * time.
     */
    @Test
    public void testAddToListButNotUsingWMSSelectors()
            throws PortalServiceException {
        CSWRecord cswRecord1 = new CSWRecord("serviceName", "idA", "infoUrlA", "dataIdAbstractA", null, null, layerName1);
        CSWRecord cswRecord2 = new CSWRecord("serviceName", "idA", "infoUrlA", "dataIdAbstractA", null, null, layerName2);
        CSWRecord cswRecord3 = new CSWRecord("serviceName", "idA", "infoUrlA", "dataIdAbstractA", null, null, layerName3);
        
        WMSSelector wmsSelector1 = new WMSSelector(layerName1); 
        WMSSelector wmsSelector2 = new WMSSelector(layerName2); 
        WMSSelector wmsSelector3 = new WMSSelector(layerName3); 

        knownLayerService = buildKnownLayerServiceWithTheseWMSSelectorItemsAndKnownLayerIdOf("someid",wmsSelector1,wmsSelector2,wmsSelector3 );
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord2, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord2, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord3, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord1, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord3, knownLayer);
        // Different to when layers are through a WMSSelectors.  In this case, all layers added are used, regardless of duplicates
        Assert.assertEquals(5, cswRecordListToAddTo.size());
    }
}

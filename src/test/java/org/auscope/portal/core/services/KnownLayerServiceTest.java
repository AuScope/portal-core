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
 * @author u86990
 *
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
        // CSWRecord cswRec1 = new CSWRecord("service1", "id1", "infoUrl1", "dataIdAbstract1", null, null);
        // CSWRecord cswRec2 = new CSWRecord("service2", "id2", "infoUrl2", "dataIdAbstract2", null, null);
        // cswRecord = new CSWRecord(layerName1, "idA", "infoUrlA", "dataIdAbstractA", null, null);
        //
        // cswRecordList.add(cswRec1);
        // cswRecordList.add(cswRec2);
        layerName1 = "layerName1";
        layerName2 = "layerName2";
        layerName3 = "layerName3";

        // In the application, this specification of layernames is in the auscope-known-layers bean definition for a layer, such as:
        // <bean class="org.auscope.portal.core.view.knownlayer.WMSSelectors">
        // <constructor-arg name="layersMode" value="AND"/>
        // <constructor-arg name="layerNames">
        // <list>
        // <value>TOPO250K_Roads</value>
        // <value>Roads_1</value>
        // <value>Roads_2</value>
        // <value>Roads_3</value>
        // <value>Roads_4</value>
        // </list>
        // </constructor-arg>
        // </bean>
        // The whole point of the work this is testing is to retain that order.
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
     * .
     * 
     * @throws PortalServiceException
     */
    @Test(expected = PortalServiceException.class)
    public void testAddToListConsiderWMSSelectorsDoesntContainRecordName() throws PortalServiceException {
        /*
         * This will be an exception as the knownLayer contains a WMSSelectors instanceof KnownLayer but the cswRecord.getServiceName ('unknownLayerName') is
         * not one of the layerNames in the knownLayer
         */
        CSWRecord cswRecord = new CSWRecord("unknownLayerName", "idA", "infoUrlA", "dataIdAbstractA", null, null);

        knownLayerService = buildKnownLayerServiceWithWMSSelectorsAndKnownLayerIdOf("thisDoesntExistInWMSSelectors");
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord, knownLayer);
        Assert.assertNotEquals(0, cswRecordListToAddTo.size());
        // System.out.println("cswRecordListToAddTo: " + cswRecordListToAddTo);
    }

    @Test
    public void testAddToListConsiderWMSSelectorsContainsRecordName() throws PortalServiceException {
        /*
         * This will work as the knownLayer contains a WMSSelectors instanceof KnownLayer and the cswRecord.getServiceName (layerName1) is one of the layerNames
         * in the knownLayer
         */
        CSWRecord cswRecord = new CSWRecord(layerName1, "idA", "infoUrlA", "dataIdAbstractA", null, null);

        knownLayerService = buildKnownLayerServiceWithWMSSelectorsAndKnownLayerIdOf("someid");
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord, knownLayer);

        int requiredSize = ((WMSSelectors) knownLayer.getKnownLayerSelector()).getWmsSelectors().size();
        int nullsAfterIndex = 0;

        Assert.assertEquals(requiredSize, cswRecordListToAddTo.size());
        checkCorrectSize(cswRecordListToAddTo, nullsAfterIndex);
        // System.out.println("cswRecordListToAddTo: " + cswRecordListToAddTo);
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

    @Test
    public void testAddToListConsiderWMSSelectorsContainsRecordNameTwoRecords() throws PortalServiceException {
        /*
         * This will work as the knownLayer contains a WMSSelectors instanceof KnownLayer and the cswRecord.getServiceName (layerName1, layerName2) are the
         * layerNames in the knownLayer
         */
        CSWRecord cswRecord1 = new CSWRecord(layerName1, "idA", "infoUrlA", "dataIdAbstractA", null, null);
        CSWRecord cswRecord2 = new CSWRecord(layerName2, "idA", "infoUrlA", "dataIdAbstractA", null, null);

        knownLayerService = buildKnownLayerServiceWithWMSSelectorsAndKnownLayerIdOf("someid");
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord1, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord2, knownLayer);

        int requiredSize = ((WMSSelectors) knownLayer.getKnownLayerSelector()).getWmsSelectors().size();
        int nullsAfterIndex = 1;

        Assert.assertEquals(requiredSize, cswRecordListToAddTo.size());
        checkCorrectSize(cswRecordListToAddTo, nullsAfterIndex);

        // System.out.println("cswRecordListToAddTo: " + cswRecordListToAddTo);
    }

    @Test
    public void testAddToListConsiderWMSSelectorsContainsRecordNameTwoRecordsOneDoubled()
            throws PortalServiceException {
        /*
         * This will work as the knownLayer contains a WMSSelectors instanceof KnownLayer and the cswRecord.getServiceName (layerName1, layerName2) are the
         * layerNames in the knownLayer. One is sent twice but shouldn't be included the second time.
         */
        CSWRecord cswRecord1 = new CSWRecord(layerName1, "idA", "infoUrlA", "dataIdAbstractA", null, null);
        CSWRecord cswRecord2 = new CSWRecord(layerName2, "idA", "infoUrlA", "dataIdAbstractA", null, null);

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

    @Test
    public void testAddToListConsiderWMSSelectorsContainsRecordNameAllRecords()
            throws PortalServiceException {
        /*
         * Add all This will work as the knownLayer contains a WMSSelectors instanceof KnownLayer and the cswRecord.getServiceName (layerName1, layerName2, ..3)
         * are the layerNames in the knownLayer. Two are sent twice but shouldn't be included the second time.
         */
        CSWRecord cswRecord1 = new CSWRecord(layerName1, "idA", "infoUrlA", "dataIdAbstractA", null, null);
        CSWRecord cswRecord2 = new CSWRecord(layerName2, "idA", "infoUrlA", "dataIdAbstractA", null, null);
        CSWRecord cswRecord3 = new CSWRecord(layerName3, "idA", "infoUrlA", "dataIdAbstractA", null, null);

        knownLayerService = buildKnownLayerServiceWithWMSSelectorsAndKnownLayerIdOf("someid");
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord1, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord2, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord2, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord3, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord3, knownLayer);
        Assert.assertEquals(3, cswRecordListToAddTo.size());
        // System.out.println("cswRecordListToAddTo: " + cswRecordListToAddTo);
    }

    @Test
    public void testAddToListConsiderWMSSelectorsContainsRecordNameAllRecordsCheckOrder01()
            throws PortalServiceException {
        /*
         * Add all - in order they are expected This will work as the knownLayer contains a WMSSelectors instanceof KnownLayer and the cswRecord.getServiceName
         * (layerName1, layerName2, ..3) are the layerNames in the knownLayer. Two are sent twice but shouldn't be included the second time.
         */
        CSWRecord cswRecord1 = new CSWRecord(layerName1, "idA", "infoUrlA", "dataIdAbstractA", null, null);
        CSWRecord cswRecord2 = new CSWRecord(layerName2, "idA", "infoUrlA", "dataIdAbstractA", null, null);
        CSWRecord cswRecord3 = new CSWRecord(layerName3, "idA", "infoUrlA", "dataIdAbstractA", null, null);

        knownLayerService = buildKnownLayerServiceWithWMSSelectorsAndKnownLayerIdOf("someid");
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord1, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord2, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord2, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord3, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord3, knownLayer);
        Assert.assertEquals(3, cswRecordListToAddTo.size());
        Assert.assertEquals(layerName1, cswRecordListToAddTo.get(0).getServiceName());
        Assert.assertEquals(layerName2, cswRecordListToAddTo.get(1).getServiceName());
        Assert.assertEquals(layerName3, cswRecordListToAddTo.get(2).getServiceName());
        // System.out.println("cswRecordListToAddTo: " + cswRecordListToAddTo);
    }

    @Test
    public void testAddToListConsiderWMSSelectorsContainsRecordNameAllRecordsCheckOrder02()
            throws PortalServiceException {
        /*
         * Add all - NOT in order they are expected This will work as the knownLayer contains a WMSSelectors instanceof KnownLayer and the
         * cswRecord.getServiceName (layerName1, layerName2, ..3) are the layerNames in the knownLayer. Two are sent twice but shouldn't be included the second
         * time.
         */
        CSWRecord cswRecord1 = new CSWRecord(layerName1, "idA", "infoUrlA", "dataIdAbstractA", null, null);
        CSWRecord cswRecord2 = new CSWRecord(layerName2, "idA", "infoUrlA", "dataIdAbstractA", null, null);
        CSWRecord cswRecord3 = new CSWRecord(layerName3, "idA", "infoUrlA", "dataIdAbstractA", null, null);

        knownLayerService = buildKnownLayerServiceWithWMSSelectorsAndKnownLayerIdOf("someid");
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord2, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord2, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord3, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord1, knownLayer);
        knownLayerService.addToListConsiderWMSSelectors(cswRecordListToAddTo, cswRecord3, knownLayer);
        Assert.assertEquals(3, cswRecordListToAddTo.size());
        Assert.assertEquals(layerName1, cswRecordListToAddTo.get(0).getServiceName());
        Assert.assertEquals(layerName2, cswRecordListToAddTo.get(1).getServiceName());
        Assert.assertEquals(layerName3, cswRecordListToAddTo.get(2).getServiceName());
        // System.out.println("cswRecordListToAddTo: " + cswRecordListToAddTo);
    }

    @Test
    public void testAddToListButNotUsingWMSSelectors()
            throws PortalServiceException {
        /*
         * Add all - NOT in order they are expected This will work as the knownLayer contains a WMSSelectors instanceof KnownLayer and the
         * cswRecord.getServiceName (layerName1, layerName2, ..3) are the layerNames in the knownLayer. Two are sent twice but shouldn't be included the second
         * time.
         */
        CSWRecord cswRecord1 = new CSWRecord(layerName1, "idA", "infoUrlA", "dataIdAbstractA", null, null);
        CSWRecord cswRecord2 = new CSWRecord(layerName2, "idA", "infoUrlA", "dataIdAbstractA", null, null);
        CSWRecord cswRecord3 = new CSWRecord(layerName3, "idA", "infoUrlA", "dataIdAbstractA", null, null);
        
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

    /**
     * @param cswRecordListToAddTo2
     * @return
     */
//    private Iterable<String> getServiceNames(List<CSWRecord> cswRecordListToAddTo2) {
//        // TODO Auto-generated method stub
//        return null;
//    }

    /**
     * We have a bug related to GPT-103 Fix order of Anded Layers.  I'm investigating possible reasons (the stack trace isn't really helping and I can't reproduce it on my macine - it only happens on the server).
     * 
     * I'm investing what happens if CSWRecord.layer is null.  A use is:
     * 
     *             int indexInWMSSelectorsList = layerNames.indexOf(record.getLayerName());
     *    
     * Look at what various values do.
     */
    @Test
    public void testIndexOfVariousValues() {
        String nullString = null;
        String emptyString = "";
        String data = "blah blah blah";
        
        // no problem
        data.indexOf(emptyString);
        // Gives an NPE - i think this is our problem.
        data.indexOf(nullString);
    }
}

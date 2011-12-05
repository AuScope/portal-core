package org.auscope.portal.server.web.service;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.csw.record.AbstractCSWOnlineResource;
import org.auscope.portal.csw.record.AbstractCSWOnlineResource.OnlineResourceType;
import org.auscope.portal.csw.record.CSWOnlineResourceImpl;
import org.auscope.portal.csw.record.CSWRecord;
import org.auscope.portal.mineraloccurrence.BoreholeFilter;
import org.auscope.portal.nvcl.NVCLNamespaceContext;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.domain.wfs.WFSKMLResponse;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * The Class TestBoreholeService.
 */
public class TestBoreholeService {

    /** The context. */
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    /** The service. */
    private BoreholeService service;

    /** The mock http service caller. */
    private HttpServiceCaller mockHttpServiceCaller = context.mock(HttpServiceCaller.class);

    /** The mock method maker. */
    private WFSGetFeatureMethodMaker mockMethodMaker = context.mock(WFSGetFeatureMethodMaker.class);

    /** The mock HTTP method*/
    private HttpMethodBase mockMethod = context.mock(HttpMethodBase.class);

    /** The mock GML transformer*/
    private GmlToKml mockGmlToKml = context.mock(GmlToKml.class);

    /** The Constant GETSCANNEDBOREHOLEXML. */
    private static final String GETSCANNEDBOREHOLEXML = "src/test/resources/GetScannedBorehole.xml";

    /** The Constant HOLEIDS. */
    private static final String[] HOLEIDS =  new String[] {"http://nvclwebservices.vm.csiro.au/resource/feature/CSIRO/borehole/WTB5", "http://nvclwebservices.vm.csiro.au/resource/feature/CSIRO/borehole/GSDD006", "http://nvclwebservices.vm.csiro.au/resource/feature/CSIRO/borehole/GDDH7"};

    /**
     * Setup.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        service = new BoreholeService();
        service.setHttpServiceCaller(mockHttpServiceCaller);
        service.setWFSGetFeatureMethodMakerPOST(mockMethodMaker);
        service.setGmlToKml(mockGmlToKml);
    }

    /**
     * Test get all boreholes no bbox.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetAllBoreholesNoBbox() throws Exception {
        final FilterBoundingBox bbox = null;
        final String serviceURL = "http://example.com";
        final int maxFeatures = 45;
        final String boreholeName = "borehole-name";
        final String custodian = "custodian";
        final String dateOfDrilling = "2011-01-01";
        final String gmlString = "xmlString";
        final String kmlString = "kmlString";
        final List<String> restrictedIds = null;

        context.checking(new Expectations() {{

            oneOf(mockMethodMaker).makeMethod(with(equal(serviceURL)), with(equal("gsml:Borehole")), with(any(String.class)), with(equal(maxFeatures)));will(returnValue(mockMethod));

            oneOf(mockHttpServiceCaller).getHttpClient();
            oneOf(mockHttpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));will(returnValue(gmlString));

            oneOf(mockGmlToKml).convert(gmlString, serviceURL);will(returnValue(kmlString));
        }});

        WFSKMLResponse result = service.getAllBoreholes(serviceURL, boreholeName, custodian, dateOfDrilling, maxFeatures, bbox, restrictedIds);
        Assert.assertNotNull(result);
        Assert.assertEquals(gmlString, result.getGml());
        Assert.assertEquals(kmlString, result.getKml());
        Assert.assertSame(mockMethod, result.getMethod());
    }

    /**
     * Test get all boreholes bbox.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetAllBoreholesBbox() throws Exception {
        final FilterBoundingBox bbox = new FilterBoundingBox("mySrs", new double[] {0, 1}, new double[] {2, 3});
        final String serviceURL = "http://example.com";
        final int maxFeatures = 45;
        final String boreholeName = "borehole-name";
        final String custodian = "custodian";
        final String dateOfDrilling = "2011-01-01";
        final String gmlString = "xmlString";
        final String kmlString = "kmlString";
        final List<String> restrictedIds = null;

        context.checking(new Expectations() {{

            oneOf(mockMethodMaker).makeMethod(with(equal(serviceURL)), with(equal("gsml:Borehole")), with(any(String.class)), with(equal(maxFeatures)));will(returnValue(mockMethod));
            oneOf(mockHttpServiceCaller).getHttpClient();
            oneOf(mockHttpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));will(returnValue(gmlString));

            oneOf(mockGmlToKml).convert(gmlString, serviceURL);will(returnValue(kmlString));
        }});

        WFSKMLResponse result = service.getAllBoreholes(serviceURL, boreholeName, custodian, dateOfDrilling, maxFeatures, bbox, restrictedIds);
        Assert.assertNotNull(result);
        Assert.assertEquals(gmlString, result.getGml());
        Assert.assertEquals(kmlString, result.getKml());
        Assert.assertSame(mockMethod, result.getMethod());
    }

    /**
     * Test get restricted boreholes bbox.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetRestrictedBoreholesBbox() throws Exception {
        final String serviceURL = "http://example.com";
        final int maxFeatures = 45;
        final String boreholeName = "asda";
        final String custodian = "shaksdhska";
        final String dateOfDrilling = "2010-01-02";
        final String gmlString = "xmlString";
        final String kmlString = "kmlString";
        final List<String> restrictedIds = Arrays.asList("id1", "id2", "id3");
        final String filterString = (new BoreholeFilter(boreholeName, custodian, dateOfDrilling, restrictedIds)).getFilterStringAllRecords();

        context.checking(new Expectations() {{
            oneOf(mockMethodMaker).makeMethod(serviceURL, "gsml:Borehole", filterString, maxFeatures);will(returnValue(mockMethod));
            oneOf(mockHttpServiceCaller).getHttpClient();
            oneOf(mockHttpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));will(returnValue(gmlString));

            oneOf(mockGmlToKml).convert(gmlString, serviceURL);will(returnValue(kmlString));
        }});

        WFSKMLResponse result = service.getAllBoreholes(serviceURL, boreholeName, custodian, dateOfDrilling, maxFeatures, null, restrictedIds);
        Assert.assertNotNull(result);
        Assert.assertEquals(gmlString, result.getGml());
        Assert.assertEquals(kmlString, result.getKml());
        Assert.assertSame(mockMethod, result.getMethod());
    }

    /**
     * Tests that the service correctly parses a response from an NVCL WFS.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetHyloggerIDs() throws Exception {
        final CSWRecord mockRecord1 = context.mock(CSWRecord.class, "mockRecord1"); //good record
        final CSWRecord mockRecord2 = context.mock(CSWRecord.class, "mockRecord2"); //has the wrong wfs
        final CSWRecord mockRecord3 = context.mock(CSWRecord.class, "mockRecord3"); //has no wfs
        final CSWCacheService mockCSWService = context.mock(CSWCacheService.class);
        final CSWRecordsHostFilter hostFilter=new CSWRecordsHostFilter("");
        final AbstractCSWOnlineResource mockRecord1Resource1 = new CSWOnlineResourceImpl(new URL("http://record.1.resource.1"), "wfs", "dne", "description");
        final AbstractCSWOnlineResource mockRecord1Resource2 = new CSWOnlineResourceImpl(new URL("http://record.1.resource.2"), "wfs", NVCLNamespaceContext.PUBLISHED_DATASETS_TYPENAME, "description");

        final AbstractCSWOnlineResource mockRecord2Resource1 = new CSWOnlineResourceImpl(new URL("http://record.2.resource.1"), "wfs", "dne", "description");

        final String successResponse = org.auscope.portal.Util.loadXML(GETSCANNEDBOREHOLEXML);

        context.checking(new Expectations() {{
            oneOf(mockCSWService).getWFSRecords();
            will(returnValue(Arrays.asList(mockRecord1, mockRecord2, mockRecord3)));

            oneOf(mockRecord1).getOnlineResourcesByType(hostFilter,OnlineResourceType.WFS);
            will(returnValue(new AbstractCSWOnlineResource[] {mockRecord1Resource1, mockRecord1Resource2}));

            oneOf(mockRecord2).getOnlineResourcesByType(hostFilter,OnlineResourceType.WFS);
            will(returnValue(new AbstractCSWOnlineResource[] {mockRecord2Resource1}));

            oneOf(mockRecord3).getOnlineResourcesByType(hostFilter,OnlineResourceType.WFS);
            will(returnValue(new AbstractCSWOnlineResource[] {}));

            oneOf(mockMethodMaker).makeMethod(mockRecord1Resource2.getLinkage().toString(), mockRecord1Resource2.getName(), "", 0);
            oneOf(mockHttpServiceCaller).getHttpClient();
            oneOf(mockHttpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));
            will(returnValue(successResponse));
        }});

        List<String> restrictedIDs = service.discoverHyloggerBoreholeIDs(mockCSWService,hostFilter);
        Assert.assertNotNull(restrictedIDs);
        Assert.assertArrayEquals(HOLEIDS, restrictedIDs.toArray(new String[restrictedIDs.size()]));
    }

    /**
     * Tests that the service correctly parses a response from an NVCL WFS (even when there is an error).
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetHyloggerIDsWithError() throws Exception {
        final CSWRecord mockRecord1 = context.mock(CSWRecord.class, "mockRecord1"); //will return failure
        final CSWRecord mockRecord2 = context.mock(CSWRecord.class, "mockRecord2"); //good record
        final CSWCacheService mockCSWService = context.mock(CSWCacheService.class);
        final HttpClient mockHttpClient = context.mock(HttpClient.class);
        final HttpMethodBase mockRecord1Method = context.mock(HttpMethodBase.class, "rec1method");
        final HttpMethodBase mockRecord2Method = context.mock(HttpMethodBase.class, "rec2method");
        final CSWRecordsHostFilter hostFilter=new CSWRecordsHostFilter("");
        final AbstractCSWOnlineResource mockRecord1Resource1 = new CSWOnlineResourceImpl(new URL("http://record.1.resource.1"), "wfs", NVCLNamespaceContext.PUBLISHED_DATASETS_TYPENAME, "description");
        final AbstractCSWOnlineResource mockRecord2Resource1 = new CSWOnlineResourceImpl(new URL("http://record.2.resource.1"), "wfs", NVCLNamespaceContext.PUBLISHED_DATASETS_TYPENAME, "description");

        final String successResponse = org.auscope.portal.Util.loadXML(GETSCANNEDBOREHOLEXML);

        context.checking(new Expectations() {{
            oneOf(mockCSWService).getWFSRecords();
            will(returnValue(Arrays.asList(mockRecord1, mockRecord2)));

            oneOf(mockRecord1).getOnlineResourcesByType(hostFilter,OnlineResourceType.WFS);
            will(returnValue(new AbstractCSWOnlineResource[] {mockRecord1Resource1}));

            oneOf(mockRecord2).getOnlineResourcesByType(hostFilter,OnlineResourceType.WFS);
            will(returnValue(new AbstractCSWOnlineResource[] {mockRecord2Resource1}));

            oneOf(mockMethodMaker).makeMethod(mockRecord1Resource1.getLinkage().toString(), mockRecord1Resource1.getName(), "", 0);
            will(returnValue(mockRecord1Method));

            oneOf(mockMethodMaker).makeMethod(mockRecord2Resource1.getLinkage().toString(), mockRecord2Resource1.getName(), "", 0);
            will(returnValue(mockRecord2Method));

            allowing(mockHttpServiceCaller).getHttpClient();
            will(returnValue(mockHttpClient));

            oneOf(mockHttpServiceCaller).getMethodResponseAsString(mockRecord1Method, mockHttpClient);
            will(throwException(new Exception("I'm an exception!")));

            oneOf(mockHttpServiceCaller).getMethodResponseAsString(mockRecord2Method, mockHttpClient);
            will(returnValue(successResponse));
        }});

        List<String> restrictedIDs = service.discoverHyloggerBoreholeIDs(mockCSWService,hostFilter);
        Assert.assertNotNull(restrictedIDs);
        Assert.assertArrayEquals(HOLEIDS, restrictedIDs.toArray(new String[restrictedIDs.size()]));
    }

}

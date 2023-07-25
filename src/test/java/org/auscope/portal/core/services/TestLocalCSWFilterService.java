package org.auscope.portal.core.services;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.auscope.portal.core.services.csw.SearchFacet;
import org.auscope.portal.core.services.csw.SearchFacet.Comparison;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.methodmakers.filter.csw.CSWGetDataRecordsFilter.KeywordMatchType;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource.OnlineResourceType;
import org.auscope.portal.core.services.responses.csw.CSWGetRecordResponse;
import org.auscope.portal.core.services.responses.csw.CSWOnlineResourceImpl;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.search.FacetedMultiSearchResponse;
import org.auscope.portal.core.services.responses.search.FacetedSearchResponse;
import org.auscope.portal.core.test.BasicThreadExecutor;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.jmock.CSWGetDataRecordsFilterMatcher;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the filtering at LocalCSWFilterService
 * @author Josh Vote (CSIRO)
 *
 */
public class TestLocalCSWFilterService extends PortalTestClass {

    private CSWFilterService mockFilterService = context.mock(CSWFilterService.class);
    private CSWGetRecordResponse mockResponse1 = context.mock(CSWGetRecordResponse.class, "mockResponse1");
    private CSWGetRecordResponse mockResponse2 = context.mock(CSWGetRecordResponse.class, "mockResponse2");
    private CSWGetRecordResponse mockResponse3 = context.mock(CSWGetRecordResponse.class, "mockResponse3");
    private LocalCSWFilterService localFilterService;
    private BasicThreadExecutor executor;

    @Before
    public void setup() {
        executor = new BasicThreadExecutor();
        localFilterService = new LocalCSWFilterService(mockFilterService, executor);
    }

    @After
    public void teardown() throws InterruptedException {
        executor.getExecutorService().shutdown();
        executor.getExecutorService().awaitTermination(5000, TimeUnit.MILLISECONDS);
    }

    /**
     * Ensures our remote filter only requests the bare minimum number of records (while also respecting page size)
     * @throws Exception
     */
    @Test
    public void testRemoteFilter() throws Exception {
        final String serviceId = "service-id";
        final FilterBoundingBox bbox = new FilterBoundingBox("WGS:84", new double[] {-10.0,  -10.0}, new double[] {10.0,  10.0});
        final List<SearchFacet<? extends Object>> facets = Arrays.asList(
                new SearchFacet<FilterBoundingBox>(bbox, "bbox", Comparison.Equal),
                new SearchFacet<String>("kw1", "keyword", Comparison.Equal),
                new SearchFacet<String>("kw2", "keyword", Comparison.Equal),
                new SearchFacet<DateTime>(new DateTime(0l), "datefrom", Comparison.GreaterThan),
                new SearchFacet<DateTime>(new DateTime(1000l), "dateto", Comparison.LessThan));

        final int startIndex = 1;
        final int maxRecords = 3;
        final int pageSize = 2;

        localFilterService.setPageSize(pageSize);

        context.checking(new Expectations() {{
            //Request a full page initially
            oneOf(mockFilterService).getFilteredRecords(with(equal(serviceId)),
                                                        with(new CSWGetDataRecordsFilterMatcher(bbox,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                                                        with(equal(pageSize)),
                                                        with(equal(startIndex)));
            will(returnValue(mockResponse1));

            allowing(mockResponse1).getNextRecord();will(returnValue(3));
            allowing(mockResponse1).getRecordsMatched();will(returnValue(4));
            allowing(mockResponse1).getRecordsReturned();will(returnValue(2));
            allowing(mockResponse1).getRecords();will(returnValue(Arrays.asList(new CSWRecord("rec1"), new CSWRecord("rec2"))));

            //one remaining record to get on the second request (despite 2 being available)
            oneOf(mockFilterService).getFilteredRecords(with(equal(serviceId)),
                    with(new CSWGetDataRecordsFilterMatcher(bbox,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(1)),
                    with(equal(startIndex + pageSize)));
            will(returnValue(mockResponse2));

            allowing(mockResponse2).getNextRecord();will(returnValue(4));
            allowing(mockResponse2).getRecordsMatched();will(returnValue(4));
            allowing(mockResponse2).getRecordsReturned();will(returnValue(1));
            allowing(mockResponse2).getRecords();will(returnValue(Arrays.asList(new CSWRecord("rec3"))));

        }});

        FacetedSearchResponse response = localFilterService.getFilteredRecords(serviceId, null, facets, startIndex, maxRecords);

        Assert.assertNotNull(response);
        Assert.assertEquals(4, response.getNextIndex());
        Assert.assertEquals(3, response.getRecords().size());

        Assert.assertEquals("rec1", response.getRecords().get(0).getFileIdentifier());
        Assert.assertEquals("rec2", response.getRecords().get(1).getFileIdentifier());
        Assert.assertEquals("rec3", response.getRecords().get(2).getFileIdentifier());
    }

    /**
     * Ensures our local filter filters correctly (and requests appropriate sized requests)
     * @throws Exception
     */
    @Test
    public void testLocalFilter() throws Exception {
        final String serviceId = "service-id";
        final FilterBoundingBox bbox = new FilterBoundingBox("WGS:84", new double[] {-10.0,  -10.0}, new double[] {10.0,  10.0});
        final List<SearchFacet<? extends Object>> facets = Arrays.asList(
                new SearchFacet<FilterBoundingBox>(bbox, "bbox", Comparison.Equal),
                new SearchFacet<String>("kw1", "keyword", Comparison.Equal),
                new SearchFacet<String>("kw2", "keyword", Comparison.Equal),
                new SearchFacet<OnlineResourceType>(OnlineResourceType.WCS, "servicetype", Comparison.Equal),
                new SearchFacet<OnlineResourceType>(OnlineResourceType.WMS, "servicetype", Comparison.Equal));

        final int startIndex = 1;
        final int maxRecords = 3;
        final int pageSize = 2;

        localFilterService.setPageSize(pageSize);

        context.checking(new Expectations() {{
            //Request a full page initially
            oneOf(mockFilterService).getFilteredRecords(with(equal(serviceId)),
                                                        with(new CSWGetDataRecordsFilterMatcher(bbox,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                                                        with(equal(pageSize)),
                                                        with(equal(startIndex)));
            will(returnValue(mockResponse1));
            allowing(mockResponse1).getNextRecord();will(returnValue(3));
            allowing(mockResponse1).getRecordsMatched();will(returnValue(100));
            allowing(mockResponse1).getRecordsReturned();will(returnValue(2));
            allowing(mockResponse1).getRecords();will(returnValue(Arrays.asList(
                    new CSWRecord(serviceId, "rec1", null, null, new AbstractCSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WCS", "wcs", "")}, null),
                    new CSWRecord(serviceId, "rec2", null, null, new AbstractCSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WMS", "wms", "")}, null))));

            //Still 3 records to find
            oneOf(mockFilterService).getFilteredRecords(with(equal(serviceId)),
                    with(new CSWGetDataRecordsFilterMatcher(bbox,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(pageSize)),
                    with(equal(startIndex + pageSize)));
            will(returnValue(mockResponse2));

            allowing(mockResponse2).getNextRecord();will(returnValue(5));
            allowing(mockResponse2).getRecordsMatched();will(returnValue(100));
            allowing(mockResponse2).getRecordsReturned();will(returnValue(2));
            allowing(mockResponse2).getRecords();will(returnValue(Arrays.asList(
                    new CSWRecord(serviceId, "rec3", null, null, new AbstractCSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WCS", "wcs", ""), new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WMS", "wms", "")}, null),
                    new CSWRecord(serviceId, "rec4", null, null, new AbstractCSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WCS", "wcs", ""), new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WMS", "wms", "")}, null))));

            //Still 1 more record to find
            oneOf(mockFilterService).getFilteredRecords(with(equal(serviceId)),
                    with(new CSWGetDataRecordsFilterMatcher(bbox,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(pageSize)),
                    with(equal(startIndex + pageSize * 2)));
            will(returnValue(mockResponse3));

            allowing(mockResponse3).getNextRecord();will(returnValue(7));
            allowing(mockResponse3).getRecordsMatched();will(returnValue(100));
            allowing(mockResponse3).getRecordsReturned();will(returnValue(2));
            allowing(mockResponse3).getRecords();will(returnValue(Arrays.asList(
                    new CSWRecord(serviceId, "rec5", null, null, new AbstractCSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WCS", "wcs", ""), new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WMS", "wms", "")}, null),
                    new CSWRecord(serviceId, "rec6", null, null, new AbstractCSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WCS", "wcs", ""), new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WMS", "wms", "")}, null))));
        }});

        FacetedSearchResponse response = localFilterService.getFilteredRecords(serviceId, null, facets, startIndex, maxRecords);

        Assert.assertNotNull(response);
        Assert.assertEquals(3, response.getRecords().size());
        Assert.assertEquals(6, response.getNextIndex());

        Assert.assertEquals("rec3", response.getRecords().get(0).getFileIdentifier());
        Assert.assertEquals("rec4", response.getRecords().get(1).getFileIdentifier());
        Assert.assertEquals("rec5", response.getRecords().get(2).getFileIdentifier());
    }

    /**
     * Ensures our local filter filters correctly abort searching if the remote source indicates no more records
     * @throws Exception
     */
    @Test
    public void testLocalFilterRunoff() throws Exception {
        final String serviceId = "service-id";
        final FilterBoundingBox bbox = new FilterBoundingBox("WGS:84", new double[] {-10.0,  -10.0}, new double[] {10.0,  10.0});
        final List<SearchFacet<? extends Object>> facets = Arrays.asList(
                new SearchFacet<FilterBoundingBox>(bbox, "bbox", Comparison.Equal),
                new SearchFacet<String>("kw1", "keyword", Comparison.Equal),
                new SearchFacet<String>("kw2", "keyword", Comparison.Equal),
                new SearchFacet<OnlineResourceType>(OnlineResourceType.WCS, "servicetype", Comparison.Equal));

        final int startIndex = 1;
        final int maxRecords = 2;
        final int pageSize = 2;

        localFilterService.setPageSize(pageSize);

        context.checking(new Expectations() {{
            //Request a full page initially
            oneOf(mockFilterService).getFilteredRecords(with(equal(serviceId)),
                                                        with(new CSWGetDataRecordsFilterMatcher(bbox,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                                                        with(equal(pageSize)),
                                                        with(equal(startIndex)));
            will(returnValue(mockResponse1));
            allowing(mockResponse1).getNextRecord();will(returnValue(3));
            allowing(mockResponse1).getRecordsMatched();will(returnValue(4));
            allowing(mockResponse1).getRecordsReturned();will(returnValue(2));
            allowing(mockResponse1).getRecords();will(returnValue(Arrays.asList(
                    new CSWRecord(serviceId, "rec1", null, null, new AbstractCSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WCS", "wcs", "")}, null),
                    new CSWRecord(serviceId, "rec2", null, null, new AbstractCSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WMS", "wms", "")}, null))));

            //Still 1 record to find
            oneOf(mockFilterService).getFilteredRecords(with(equal(serviceId)),
                    with(new CSWGetDataRecordsFilterMatcher(bbox,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(pageSize)),
                    with(equal(startIndex + pageSize)));
            will(returnValue(mockResponse2));

            allowing(mockResponse2).getNextRecord();will(returnValue(0));
            allowing(mockResponse2).getRecordsMatched();will(returnValue(4));
            allowing(mockResponse2).getRecordsReturned();will(returnValue(2));
            allowing(mockResponse2).getRecords();will(returnValue(Arrays.asList(
                    new CSWRecord(serviceId, "rec3", null, null, new AbstractCSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WMS", "wms", "")}, null),
                    new CSWRecord(serviceId, "rec4", null, null, new AbstractCSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WMS", "wms", "")}, null))));
        }});

        FacetedSearchResponse response = localFilterService.getFilteredRecords(serviceId, null, facets, startIndex, maxRecords);

        Assert.assertNotNull(response);
        Assert.assertEquals(1, response.getRecords().size());
        Assert.assertEquals(0, response.getNextIndex());

        Assert.assertEquals("rec1", response.getRecords().get(0).getFileIdentifier());
    }

    /**
     * Ensures we handle service exceptions gracefully
     * @throws Exception
     */
    @Test(expected=PortalServiceException.class)
    public void testServiceException() throws Exception {
        final String serviceId = "service-id";
        final FilterBoundingBox bbox = new FilterBoundingBox("WGS:84", new double[] {-10.0,  -10.0}, new double[] {10.0,  10.0});
        final List<SearchFacet<? extends Object>> facets = Arrays.asList(
                new SearchFacet<FilterBoundingBox>(bbox, "bbox", Comparison.Equal),
                new SearchFacet<String>("kw1", "keyword", Comparison.Equal),
                new SearchFacet<String>("kw2", "keyword", Comparison.Equal),
                new SearchFacet<DateTime>(new DateTime(0l), "datefrom", Comparison.GreaterThan),
                new SearchFacet<DateTime>(new DateTime(1000l), "dateto", Comparison.LessThan));

        final int startIndex = 1;
        final int maxRecords = 3;
        final int pageSize = 2;

        localFilterService.setPageSize(pageSize);

        context.checking(new Expectations() {{
            //Request a full page initially
            oneOf(mockFilterService).getFilteredRecords(with(equal(serviceId)),
                                                        with(new CSWGetDataRecordsFilterMatcher(bbox,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                                                        with(equal(pageSize)),
                                                        with(equal(startIndex)));
            will(returnValue(mockResponse1));

            allowing(mockResponse1).getNextRecord();will(returnValue(3));
            allowing(mockResponse1).getRecordsMatched();will(returnValue(4));
            allowing(mockResponse1).getRecordsReturned();will(returnValue(2));
            allowing(mockResponse1).getRecords();will(returnValue(Arrays.asList(new CSWRecord("rec1"), new CSWRecord("rec2"))));

            //Second request throws an exception
            oneOf(mockFilterService).getFilteredRecords(with(equal(serviceId)),
                    with(new CSWGetDataRecordsFilterMatcher(bbox,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(1)),
                    with(equal(startIndex + pageSize)));
            will(throwException(new PortalServiceException("Test exception")));
        }});

        localFilterService.getFilteredRecords(serviceId, null, facets, startIndex, maxRecords);
    }

    /**
     * Tests that the underlying implementation correctly redistributes fulfillment across services the remaining services
     * @throws Exception
     */
    @Test
    public void testGetMultiFilteredRecords_RedistributeFulfillment() throws Exception {
        final int RECORD_REQUEST_COUNT = 11;
        final String[] serviceIds = new String[] {"service1", "service2", "service3"};
        final HashMap<String, Integer> startIndexes = new HashMap<String, Integer>();
        startIndexes.put("service1", 1);
        startIndexes.put("service2", 100);
        startIndexes.put("service3", 10);

        final List<SearchFacet<? extends Object>> facets = Arrays.asList(
                new SearchFacet<String>("kw1", "keyword", Comparison.Equal),
                new SearchFacet<String>("kw2", "keyword", Comparison.Equal));

        final CSWGetRecordResponse mockServ1Res1 = context.mock(CSWGetRecordResponse.class, "mockServ1Res1");
        final CSWGetRecordResponse mockServ1Res2 = context.mock(CSWGetRecordResponse.class, "mockServ1Res2");
        final CSWGetRecordResponse mockServ1Res3 = context.mock(CSWGetRecordResponse.class, "mockServ1Res3");
        final CSWGetRecordResponse mockServ2Res1 = context.mock(CSWGetRecordResponse.class, "mockServ2Res1");
        final CSWGetRecordResponse mockServ3Res1 = context.mock(CSWGetRecordResponse.class, "mockServ3Res1");
        final CSWGetRecordResponse mockServ3Res2 = context.mock(CSWGetRecordResponse.class, "mockServ3Res2");

        final Sequence serv1Sequence = context.sequence("serv1Sequence");
        final Sequence serv2Sequence = context.sequence("serv2Sequence");
        final Sequence serv3Sequence = context.sequence("serv3Sequence");

        context.checking(new Expectations() {{
            //Service 1 will make three requests. The first 4 records and then a redistributed 2 and then a final redistributed 1
            oneOf(mockFilterService).getFilteredRecords(
                    with(equal("service1")),
                    with(new CSWGetDataRecordsFilterMatcher(null,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(4)),
                    with(equal(1)));
            inSequence(serv1Sequence);
            will(delayReturnValue(1000, mockServ1Res1));

            allowing(mockServ1Res1).getNextRecord();will(returnValue(5));
            allowing(mockServ1Res1).getRecordsMatched();will(returnValue(88));
            allowing(mockServ1Res1).getRecordsReturned();will(returnValue(4));
            allowing(mockServ1Res1).getRecords();will(returnValue(Arrays.asList(new CSWRecord("s1rec1"), new CSWRecord("s1rec2"), new CSWRecord("s1rec3"), new CSWRecord("s1rec4"))));

            oneOf(mockFilterService).getFilteredRecords(
                    with(equal("service1")),
                    with(new CSWGetDataRecordsFilterMatcher(null,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(2)),
                    with(equal(5)));
            inSequence(serv1Sequence);
            will(delayReturnValue(500, mockServ1Res2));
            allowing(mockServ1Res2).getNextRecord();will(returnValue(7));
            allowing(mockServ1Res2).getRecordsMatched();will(returnValue(88));
            allowing(mockServ1Res2).getRecordsReturned();will(returnValue(2));
            allowing(mockServ1Res2).getRecords();will(returnValue(Arrays.asList(new CSWRecord("s1rec5"), new CSWRecord("s1rec6"))));

            oneOf(mockFilterService).getFilteredRecords(
                    with(equal("service1")),
                    with(new CSWGetDataRecordsFilterMatcher(null,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(1)),
                    with(equal(7)));
            inSequence(serv1Sequence);
            will(returnValue(mockServ1Res3));
            allowing(mockServ1Res3).getNextRecord();will(returnValue(8));
            allowing(mockServ1Res3).getRecordsMatched();will(returnValue(88));
            allowing(mockServ1Res3).getRecordsReturned();will(returnValue(1));
            allowing(mockServ1Res3).getRecords();will(returnValue(Arrays.asList(new CSWRecord("s1rec7"))));

            //Service 2 will make one request for four records and then return 0
            oneOf(mockFilterService).getFilteredRecords(
                    with(equal("service2")),
                    with(new CSWGetDataRecordsFilterMatcher(null,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(4)),
                    with(equal(100)));
            inSequence(serv2Sequence);
            will(delayReturnValue(250, mockServ2Res1));
            allowing(mockServ2Res1).getNextRecord();will(returnValue(0));
            allowing(mockServ2Res1).getRecordsMatched();will(returnValue(0));
            allowing(mockServ2Res1).getRecordsReturned();will(returnValue(0));
            allowing(mockServ2Res1).getRecords();will(returnValue(new ArrayList<CSWRecord>()));


            //Service 3 will make one two requests. The first for 3 records will be fulfilled, the second redistributed request for 2 will only be half fulfilled
            oneOf(mockFilterService).getFilteredRecords(
                    with(equal("service3")),
                    with(new CSWGetDataRecordsFilterMatcher(null,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(3)),
                    with(equal(10)));
            inSequence(serv3Sequence);
            will(delayReturnValue(500, mockServ3Res1));
            allowing(mockServ3Res1).getNextRecord();will(returnValue(14));
            allowing(mockServ3Res1).getRecordsMatched();will(returnValue(4));
            allowing(mockServ3Res1).getRecordsReturned();will(returnValue(3));
            allowing(mockServ3Res1).getRecords();will(returnValue(Arrays.asList(new CSWRecord("s3rec1"), new CSWRecord("s3rec2"), new CSWRecord("s3rec3"))));

            oneOf(mockFilterService).getFilteredRecords(
                    with(equal("service3")),
                    with(new CSWGetDataRecordsFilterMatcher(null,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(2)),
                    with(equal(13)));
            inSequence(serv3Sequence);
            will(delayReturnValue(1000, mockServ3Res2));
            allowing(mockServ3Res2).getNextRecord();will(returnValue(0));
            allowing(mockServ3Res2).getRecordsMatched();will(returnValue(1));
            allowing(mockServ3Res2).getRecordsReturned();will(returnValue(1));
            allowing(mockServ3Res2).getRecords();will(returnValue(Arrays.asList(new CSWRecord("s3rec4"))));
        }});

        FacetedMultiSearchResponse response = localFilterService.getFilteredRecords(serviceIds, null, facets, startIndexes, RECORD_REQUEST_COUNT);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getNextIndexes());
        Assert.assertNotNull(response.getRecords());
        Assert.assertNotNull(response.getStartIndexes());

        Assert.assertEquals(Integer.valueOf(8), response.getNextIndexes().get("service1"));
        Assert.assertEquals(Integer.valueOf(0), response.getNextIndexes().get("service2"));
        Assert.assertEquals(Integer.valueOf(0), response.getNextIndexes().get("service3"));

        Assert.assertEquals(Integer.valueOf(1), response.getStartIndexes().get("service1"));
        Assert.assertEquals(Integer.valueOf(100), response.getStartIndexes().get("service2"));
        Assert.assertEquals(Integer.valueOf(10), response.getStartIndexes().get("service3"));

        String[] expectedRecordOrdering = new String[] {"s1rec1","s3rec1","s1rec2","s3rec2","s1rec3","s3rec3","s1rec4","s3rec4","s1rec5","s1rec6","s1rec7"};
        Assert.assertEquals(expectedRecordOrdering.length, response.getRecords().size());
        for (int i = 0; i < expectedRecordOrdering.length; i++) {
            Assert.assertEquals(expectedRecordOrdering[i], response.getRecords().get(i).getFileIdentifier());
        }
    }

    /**
     * Tests that everything correctly shutsdown if we don't have enough records to fulfill the entire request
     *
     * @throws Exception
     */
    @Test
    public void testGetMultiFilteredRecords_Unfulfillable() throws Exception {
        final int RECORD_REQUEST_COUNT = 11;
        final String[] serviceIds = new String[] {"service1", "service2", "service3"};
        final HashMap<String, Integer> startIndexes = new HashMap<String, Integer>();
        startIndexes.put("service1", 1);
        startIndexes.put("service2", 100);
        startIndexes.put("service3", 10);

        final List<SearchFacet<? extends Object>> facets = Arrays.asList(
                new SearchFacet<String>("kw1", "keyword", Comparison.Equal),
                new SearchFacet<String>("kw2", "keyword", Comparison.Equal));

        final CSWGetRecordResponse mockServ1Res1 = context.mock(CSWGetRecordResponse.class, "mockServ1Res1");
        final CSWGetRecordResponse mockServ1Res2 = context.mock(CSWGetRecordResponse.class, "mockServ1Res2");
        final CSWGetRecordResponse mockServ2Res1 = context.mock(CSWGetRecordResponse.class, "mockServ2Res1");
        final CSWGetRecordResponse mockServ3Res1 = context.mock(CSWGetRecordResponse.class, "mockServ3Res1");

        final Sequence serv1Sequence = context.sequence("serv1Sequence");
        final Sequence serv2Sequence = context.sequence("serv2Sequence");
        final Sequence serv3Sequence = context.sequence("serv3Sequence");

        context.checking(new Expectations() {{
            //Service 1 will make 2 requests. The first 4 records and then a redistribution of 4 from service 2 (as service 3 will have no more available to redistribute to)
            oneOf(mockFilterService).getFilteredRecords(
                    with(equal("service1")),
                    with(new CSWGetDataRecordsFilterMatcher(null,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(4)),
                    with(equal(1)));
            inSequence(serv1Sequence);
            will(delayReturnValue(1000, mockServ1Res1)); //We need a delay here so that we can be sure that service3 and service2 fulfillment redistributions have both occurred
                                                         //(otherwise our mock object expectation definitions become silly complicated)

            allowing(mockServ1Res1).getNextRecord();will(returnValue(5));
            allowing(mockServ1Res1).getRecordsMatched();will(returnValue(5));
            allowing(mockServ1Res1).getRecordsReturned();will(returnValue(4));
            allowing(mockServ1Res1).getRecords();will(returnValue(Arrays.asList(new CSWRecord("s1rec1"), new CSWRecord("s1rec2"), new CSWRecord("s1rec3"), new CSWRecord("s1rec4"))));

            oneOf(mockFilterService).getFilteredRecords(
                    with(equal("service1")),
                    with(new CSWGetDataRecordsFilterMatcher(null,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(4)),
                    with(equal(5)));
            inSequence(serv1Sequence);
            will(returnValue(mockServ1Res2));
            allowing(mockServ1Res2).getNextRecord();will(returnValue(0));
            allowing(mockServ1Res2).getRecordsMatched();will(returnValue(5));
            allowing(mockServ1Res2).getRecordsReturned();will(returnValue(1));
            allowing(mockServ1Res2).getRecords();will(returnValue(Arrays.asList(new CSWRecord("s1rec5"))));

            //Service 2 will make one request for four records and then return 0
            oneOf(mockFilterService).getFilteredRecords(
                    with(equal("service2")),
                    with(new CSWGetDataRecordsFilterMatcher(null,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(4)),
                    with(equal(100)));
            inSequence(serv2Sequence);
            will(delayReturnValue(250, mockServ2Res1));
            allowing(mockServ2Res1).getNextRecord();will(returnValue(0));
            allowing(mockServ2Res1).getRecordsMatched();will(returnValue(0));
            allowing(mockServ2Res1).getRecordsReturned();will(returnValue(0));
            allowing(mockServ2Res1).getRecords();will(returnValue(new ArrayList<CSWRecord>()));


            //Service 3 will make one request. The first for 3 records will be fulfilled but there will be no more available.
            oneOf(mockFilterService).getFilteredRecords(
                    with(equal("service3")),
                    with(new CSWGetDataRecordsFilterMatcher(null,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(3)),
                    with(equal(10)));
            inSequence(serv3Sequence);
            will(delayReturnValue(500, mockServ3Res1));
            allowing(mockServ3Res1).getNextRecord();will(returnValue(0));
            allowing(mockServ3Res1).getRecordsMatched();will(returnValue(3));
            allowing(mockServ3Res1).getRecordsReturned();will(returnValue(3));
            allowing(mockServ3Res1).getRecords();will(returnValue(Arrays.asList(new CSWRecord("s3rec1"), new CSWRecord("s3rec2"), new CSWRecord("s3rec3"))));
        }});

        FacetedMultiSearchResponse response = localFilterService.getFilteredRecords(serviceIds, null, facets, startIndexes, RECORD_REQUEST_COUNT);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getNextIndexes());
        Assert.assertNotNull(response.getRecords());
        Assert.assertNotNull(response.getStartIndexes());

        Assert.assertEquals(Integer.valueOf(0), response.getNextIndexes().get("service1"));
        Assert.assertEquals(Integer.valueOf(0), response.getNextIndexes().get("service2"));
        Assert.assertEquals(Integer.valueOf(0), response.getNextIndexes().get("service3"));

        Assert.assertEquals(Integer.valueOf(1), response.getStartIndexes().get("service1"));
        Assert.assertEquals(Integer.valueOf(100), response.getStartIndexes().get("service2"));
        Assert.assertEquals(Integer.valueOf(10), response.getStartIndexes().get("service3"));

        String[] expectedRecordOrdering = new String[] {"s1rec1","s3rec1","s1rec2","s3rec2","s1rec3","s3rec3","s1rec4","s1rec5"};
        Assert.assertEquals(expectedRecordOrdering.length, response.getRecords().size());
        for (int i = 0; i < expectedRecordOrdering.length; i++) {
            Assert.assertEquals(expectedRecordOrdering[i], response.getRecords().get(i).getFileIdentifier());
        }
    }

    /**
     * Tests that everything works correctly when we get absolutely nothing back
     * @throws Exception
     */
    @Test
    public void testGetMultiFilteredRecords_NullSet() throws Exception {
        final int RECORD_REQUEST_COUNT = 11;
        final String[] serviceIds = new String[] {"service1", "service2", "service3"};
        final HashMap<String, Integer> startIndexes = new HashMap<String, Integer>();
        startIndexes.put("service1", 1);
        startIndexes.put("service2", 100);
        startIndexes.put("service3", 10);

        final List<SearchFacet<? extends Object>> facets = Arrays.asList(
                new SearchFacet<String>("kw1", "keyword", Comparison.Equal),
                new SearchFacet<String>("kw2", "keyword", Comparison.Equal));

        final CSWGetRecordResponse mockServ1Res1 = context.mock(CSWGetRecordResponse.class, "mockServ1Res1");
        final CSWGetRecordResponse mockServ2Res1 = context.mock(CSWGetRecordResponse.class, "mockServ2Res1");
        final CSWGetRecordResponse mockServ3Res1 = context.mock(CSWGetRecordResponse.class, "mockServ3Res1");

        final Sequence serv1Sequence = context.sequence("serv1Sequence");
        final Sequence serv2Sequence = context.sequence("serv2Sequence");
        final Sequence serv3Sequence = context.sequence("serv3Sequence");

        context.checking(new Expectations() {{
            //Service 1 will make 2 requests. The first 4 records and then a redistributed 1 (of 2)
            oneOf(mockFilterService).getFilteredRecords(
                    with(equal("service1")),
                    with(new CSWGetDataRecordsFilterMatcher(null,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(any(Integer.class)),
                    with(equal(1)));
            inSequence(serv1Sequence);
            will(returnValue(mockServ1Res1));
            allowing(mockServ1Res1).getNextRecord();will(returnValue(0));
            allowing(mockServ1Res1).getRecordsMatched();will(returnValue(0));
            allowing(mockServ1Res1).getRecordsReturned();will(returnValue(0));
            allowing(mockServ1Res1).getRecords();will(returnValue(new ArrayList<CSWRecord>()));

            //Service 2 will make one request for four records and then return 0
            oneOf(mockFilterService).getFilteredRecords(
                    with(equal("service2")),
                    with(new CSWGetDataRecordsFilterMatcher(null,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(any(Integer.class)),
                    with(equal(100)));
            inSequence(serv2Sequence);
            will(returnValue(mockServ2Res1));
            allowing(mockServ2Res1).getNextRecord();will(returnValue(0));
            allowing(mockServ2Res1).getRecordsMatched();will(returnValue(0));
            allowing(mockServ2Res1).getRecordsReturned();will(returnValue(0));
            allowing(mockServ2Res1).getRecords();will(returnValue(new ArrayList<CSWRecord>()));


            //Service 3 will make one requests. The first for 3 records will be fulfilled
            oneOf(mockFilterService).getFilteredRecords(
                    with(equal("service3")),
                    with(new CSWGetDataRecordsFilterMatcher(null,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(any(Integer.class)),
                    with(equal(10)));
            inSequence(serv3Sequence);
            will(returnValue(mockServ3Res1));
            allowing(mockServ3Res1).getNextRecord();will(returnValue(0));
            allowing(mockServ3Res1).getRecordsMatched();will(returnValue(0));
            allowing(mockServ3Res1).getRecordsReturned();will(returnValue(0));
            allowing(mockServ3Res1).getRecords();will(returnValue(new ArrayList<CSWRecord>()));
        }});

        FacetedMultiSearchResponse response = localFilterService.getFilteredRecords(serviceIds, null, facets, startIndexes, RECORD_REQUEST_COUNT);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getNextIndexes());
        Assert.assertNotNull(response.getRecords());
        Assert.assertNotNull(response.getStartIndexes());

        Assert.assertEquals(Integer.valueOf(0), response.getNextIndexes().get("service1"));
        Assert.assertEquals(Integer.valueOf(0), response.getNextIndexes().get("service2"));
        Assert.assertEquals(Integer.valueOf(0), response.getNextIndexes().get("service3"));

        Assert.assertEquals(Integer.valueOf(1), response.getStartIndexes().get("service1"));
        Assert.assertEquals(Integer.valueOf(100), response.getStartIndexes().get("service2"));
        Assert.assertEquals(Integer.valueOf(10), response.getStartIndexes().get("service3"));

        Assert.assertEquals(0, response.getRecords().size());
    }

    /**
     * Tests that the underlying implementation correctly redistributes fulfillment across services the remaining services, even when an exception is thrown
     * @throws Exception
     */
    // Carsten: Commented out due to issues: This test has a degree of non-determination and not all valid sequences of events 
    //          result in the test passing. This gives frequent false positives during unit tests. Commented out until fixed.
    //    @Test
    //   
    public void testGetMultiFilteredRecords_HandleException() throws Exception {
        final int RECORD_REQUEST_COUNT = 11;
        final String[] serviceIds = new String[] {"service1", "service2", "service3"};
        final HashMap<String, Integer> startIndexes = new HashMap<String, Integer>();
        startIndexes.put("service1", 1);
        startIndexes.put("service2", 100);
        startIndexes.put("service3", 10);

        final List<SearchFacet<? extends Object>> facets = Arrays.asList(
                new SearchFacet<String>("kw1", "keyword", Comparison.Equal),
                new SearchFacet<String>("kw2", "keyword", Comparison.Equal));

        final CSWGetRecordResponse mockServ1Res1 = context.mock(CSWGetRecordResponse.class, "mockServ1Res1");
        final CSWGetRecordResponse mockServ1Res2 = context.mock(CSWGetRecordResponse.class, "mockServ1Res2");
        final CSWGetRecordResponse mockServ2Res1 = context.mock(CSWGetRecordResponse.class, "mockServ2Res1");
        final CSWGetRecordResponse mockServ3Res1 = context.mock(CSWGetRecordResponse.class, "mockServ3Res1");
        final CSWGetRecordResponse mockServ3Res2 = context.mock(CSWGetRecordResponse.class, "mockServ3Res2");

        final Sequence serv1Sequence = context.sequence("serv1Sequence");
        final Sequence serv2Sequence = context.sequence("serv2Sequence");
        final Sequence serv3Sequence = context.sequence("serv3Sequence");

        context.checking(new Expectations() {{
            //Service 1 will make three requests. The first 4 records and then a redistributed 2 and then throw a exception
            oneOf(mockFilterService).getFilteredRecords(
                    with(equal("service1")),
                    with(new CSWGetDataRecordsFilterMatcher(null,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(4)),
                    with(equal(1)));
            inSequence(serv1Sequence);
            will(returnValue(mockServ1Res1));

            allowing(mockServ1Res1).getNextRecord();will(returnValue(5));
            allowing(mockServ1Res1).getRecordsMatched();will(returnValue(88));
            allowing(mockServ1Res1).getRecordsReturned();will(returnValue(4));
            allowing(mockServ1Res1).getRecords();will(returnValue(Arrays.asList(new CSWRecord("s1rec1"), new CSWRecord("s1rec2"), new CSWRecord("s1rec3"), new CSWRecord("s1rec4"))));

            oneOf(mockFilterService).getFilteredRecords(
                    with(equal("service1")),
                    with(new CSWGetDataRecordsFilterMatcher(null,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(2)),
                    with(equal(5)));
            inSequence(serv1Sequence);
            will(returnValue(mockServ1Res2));
            allowing(mockServ1Res2).getNextRecord();will(returnValue(7));
            allowing(mockServ1Res2).getRecordsMatched();will(returnValue(88));
            allowing(mockServ1Res2).getRecordsReturned();will(returnValue(2));
            allowing(mockServ1Res2).getRecords();will(returnValue(Arrays.asList(new CSWRecord("s1rec5"), new CSWRecord("s1rec6"))));

            oneOf(mockFilterService).getFilteredRecords(
                    with(equal("service1")),
                    with(new CSWGetDataRecordsFilterMatcher(null,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(1)),
                    with(equal(7)));
            inSequence(serv1Sequence);
            will(throwException(new IOException("mock IO exception")));

            //Service 2 will make one request for four records and then return 0
            oneOf(mockFilterService).getFilteredRecords(
                    with(equal("service2")),
                    with(new CSWGetDataRecordsFilterMatcher(null,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(4)),
                    with(equal(100)));
            inSequence(serv2Sequence);
            will(returnValue(mockServ2Res1));
            allowing(mockServ2Res1).getNextRecord();will(returnValue(0));
            allowing(mockServ2Res1).getRecordsMatched();will(returnValue(0));
            allowing(mockServ2Res1).getRecordsReturned();will(returnValue(0));
            allowing(mockServ2Res1).getRecords();will(returnValue(new ArrayList<CSWRecord>()));


            //Service 3 will make one two requests. The first for 3 records will be fulfilled, the second redistributed request for 2 will only be half fulfilled
            oneOf(mockFilterService).getFilteredRecords(
                    with(equal("service3")),
                    with(new CSWGetDataRecordsFilterMatcher(null,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(3)),
                    with(equal(10)));
            inSequence(serv3Sequence);
            will(returnValue(mockServ3Res1));
            allowing(mockServ3Res1).getNextRecord();will(returnValue(14));
            allowing(mockServ3Res1).getRecordsMatched();will(returnValue(4));
            allowing(mockServ3Res1).getRecordsReturned();will(returnValue(3));
            allowing(mockServ3Res1).getRecords();will(returnValue(Arrays.asList(new CSWRecord("s3rec1"), new CSWRecord("s3rec2"), new CSWRecord("s3rec3"))));

            oneOf(mockFilterService).getFilteredRecords(
                    with(equal("service3")),
                    with(new CSWGetDataRecordsFilterMatcher(null,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(2)),
                    with(equal(13)));
            inSequence(serv3Sequence);
            will(returnValue(mockServ3Res2));
            allowing(mockServ3Res2).getNextRecord();will(returnValue(0));
            allowing(mockServ3Res2).getRecordsMatched();will(returnValue(1));
            allowing(mockServ3Res2).getRecordsReturned();will(returnValue(1));
            allowing(mockServ3Res2).getRecords();will(returnValue(Arrays.asList(new CSWRecord("s3rec4"))));
        }});

        FacetedMultiSearchResponse response = localFilterService.getFilteredRecords(serviceIds, null, facets, startIndexes, RECORD_REQUEST_COUNT);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getNextIndexes());
        Assert.assertNotNull(response.getRecords());
        Assert.assertNotNull(response.getStartIndexes());

        Assert.assertEquals(Integer.valueOf(7), response.getNextIndexes().get("service1"));
        Assert.assertEquals(Integer.valueOf(0), response.getNextIndexes().get("service2"));
        Assert.assertEquals(Integer.valueOf(0), response.getNextIndexes().get("service3"));

        Assert.assertEquals(Integer.valueOf(1), response.getStartIndexes().get("service1"));
        Assert.assertEquals(Integer.valueOf(100), response.getStartIndexes().get("service2"));
        Assert.assertEquals(Integer.valueOf(10), response.getStartIndexes().get("service3"));

        Assert.assertEquals(10, response.getRecords().size());
        Assert.assertEquals("s1rec1", response.getRecords().get(0).getFileIdentifier());
    }
}

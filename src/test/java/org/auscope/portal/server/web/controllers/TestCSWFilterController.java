package org.auscope.portal.server.web.controllers;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.auscope.portal.CSWGetDataRecordsFilterMatcher;
import org.auscope.portal.csw.CSWGetDataRecordsFilter.KeywordMatchType;
import org.auscope.portal.csw.CSWGetRecordResponse;
import org.auscope.portal.csw.record.CSWRecord;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.web.service.CSWFilterService;
import org.auscope.portal.server.web.service.CSWServiceItem;
import org.auscope.portal.server.web.view.ViewCSWRecordFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

/**
 * Unit tests for CSWFilterController
 * @author Josh Vote
 *
 */
public class TestCSWFilterController {
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    private ViewCSWRecordFactory mockViewRecordFactory;
    private CSWFilterService mockService;
    private CSWFilterController controller;

    /**
     * Initialise our unit tests
     */
    @Before
    public void init() {
        mockViewRecordFactory = context.mock(ViewCSWRecordFactory.class);
        mockService = context.mock(CSWFilterService.class);
        controller = new CSWFilterController(mockService, mockViewRecordFactory);
    }

    private static CSWGetDataRecordsFilterMatcher aCSWFilter(FilterBoundingBox spatialBounds,
            String[] keywords, String capturePlatform, String sensor, KeywordMatchType matchType) {
        return new CSWGetDataRecordsFilterMatcher(spatialBounds,keywords, capturePlatform, sensor, matchType);
    }

    /**
     * Tests that requesting filtered records relies correctly on all dependencies
     * @throws Exception
     */
    @Test
    public void testGetFilteredRecords() throws Exception {
        final String anyText = "any-text";
        final String cswServiceId = "my-csw-service-id";
        final double east = 0.1;
        final double west = 5.5;
        final double north = 4.8;
        final double south = 8.6;
        final String[] keywords = new String[] {"kw1", "kw2"};
        final String capturePlatform = "capturePlatform";
        final String sensor = "sensor";
        final Integer maxRecords = 123;
        final FilterBoundingBox expectedBBox = new FilterBoundingBox("",
                new double[] {east, south},
                new double[] {west, north});
        final CSWRecord[] filteredRecs = new CSWRecord[] {
                context.mock(CSWRecord.class, "cswRecord1"),
                context.mock(CSWRecord.class, "cswRecord2"),
                context.mock(CSWRecord.class, "cswRecord3")
        };
        final CSWGetRecordResponse filteredResponse = context.mock(CSWGetRecordResponse.class, "cswResponse1");

        final ModelMap mockViewRec1 = context.mock(ModelMap.class, "mockViewRec1");
        final ModelMap mockViewRec2 = context.mock(ModelMap.class, "mockViewRec2");
        final ModelMap mockViewRec3 = context.mock(ModelMap.class, "mockViewRec3");
        final int response1RecordsMatched = 442;
        final KeywordMatchType matchType = KeywordMatchType.All;
        final Integer startPosition = 3;

        context.checking(new Expectations() {{
            oneOf(mockService).getFilteredRecords(with(equal(cswServiceId)), with(aCSWFilter(expectedBBox, keywords, capturePlatform, sensor, matchType)), with(equal(maxRecords)), with(equal(startPosition + 1)));will(returnValue(filteredResponse));

            oneOf(filteredResponse).getRecordsMatched();will(returnValue(response1RecordsMatched));
            oneOf(filteredResponse).getRecords();will(returnValue(Arrays.asList(filteredRecs[0], filteredRecs[1], filteredRecs[2])));

            oneOf(mockViewRecordFactory).toView(filteredRecs[0]);will(returnValue(mockViewRec1));
            oneOf(mockViewRecordFactory).toView(filteredRecs[1]);will(returnValue(mockViewRec2));
            oneOf(mockViewRecordFactory).toView(filteredRecs[2]);will(returnValue(mockViewRec3));
        }});

        ModelAndView mav = controller.getFilteredCSWRecords(cswServiceId, anyText, west, east, north, south, keywords, KeywordMatchType.All, capturePlatform, sensor, maxRecords, startPosition);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        Collection<ModelMap> dataRecs = (Collection<ModelMap>) mav.getModel().get("data");
        Assert.assertNotNull(dataRecs);
        Assert.assertTrue(dataRecs.contains(mockViewRec1));
        Assert.assertTrue(dataRecs.contains(mockViewRec2));
        Assert.assertTrue(dataRecs.contains(mockViewRec3));
        Assert.assertEquals(3, dataRecs.size());

        Assert.assertEquals(response1RecordsMatched, mav.getModel().get("totalResults"));
    }

    /**
     * Tests that requesting filtered records (with no service id) relies correctly on all dependencies
     * @throws Exception
     */
    @Test
    public void testGetFilteredRecordsNoServiceID() throws Exception {
        final String anyText = "any-text";
        final double east = 0.1;
        final double west = 5.5;
        final double north = 4.8;
        final double south = 8.6;
        final String[] keywords = new String[] {"kw1", "kw2"};
        final String capturePlatform = "capturePlatform";
        final String sensor = "sensor";
        final Integer maxRecords = 123;
        final FilterBoundingBox expectedBBox = new FilterBoundingBox("",
                new double[] {east, south},
                new double[] {west, north});
        final CSWRecord[] filteredRecs = new CSWRecord[] {
                context.mock(CSWRecord.class, "cswRecord1"),
                context.mock(CSWRecord.class, "cswRecord2"),
                context.mock(CSWRecord.class, "cswRecord3")
        };
        final CSWGetRecordResponse[] filteredResponses = new CSWGetRecordResponse[] {
                context.mock(CSWGetRecordResponse.class, "cswResponse1"),
                context.mock(CSWGetRecordResponse.class, "cswResponse2"),
        };
        final ModelMap mockViewRec1 = context.mock(ModelMap.class, "mockViewRec1");
        final ModelMap mockViewRec2 = context.mock(ModelMap.class, "mockViewRec2");
        final ModelMap mockViewRec3 = context.mock(ModelMap.class, "mockViewRec3");
        final int response1RecordsMatched = 234;
        final int response2RecordsMatched = 723;
        final KeywordMatchType matchType = KeywordMatchType.All;

        context.checking(new Expectations() {{
            oneOf(mockService).getFilteredRecords(with(aCSWFilter(expectedBBox, keywords, capturePlatform, sensor, matchType)), with(equal(maxRecords)));will(returnValue(filteredResponses));

            oneOf(filteredResponses[0]).getRecordsMatched();will(returnValue(response1RecordsMatched));
            oneOf(filteredResponses[1]).getRecordsMatched();will(returnValue(response2RecordsMatched));
            oneOf(filteredResponses[0]).getRecords();will(returnValue(Arrays.asList(filteredRecs[0])));
            oneOf(filteredResponses[1]).getRecords();will(returnValue(Arrays.asList(filteredRecs[1], filteredRecs[2])));

            oneOf(mockViewRecordFactory).toView(filteredRecs[0]);will(returnValue(mockViewRec1));
            oneOf(mockViewRecordFactory).toView(filteredRecs[1]);will(returnValue(mockViewRec2));
            oneOf(mockViewRecordFactory).toView(filteredRecs[2]);will(returnValue(mockViewRec3));
        }});

        ModelAndView mav = controller.getFilteredCSWRecords(null, anyText, west, east, north, south, keywords, KeywordMatchType.All, capturePlatform, sensor, maxRecords, null);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        Collection<ModelMap> dataRecs = (Collection<ModelMap>) mav.getModel().get("data");
        Assert.assertNotNull(dataRecs);
        Assert.assertTrue(dataRecs.contains(mockViewRec1));
        Assert.assertTrue(dataRecs.contains(mockViewRec2));
        Assert.assertTrue(dataRecs.contains(mockViewRec3));
        Assert.assertEquals(3, dataRecs.size());

        Assert.assertEquals(response1RecordsMatched + response2RecordsMatched, mav.getModel().get("totalResults"));
    }

    /**
     * Tests that requesting filtered records fails gracefully
     * @throws Exception
     */
    @Test
    public void testGetFilteredRecordsError() throws Exception {
        final String anyText = "any-text";
        final String cswServiceId = "my-csw-service-id";
        final double east = 0.1;
        final double west = 5.5;
        final double north = 4.8;
        final double south = 8.6;
        final String[] keywords = new String[] {"kw1", "kw2"};
        final String capturePlatform = "capturePlatform";
        final String sensor = "sensor";
        final Integer maxRecords = 123;
        final FilterBoundingBox expectedBBox = new FilterBoundingBox("",
                new double[] {east, south},
                new double[] {west, north});
        final KeywordMatchType matchType = KeywordMatchType.Any;
        final Integer startPosition = 0;

        context.checking(new Expectations() {{
            //Throw an error
            oneOf(mockService).getFilteredRecords(with(equal(cswServiceId)), with(aCSWFilter(expectedBBox, keywords, capturePlatform, sensor, matchType)), with(equal(maxRecords)), with(equal(startPosition + 1)));will(throwException(new Exception()));
        }});

        ModelAndView mav = controller.getFilteredCSWRecords(cswServiceId, anyText, west, east, north, south, keywords, matchType, capturePlatform, sensor, maxRecords, startPosition);
        Assert.assertNotNull(mav);
        Assert.assertFalse((Boolean) mav.getModel().get("success"));
    }

    /**
     * Tests that requesting filtered records relies correctly on all dependencies when all optional
     * parameters are omitted
     * @throws Exception
     */
    @Test
    public void testGetFilteredRecordsOptionalParams() throws Exception {
        final String anyText = null;
        final String cswServiceId = null;
        final Double east = null;
        final Double west = null;
        final Double north = null;
        final Double south = null;
        final String[] keywords = null;
        final String capturePlatform = null;
        final String sensor = null;
        final FilterBoundingBox expectedBBox = null;
        final Integer maxRecords = null;
        final CSWRecord[] filteredRecs = new CSWRecord[] {
                context.mock(CSWRecord.class, "cswRecord1"),
                context.mock(CSWRecord.class, "cswRecord2"),
                context.mock(CSWRecord.class, "cswRecord3")
        };
        final CSWGetRecordResponse[] filteredResponses = new CSWGetRecordResponse[] {
                context.mock(CSWGetRecordResponse.class, "cswResponse1"),
                context.mock(CSWGetRecordResponse.class, "cswResponse2"),
        };
        final ModelMap mockViewRec1 = context.mock(ModelMap.class, "mockViewRec1");
        final ModelMap mockViewRec2 = context.mock(ModelMap.class, "mockViewRec2");
        final ModelMap mockViewRec3 = context.mock(ModelMap.class, "mockViewRec3");
        final KeywordMatchType matchType = null;
        final Integer startPosition = null;
        final int response1RecordsMatched = 234;
        final int response2RecordsMatched = 723;

        context.checking(new Expectations() {{
            //Throw an error
            oneOf(mockService).getFilteredRecords(with(aCSWFilter(expectedBBox, keywords, capturePlatform, sensor, matchType)), with(equal(CSWFilterController.DEFAULT_MAX_RECORDS)));will(returnValue(filteredResponses));

            oneOf(filteredResponses[0]).getRecordsMatched();will(returnValue(response1RecordsMatched));
            oneOf(filteredResponses[1]).getRecordsMatched();will(returnValue(response2RecordsMatched));
            oneOf(filteredResponses[0]).getRecords();will(returnValue(Arrays.asList(filteredRecs[0])));
            oneOf(filteredResponses[1]).getRecords();will(returnValue(Arrays.asList(filteredRecs[1], filteredRecs[2])));

            oneOf(mockViewRecordFactory).toView(filteredRecs[0]);will(returnValue(mockViewRec1));
            oneOf(mockViewRecordFactory).toView(filteredRecs[1]);will(returnValue(mockViewRec2));
            oneOf(mockViewRecordFactory).toView(filteredRecs[2]);will(returnValue(mockViewRec3));
        }});

        ModelAndView mav = controller.getFilteredCSWRecords(cswServiceId, anyText, west, east, north, south, keywords, matchType, capturePlatform, sensor, maxRecords, startPosition);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        Collection<ModelMap> dataRecs = (Collection<ModelMap>) mav.getModel().get("data");
        Assert.assertNotNull(dataRecs);
        Assert.assertTrue(dataRecs.contains(mockViewRec1));
        Assert.assertTrue(dataRecs.contains(mockViewRec2));
        Assert.assertEquals(3, dataRecs.size());
        Assert.assertEquals(response1RecordsMatched + response2RecordsMatched, mav.getModel().get("totalResults"));
    }

    /**
     * Tests that requesting filtered count relies correctly on all dependencies
     * @throws Exception
     */
    @Test
    public void testGetFilteredRecordsCountSingleCSW() throws Exception {
        final String anyText = "any-text";
        final String cswServiceId = "my-csw-service-id";
        final double east = 0.1;
        final double west = 5.5;
        final double north = 4.8;
        final double south = 8.6;
        final String[] keywords = new String[] {"kw1", "kw2"};
        final String capturePlatform = "capturePlatform";
        final String sensor = "sensor";
        final FilterBoundingBox expectedBBox = new FilterBoundingBox("",
                new double[] {east, south},
                new double[] {west, north});
        final Integer expectedCount = 15;
        final Integer maxRecords = 123;
        final KeywordMatchType matchType = KeywordMatchType.All;

        context.checking(new Expectations() {{
            oneOf(mockService).getFilteredRecordsCount(with(equal(cswServiceId)), with(aCSWFilter(expectedBBox, keywords, capturePlatform, sensor, matchType)), with(equal(maxRecords)));will(returnValue(expectedCount));
        }});

        ModelAndView mav = controller.getFilteredCSWRecordsCount(cswServiceId, anyText, west, east, north, south, keywords, matchType, capturePlatform, sensor, maxRecords);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        Assert.assertEquals(expectedCount, (Integer) mav.getModel().get("data"));
    }

    /**
     * Tests that requesting filtered count relies correctly on all dependencies
     * @throws Exception
     */
    @Test
    public void testGetFilteredRecordsCountAllCSWs() throws Exception {
        final String anyText = "any-text";
        final String cswServiceId = null;
        final double east = 0.1;
        final double west = 5.5;
        final double north = 4.8;
        final double south = 8.6;
        final String[] keywords = new String[] {"kw1", "kw2"};
        final String capturePlatform = "capturePlatform";
        final String sensor = "sensor";
        final FilterBoundingBox expectedBBox = new FilterBoundingBox("",
                new double[] {east, south},
                new double[] {west, north});
        final Integer expectedCount = 15;
        final Integer maxRecords = 123;
        final KeywordMatchType matchType = KeywordMatchType.All;

        context.checking(new Expectations() {{
            oneOf(mockService).getFilteredRecordsCount(with(aCSWFilter(expectedBBox, keywords, capturePlatform, sensor, matchType)), with(equal(maxRecords)));will(returnValue(expectedCount));
        }});

        ModelAndView mav = controller.getFilteredCSWRecordsCount(cswServiceId, anyText, west, east, north, south, keywords, matchType, capturePlatform, sensor, maxRecords);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        Assert.assertEquals(expectedCount, (Integer) mav.getModel().get("data"));
    }

    /**
     * Tests that requesting filtered count relies correctly on all dependencies
     * when all optional parameters are omitted
     * @throws Exception
     */
    @Test
    public void testGetFilteredRecordsCountOptionalParams() throws Exception {
        final String anyText = null;
        final String cswServiceId = null;
        final Double east = null;
        final Double west = null;
        final Double north = null;
        final Double south = null;
        final String[] keywords = null;
        final String capturePlatform = null;
        final String sensor = null;
        final FilterBoundingBox expectedBBox = null;
        final Integer expectedCount = 15;
        final Integer maxRecords = null;
        final KeywordMatchType matchType = null;

        context.checking(new Expectations() {{
            oneOf(mockService).getFilteredRecordsCount(with(aCSWFilter(expectedBBox, keywords, capturePlatform, sensor, matchType)), with(equal(0)));will(returnValue(expectedCount));
        }});

        ModelAndView mav = controller.getFilteredCSWRecordsCount(cswServiceId, anyText, west, east, north, south, keywords, matchType, capturePlatform, sensor, maxRecords);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        Assert.assertEquals(expectedCount, (Integer) mav.getModel().get("data"));
    }

    /**
     * Tests that requesting filtered count relies correctly on all dependencies
     * when all optional parameters are omitted
     * @throws Exception
     */
    @Test
    public void testGetFilteredRecordsCountError() throws Exception {
        final String anyText = "any-text";
        final String cswServiceId = "my-csw-service-id";
        final double east = 0.1;
        final double west = 5.5;
        final double north = 4.8;
        final double south = 8.6;
        final String[] keywords = new String[] {"kw1", "kw2"};
        final String capturePlatform = "capturePlatform";
        final String sensor = "sensor";
        final FilterBoundingBox expectedBBox = new FilterBoundingBox("",
                new double[] {east, south},
                new double[] {west, north});
        final Integer maxRecords = 13;
        final KeywordMatchType matchType = KeywordMatchType.All;

        context.checking(new Expectations() {{
            oneOf(mockService).getFilteredRecordsCount(with(equal(cswServiceId)), with(aCSWFilter(expectedBBox, keywords, capturePlatform, sensor, matchType)), with(equal(maxRecords)));will(throwException(new Exception()));
        }});

        ModelAndView mav = controller.getFilteredCSWRecordsCount(cswServiceId, anyText, west, east, north, south, keywords, matchType, capturePlatform, sensor, maxRecords);
        Assert.assertNotNull(mav);
        Assert.assertFalse((Boolean) mav.getModel().get("success"));
    }

    /**
     * Tests that requests for the internal CSWService list get modelled correctly
     * @throws Exception
     */
    @Test
    public void testGetCSWServices() throws Exception {
        final CSWServiceItem[] expected = {
                new CSWServiceItem("id1", "serviceUrl1", "infoUrl1", "title1"),
                new CSWServiceItem("id2", "serviceUrl2", "infoUrl2", "title2")
        };

        context.checking(new Expectations() {{
            oneOf(mockService).getCSWServiceItems();will(returnValue(expected));
        }});

        ModelAndView mav = controller.getCSWServices();
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));

        List<ModelMap> actual = (List<ModelMap>) mav.getModel().get("data");
        Assert.assertNotNull(actual);

        Assert.assertEquals(2, actual.size());
        Assert.assertEquals("id1", actual.get(0).get("id"));
        Assert.assertEquals("serviceUrl1", actual.get(0).get("url"));
        Assert.assertEquals("title1", actual.get(0).get("title"));

        Assert.assertEquals("id2", actual.get(1).get("id"));
        Assert.assertEquals("serviceUrl2", actual.get(1).get("url"));
        Assert.assertEquals("title2", actual.get(1).get("title"));
    }
}

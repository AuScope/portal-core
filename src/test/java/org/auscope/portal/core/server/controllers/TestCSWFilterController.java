package org.auscope.portal.core.server.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.auscope.portal.core.server.controllers.CSWFilterController;
import org.auscope.portal.core.services.CSWFilterService;
import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.services.csw.custom.CustomRegistryInt;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.methodmakers.filter.csw.CSWGetDataRecordsFilter.KeywordMatchType;
import org.auscope.portal.core.services.responses.csw.CSWGetRecordResponse;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.jmock.CSWGetDataRecordsFilterMatcher;
import org.auscope.portal.core.view.ViewCSWRecordFactory;
import org.auscope.portal.core.view.ViewKnownLayerFactory;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

/**
 * Unit tests for CSWFilterController
 *
 * @author Josh Vote
 *
 */
public class TestCSWFilterController extends PortalTestClass {

    private ViewCSWRecordFactory mockViewRecordFactory;
    private CSWFilterService mockService;
    private CSWFilterController controller;
    private ViewKnownLayerFactory mockKnownLayerFactory;
    private CustomRegistry customRegistry;

    /**
     * Initialise our unit tests
     */
    @Before
    public void init() {
        mockViewRecordFactory = context.mock(ViewCSWRecordFactory.class);
        mockKnownLayerFactory = context.mock(ViewKnownLayerFactory.class);
        mockService = context.mock(CSWFilterService.class);
        controller = new CSWFilterController(mockService, mockViewRecordFactory, mockKnownLayerFactory);
        customRegistry = new CustomRegistry("", "", "", "");
    }

    private static CSWGetDataRecordsFilterMatcher aCSWFilter(FilterBoundingBox spatialBounds,
            String[] keywords, String capturePlatform, String sensor, KeywordMatchType matchType) {
        return new CSWGetDataRecordsFilterMatcher(spatialBounds, keywords, capturePlatform, sensor, matchType);
    }

    /**
     * Tests that requesting filtered records relies correctly on all dependencies
     *
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

        String[] key = {"anyText", "cswServiceId", "east", "west", "north", "south", "keywords", "capturePlatform",
                "sensor"};
        String[] value = {"any-text", "my-csw-service-id", "0.1", "5.5", "4.8", "8.6", "kw1,kw2", "capturePlatform",
                "sensor"};

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
        final KeywordMatchType matchType = null;
        final Integer startPosition = 3;

        context.checking(new Expectations() {
            {
                oneOf(mockService).getFilteredRecords(with(equal(cswServiceId)),
                        with(aCSWFilter(expectedBBox, keywords, capturePlatform, sensor, matchType)),
                        with(equal(maxRecords)), with(equal(startPosition + 1)));
                will(returnValue(filteredResponse));

                oneOf(filteredResponse).getRecordsMatched();
                will(returnValue(response1RecordsMatched));

                oneOf(filteredResponse).getRecords();
                will(returnValue(Arrays.asList(filteredRecs[0], filteredRecs[1], filteredRecs[2])));

                oneOf(mockViewRecordFactory).toView(filteredRecs[0]);
                will(returnValue(mockViewRec1));

                oneOf(mockViewRecordFactory).toView(filteredRecs[1]);
                will(returnValue(mockViewRec2));

                oneOf(mockViewRecordFactory).toView(filteredRecs[2]);
                will(returnValue(mockViewRec3));
            }
        });

        ModelAndView mav = controller.getFilteredCSWRecords(key, value, maxRecords, startPosition, customRegistry);
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
     * Tests that requesting filtered records (with no service id) relies correctly on all dependencies VT: I removed this test as it is no long application.
     *
     * @throws Exception
     */

    /**
     * Tests that requesting filtered records fails gracefully
     *
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

        String[] key = {"keywordMatchType", "anyText", "cswServiceId", "east", "west", "north", "south", "keywords",
                "capturePlatform", "sensor"};
        String[] value = {"any", "any-text", "my-csw-service-id", "0.1", "5.5", "4.8", "8.6", "kw1,kw2",
                "capturePlatform", "sensor"};

        final Integer maxRecords = 123;
        final FilterBoundingBox expectedBBox = new FilterBoundingBox("",
                new double[] {east, south},
                new double[] {west, north});
        final KeywordMatchType matchType = KeywordMatchType.Any;
        final Integer startPosition = 0;

        context.checking(new Expectations() {
            {
                //Throw an error
                oneOf(mockService).getFilteredRecords(with(equal(cswServiceId)),
                        with(aCSWFilter(expectedBBox, keywords, capturePlatform, sensor, matchType)),
                        with(equal(maxRecords)), with(equal(startPosition + 1)));
                will(throwException(new Exception()));
            }
        });

        ModelAndView mav = controller.getFilteredCSWRecords(key, value, maxRecords, startPosition, customRegistry);
        Assert.assertNotNull(mav);
        Assert.assertFalse((Boolean) mav.getModel().get("success"));
    }

    /**
     * Tests that requesting filtered records relies correctly on all dependencies when all optional parameters are omitted
     *
     * @throws Exception
     */
    @Test
    public void testOptionalParamRecords() throws Exception {

        final Integer maxRecords = 123;
        final String cswServiceId = "my-csw-service-id";
        final String[] keywords = null;
        final String capturePlatform = null;
        final String sensor = null;

        String[] key = {"anyText", "cswServiceId", "east", "west", "north", "south", "keywords", "capturePlatform",
                "sensor"};
        String[] value = {null, "my-csw-service-id", null, null, null, null, null, null, null};

        final FilterBoundingBox expectedBBox = null;
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
        final KeywordMatchType matchType = null;
        final Integer startPosition = 3;

        context.checking(new Expectations() {
            {
                oneOf(mockService).getFilteredRecords(with(equal(cswServiceId)),
                        with(aCSWFilter(expectedBBox, keywords, capturePlatform, sensor, matchType)),
                        with(equal(maxRecords)), with(equal(startPosition + 1)));
                will(returnValue(filteredResponse));

                oneOf(filteredResponse).getRecordsMatched();
                will(returnValue(response1RecordsMatched));

                oneOf(filteredResponse).getRecords();
                will(returnValue(Arrays.asList(filteredRecs[0], filteredRecs[1], filteredRecs[2])));

                oneOf(mockViewRecordFactory).toView(filteredRecs[0]);
                will(returnValue(mockViewRec1));

                oneOf(mockViewRecordFactory).toView(filteredRecs[1]);
                will(returnValue(mockViewRec2));

                oneOf(mockViewRecordFactory).toView(filteredRecs[2]);
                will(returnValue(mockViewRec3));
            }
        });

        ModelAndView mav = controller.getFilteredCSWRecords(key, value, maxRecords, startPosition, customRegistry);
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

    //    /**
    //     * Tests that requesting filtered records relies correctly on all dependencies when all optional
    //     * parameters are omitted
    //     * @throws Exception
    //     */
    //    @Test
    //    public void testGetFilteredRecordsOptionalParams() throws Exception {
    //        final String anyText = null;
    //        final String cswServiceId = "my-csw-service-id";
    //        final Double east = null;
    //        final Double west = null;
    //        final Double north = null;
    //        final Double south = null;
    //        final String[] keywords = null;
    //        final String capturePlatform = null;
    //        final String sensor = null;
    //
    //        String [] key={"anyText","cswServiceId","east","west","north","south","keywords","capturePlatform","sensor"};
    //        String [] value={null,cswServiceId,null,null,null,null,null,null,null};
    //
    //        final FilterBoundingBox expectedBBox = null;
    //        final Integer maxRecords = null;
    //        final CSWRecord[] filteredRecs = new CSWRecord[] {
    //                context.mock(CSWRecord.class, "cswRecord1"),
    //                context.mock(CSWRecord.class, "cswRecord2"),
    //                context.mock(CSWRecord.class, "cswRecord3")
    //        };
    //        final CSWGetRecordResponse filteredResponses = context.mock(CSWGetRecordResponse.class, "cswResponse1");
    //        final ModelMap mockViewRec1 = context.mock(ModelMap.class, "mockViewRec1");
    //        final ModelMap mockViewRec2 = context.mock(ModelMap.class, "mockViewRec2");
    //        final ModelMap mockViewRec3 = context.mock(ModelMap.class, "mockViewRec3");
    //        final KeywordMatchType matchType = null;
    //        final Integer startPosition = 0;
    //        final int response1RecordsMatched = 234;
    //        final int response2RecordsMatched = 723;
    //
    //        context.checking(new Expectations() {{
    //            //Throw an error
    //            oneOf(mockService).getFilteredRecords(with(equal(cswServiceId)),with(aCSWFilter(expectedBBox, keywords, capturePlatform, sensor, matchType)), with(equal(CSWFilterController.DEFAULT_MAX_RECORDS)),with(equal(startPosition + 1)));will(returnValue(filteredResponses));
    //
    //            oneOf(filteredResponses[0]).getRecordsMatched();
    //            will(returnValue(response1RecordsMatched));
    //
    //            oneOf(filteredResponses[1]).getRecordsMatched();
    //            will(returnValue(response2RecordsMatched));
    //
    //            oneOf(filteredResponses[0]).getRecords();
    //            will(returnValue(Arrays.asList(filteredRecs[0])));
    //
    //            oneOf(filteredResponses[1]).getRecords();
    //            will(returnValue(Arrays.asList(filteredRecs[1], filteredRecs[2])));
    //
    //            oneOf(mockViewRecordFactory).toView(filteredRecs[0]);
    //            will(returnValue(mockViewRec1));
    //            oneOf(mockViewRecordFactory).toView(filteredRecs[1]);
    //            will(returnValue(mockViewRec2));
    //            oneOf(mockViewRecordFactory).toView(filteredRecs[2]);
    //            will(returnValue(mockViewRec3));
    //        }});
    //
    //        ModelAndView mav = controller.getFilteredCSWRecords(key,value, maxRecords, startPosition,customRegistry);
    //        Assert.assertNotNull(mav);
    //        Assert.assertTrue((Boolean) mav.getModel().get("success"));
    //        Collection<ModelMap> dataRecs = (Collection<ModelMap>) mav.getModel().get("data");
    //        Assert.assertNotNull(dataRecs);
    //        Assert.assertTrue(dataRecs.contains(mockViewRec1));
    //        Assert.assertTrue(dataRecs.contains(mockViewRec2));
    //        Assert.assertEquals(3, dataRecs.size());
    //        Assert.assertEquals(response1RecordsMatched + response2RecordsMatched, mav.getModel().get("totalResults"));
    //    }

    /**
     * Tests that requesting filtered count relies correctly on all dependencies
     *
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

        context.checking(new Expectations() {
            {
                oneOf(mockService).getFilteredRecordsCount(with(equal(cswServiceId)),
                        with(aCSWFilter(expectedBBox, keywords, capturePlatform, sensor, matchType)),
                        with(equal(maxRecords)));
                will(returnValue(expectedCount));
            }
        });

        ModelAndView mav = controller.getFilteredCSWRecordsCount(cswServiceId, anyText, west, east, north, south,
                keywords, matchType, capturePlatform, sensor, maxRecords);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        Assert.assertEquals(expectedCount, (Integer) mav.getModel().get("data"));
    }

    /**
     * Tests that requesting filtered count relies correctly on all dependencies
     *
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

        context.checking(new Expectations() {
            {
                oneOf(mockService).getFilteredRecordsCount(
                        with(aCSWFilter(expectedBBox, keywords, capturePlatform, sensor, matchType)),
                        with(equal(maxRecords)));
                will(returnValue(expectedCount));
            }
        });

        ModelAndView mav = controller.getFilteredCSWRecordsCount(cswServiceId, anyText, west, east, north, south,
                keywords, matchType, capturePlatform, sensor, maxRecords);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        Assert.assertEquals(expectedCount, (Integer) mav.getModel().get("data"));
    }

    /**
     * Tests that requesting filtered count relies correctly on all dependencies when all optional parameters are omitted
     *
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

        context.checking(new Expectations() {
            {
                oneOf(mockService).getFilteredRecordsCount(
                        with(aCSWFilter(expectedBBox, keywords, capturePlatform, sensor, matchType)), with(equal(0)));
                will(returnValue(expectedCount));
            }
        });

        ModelAndView mav = controller.getFilteredCSWRecordsCount(cswServiceId, anyText, west, east, north, south,
                keywords, matchType, capturePlatform, sensor, maxRecords);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        Assert.assertEquals(expectedCount, (Integer) mav.getModel().get("data"));
    }

    /**
     * Tests that requesting filtered count relies correctly on all dependencies when all optional parameters are omitted
     *
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

        context.checking(new Expectations() {
            {
                oneOf(mockService).getFilteredRecordsCount(with(equal(cswServiceId)),
                        with(aCSWFilter(expectedBBox, keywords, capturePlatform, sensor, matchType)),
                        with(equal(maxRecords)));
                will(throwException(new Exception()));
            }
        });

        ModelAndView mav = controller.getFilteredCSWRecordsCount(cswServiceId, anyText, west, east, north, south,
                keywords, matchType, capturePlatform, sensor, maxRecords);
        Assert.assertNotNull(mav);
        Assert.assertFalse((Boolean) mav.getModel().get("success"));
    }

    /**
     * Tests that requests for the internal CSWService list get modelled correctly
     *
     * @throws Exception
     */
    @Test
    public void testGetCSWServices() throws Exception {
        final CSWServiceItem[] expected = {
                new CSWServiceItem("id1", "serviceUrl1", "infoUrl1", "title1"),
                new CSWServiceItem("id2", "serviceUrl2", "infoUrl2", "title2")
        };

        context.checking(new Expectations() {
            {
                oneOf(mockService).getCSWServiceItems();
                will(returnValue(expected));
            }
        });

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

    /**
     * Test that getFilteredCSWKeywords returns the keywords cache as expected.
     *
     * @throws Exception
     */
    @Test
    public void testGetFilteredCSWKeywords() throws Exception {

        final String[] cswServiceIds = {"serviceIdDNE"};
        final ArrayList<CSWRecord> list = new ArrayList<CSWRecord>();
        final CSWGetRecordResponse filteredResponse = context.mock(CSWGetRecordResponse.class, "cswResponse1");

        CSWRecord c1 = new CSWRecord("c1");
        c1.setDescriptiveKeywords(new String[] {"kw1", "kw2", "kw3"});
        CSWRecord c2 = new CSWRecord("c2");
        list.add(c1);
        c2.setDescriptiveKeywords(new String[] {"kw1", "kw2"});
        list.add(c2);
        CSWRecord c3 = new CSWRecord("c3");
        c3.setDescriptiveKeywords(new String[] {"kw1"});
        list.add(c3);

        context.checking(new Expectations() {
            {
                oneOf(mockService).getCapabilitiesByServiceId(cswServiceIds[0]);
                //oneOf(mockService).getFilteredRecords(with(equal(cswServiceIds[0])),with(equal(null)),with(equal(CSWFilterController.DEFAULT_MAX_RECORDS)),with(equal(1)));will(returnValue(filteredResponse));
                oneOf(mockService).getFilteredRecords(cswServiceIds[0], null, CSWFilterController.DEFAULT_MAX_RECORDS,
                        1);
                will(returnValue(filteredResponse));
                oneOf(filteredResponse).getRecords();
                will(returnValue(list));

                exactly(2).of(filteredResponse).getNextRecord();
                will(returnValue(0));

                oneOf(filteredResponse).getRecordsMatched();
                will(returnValue(3));

            }
        });

        ModelAndView mav = controller.getFilteredCSWKeywords(cswServiceIds, "");
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        List<ModelMap> actual = (List<ModelMap>) mav.getModel().get("data");
        Assert.assertNotNull(actual);
        Assert.assertEquals(3, actual.size());

        //This is so we arent sensitive to return order
        String[] kwNames =  new String[] {"kw1", "kw2", "kw3"};
        for (ModelMap map : actual) {
            String keyword = (String) map.get("keyword");
            Assert.assertTrue(Arrays.asList(kwNames).contains(keyword));

        }

        //VT: since this is a static variable, we clean this up incase it corrupts other test cases.
        CSWFilterController.catalogueKeywordCache.clear();
    }

    /**
     * Test that getFilteredCSWKeywords returns the keywords cache as expected.
     *
     * @throws Exception
     */
    @Test
    public void testGetFilteredCSWKeywordsEmptyCSWID() throws Exception {

        final String[] cswServiceIds = {"serviceIdDNE"};
        final ArrayList<CSWRecord> list = new ArrayList<CSWRecord>();
        final CSWGetRecordResponse filteredResponse = context.mock(CSWGetRecordResponse.class, "cswResponse1");

        ModelAndView mav = controller.getFilteredCSWKeywords(null, "");
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        Assert.assertEquals(null, mav.getModel().get("data"));

    }

    private final class CustomRegistry implements CustomRegistryInt {
        private String id;
        private String title, serviceUrl;
        private String recordInformationUrl;

        public CustomRegistry(String id, String title, String serviceUrl, String recordInformationUrl) {
            this.setId(id);
            this.setTitle(title);
            if (!serviceUrl.endsWith("csw")) {
                this.setServiceUrl(serviceUrl + "/csw");
            } else {
                this.setServiceUrl(serviceUrl);
            }

            if (!recordInformationUrl.endsWith("uuid=%1$s")) {
                this.setRecordInformationUrl(recordInformationUrl + "?uuid=%1$s");
            } else {
                this.setRecordInformationUrl(recordInformationUrl);
            }
        }

        public CustomRegistry(String[] registryInfo) {
            this(registryInfo[0], registryInfo[1], registryInfo[2], registryInfo[3]);
        }

        @Override
        public boolean isEmpty() {
            //VT: All info are crucial therefore we don't recognize this registry if it is missing any information.
            if (id.isEmpty() || title.isEmpty() || serviceUrl.isEmpty() || recordInformationUrl.isEmpty()) {
                return true;
            } else {
                return false;
            }

        }

        /**
         * @return the recordInformationUrl
         */
        public String getRecordInformationUrl() {
            return recordInformationUrl;
        }

        /**
         * @param recordInformationUrl
         *            the recordInformationUrl to set
         */
        public void setRecordInformationUrl(String recordInformationUrl) {
            this.recordInformationUrl = recordInformationUrl;
        }

        /**
         * @return the serviceUrl
         */
        public String getServiceUrl() {
            return serviceUrl;
        }

        /**
         * @param serviceUrl
         *            the serviceUrl to set
         */
        public void setServiceUrl(String serviceUrl) {
            this.serviceUrl = serviceUrl;
        }

        /**
         * @return the title
         */
        public String getTitle() {
            return title;
        }

        /**
         * @param title
         *            the title to set
         */
        public void setTitle(String title) {
            this.title = title;
        }

        /**
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * @param id
         *            the id to set
         */
        public void setId(String id) {
            this.id = id;
        }

    }
}

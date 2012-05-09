package org.auscope.portal.mineraloccurrence;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.auscope.portal.PortalTestClass;
import org.auscope.portal.Util;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.domain.wfs.WFSCountResponse;
import org.auscope.portal.server.util.GmlToHtml;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker.ResultType;
import org.auscope.portal.server.web.service.BaseWFSService;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.web.service.MineralOccurrenceService;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: Mathew Wyatt
 * Date: Jun 4, 2009
 * @version: $Id$
 */
public class TestMineralOccurrenceServiceClient extends PortalTestClass {
    private MineralOccurrenceService mineralOccurrenceService;
    private HttpServiceCaller httpServiceCaller;
    private MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler;
    private HttpClient mockHttpClient;
    private GmlToKml mockGmlToKml;
    private GmlToHtml mockGmlToHtml;

    private WFSGetFeatureMethodMaker methodMaker;


    @Before
    public void setUp() {
        this.methodMaker = context.mock(WFSGetFeatureMethodMaker.class);
        this.mineralOccurrencesResponseHandler = context.mock(MineralOccurrencesResponseHandler.class);
        this.httpServiceCaller = context.mock(HttpServiceCaller.class);
        this.mockGmlToKml = context.mock(GmlToKml.class);
        this.mockGmlToHtml = context.mock(GmlToHtml.class);
        this.mineralOccurrenceService = new MineralOccurrenceService(this.httpServiceCaller, this.mineralOccurrencesResponseHandler, this.methodMaker, this.mockGmlToKml, this.mockGmlToHtml);
        this.mockHttpClient = context.mock(HttpClient.class);
        //this.commodityService = context.mock(CommodityService.class);
    }

    /**
     * The service client ties various different classes together, so we are not testing its data integrity,
     * rather we are testing that it utilises the other classes properly and send us back the right return object.
     *
     * @throws Exception
     */
    @Test
    public void testGetAllMines() throws Exception {
        final String serviceURL = "http://localhost?";

        final GetMethod mockMethod = context.mock(GetMethod.class);
        final String mockMineResponse = new String();
        @SuppressWarnings("unchecked")
        final List<Mine> mockMines = context.mock(List.class);

        context.checking(new Expectations() {{
            oneOf(methodMaker).makeMethod(with(serviceURL), with("er:MiningFeatureOccurrence"),
                    with(any(String.class)), with(any(Integer.class)), with(BaseWFSService.DEFAULT_SRS), with(equal(ResultType.Results)));
            will(returnValue(mockMethod));

            oneOf(httpServiceCaller).getHttpClient();
            will(returnValue(mockHttpClient));

            oneOf(httpServiceCaller).getMethodResponseAsString(mockMethod, mockHttpClient);
            will(returnValue(mockMineResponse));

            oneOf(mineralOccurrencesResponseHandler).getMines(mockMineResponse);
            will(returnValue(mockMines));
        }});

        List<Mine> mines = this.mineralOccurrenceService.getMines(serviceURL, null, null, 0);
        Assert.assertEquals(mockMines, mines);
    }

    /**
     * The service client ties various different classes togather, so we are not testing its data integrity,
     * rather we are testing that it utilises the other classes properly and send us back the right return object
     *
     * @throws Exception
     */
    @Test
    public void getMineWithSpecifiedName() throws Exception {
        final String serviceURL = "http://localhost?";
        final String mineName = "SomeName";

        final MineFilter mineFilter = new MineFilter(mineName);
        final GetMethod mockMethod = context.mock(GetMethod.class);
        final String mockMineResponse = new String();
        @SuppressWarnings("unchecked")
        final List<Mine> mockMines = context.mock(List.class);

        context.checking(new Expectations() {{
            oneOf(methodMaker).makeMethod(serviceURL, "er:MiningFeatureOccurrence", mineFilter.getFilterStringAllRecords(), 0, BaseWFSService.DEFAULT_SRS, ResultType.Results); will(returnValue(mockMethod));
            oneOf(httpServiceCaller).getHttpClient();
            will(returnValue(mockHttpClient));

            oneOf(httpServiceCaller).getMethodResponseAsString(mockMethod, mockHttpClient);
            will(returnValue(mockMineResponse));

            oneOf(mineralOccurrencesResponseHandler).getMines(mockMineResponse);
            will(returnValue(mockMines));
        }});

        List<Mine> mines = this.mineralOccurrenceService.getMines(serviceURL, mineName, null, 0);
        Assert.assertEquals(mockMines, mines);
    }

    /**
     * Test the event that we dont provide a name or group
     * @throws Exception
     *
    @Test
    public void testGetCommodityNoNameOrGroup() throws Exception {
        final String serviceURL = "http://localhost?";
        final String commodityName = "";

        final GetMethod mockMethod = context.mock(GetMethod.class);
        final String mockCommodityResponse = new String();
        @SuppressWarnings("unchecked")
        final Collection<Mine> mockCommodities = (Collection<Mine>)context.mock(Collection.class);

        context.checking(new Expectations() {{
            oneOf(methodMaker).makeMethod(serviceURL, "er:Commodity", ""); will(returnValue(mockMethod));
            oneOf(httpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(httpServiceCaller).getMethodResponseAsString(mockMethod, mockHttpClient); will(returnValue(mockCommodityResponse));
            oneOf(mineralOccurrencesResponseHandler).getCommodities(mockCommodityResponse); will(returnValue(mockCommodities));
        }});

        Collection<Commodity> commodities = this.commodityService.get(serviceURL, commodityName);
        Assert.assertEquals(mockCommodities, commodities);
    }
    */

    /**
     * Test the event that we provide a name
     * @throws Exception
     *
    @Test
    public void testGetCommodity() throws Exception {
        final String serviceURL = "http://localhost?";
        final String commodityName = "someName";

        final CommodityFilter commodityFilter = new CommodityFilter(commodityName);
        final GetMethod mockMethod = context.mock(GetMethod.class);
        final String mockCommodityResponse = new String();
        @SuppressWarnings("unchecked")
        final Collection<Commodity> mockCommodities = (Collection<Commodity>)context.mock(Collection.class);

        context.checking(new Expectations() {{
            oneOf(methodMaker).makeMethod(serviceURL, "er:Commodity", commodityFilter.getFilterString()); will(returnValue(mockMethod));
            oneOf(httpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(httpServiceCaller).getMethodResponseAsString(mockMethod, mockHttpClient); will(returnValue(mockCommodityResponse));
            oneOf(mineralOccurrencesResponseHandler).getCommodities(mockCommodityResponse); will(returnValue(mockCommodities));
        }});

        Collection<Commodity> commodities = this.commodityService.get(serviceURL, commodityName);
        Assert.assertEquals(mockCommodities, commodities);
    }
    */

    /**
     * Test for a valid query.
     * @throws Exception
     */
    @Test
    public void testGetMineralOccurrenceGML() throws Exception {
        final String serviceURL = "http://localhost?";
        final String commodityName = "someName";
        final String measureType = "";
        final String minOreAmount = "";
        final String minOreAmountUOM = "";
        final String minCommodityAmount = "";
        final String minCommodityAmountUOM = "";
        final FilterBoundingBox bbox = null;
        final GetMethod mockMethod = context.mock(GetMethod.class);
        final String mockCommodityResponse = Util.loadXML("src/test/resources/commodityGetFeatureResponse.xml");

        final MineralOccurrenceFilter mineralOccurrenceFilter
            = new MineralOccurrenceFilter(commodityName,
                                           measureType,
                                           minOreAmount,
                                           minOreAmountUOM,
                                           minCommodityAmount,
                                           minCommodityAmountUOM);

        context.checking(new Expectations() {{
            //the mineral occurrence query part
            oneOf(methodMaker).makeMethod(serviceURL, "gsml:MappedFeature", mineralOccurrenceFilter.getFilterStringAllRecords(), 0, BaseWFSService.DEFAULT_SRS, ResultType.Results); will(returnValue(mockMethod));

            allowing(httpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(httpServiceCaller).getMethodResponseAsString(mockMethod, mockHttpClient);will(returnValue(mockCommodityResponse));

            oneOf(mockGmlToKml).convert(mockCommodityResponse, serviceURL);
        }});

        this.mineralOccurrenceService.getMineralOccurrenceGml(serviceURL,
                                                              commodityName,
                                                              measureType,
                                                              minOreAmount,
                                                              minCommodityAmountUOM,
                                                              minCommodityAmount,
                                                              minCommodityAmountUOM,
                                                              0,
                                                              bbox);
    }

    @Test
    public void testGetMiningActivity() throws Exception {
        final Mine mockMine = context.mock(Mine.class);
        final GetMethod mockMethod = context.mock(GetMethod.class);
        final FilterBoundingBox bbox = null;
        final String serviceUrl = "http://service/url";
        final String mockActivityResponse = Util.loadXML("src/test/resources/commodityGetFeatureResponse.xml"); //any wfs response is fine - we aren't testing contents of node

        context.checking(new Expectations() {{
            ignoring(mockMine);
            oneOf(methodMaker).makeMethod(with(serviceUrl), with("er:MiningFeatureOccurrence"),
                    with(any(String.class)), with(any(Integer.class)), with(BaseWFSService.DEFAULT_SRS), with(ResultType.Results));
            will(returnValue(mockMethod));
            oneOf(httpServiceCaller).getHttpClient();
            will(returnValue(mockHttpClient));
            oneOf(httpServiceCaller).getMethodResponseAsString(mockMethod, mockHttpClient);
            will(returnValue(mockActivityResponse));

            oneOf(mockGmlToKml).convert(mockActivityResponse, serviceUrl);
        }});

        this.mineralOccurrenceService.getMiningActivityGml(serviceUrl, "", "", "", "", "", "", "", 0, bbox);
    }

    /**
     * Tests getting the count of all mines
     * @throws Exception
     */
    @Test
    public void testGetMineCount() throws Exception {
        final String wfsUrl = "http://service/wfs";
        final String mineName = "mineName";
        final FilterBoundingBox bbox = new FilterBoundingBox("bboxSrs", new double[] {1.2}, new double[] {3.4});
        final GetMethod mockMethod = context.mock(GetMethod.class);
        final int maxFeatures = 1214;
        final InputStream getCountResponse = new FileInputStream("src/test/resources/GetWFSFeatureCount.xml");


        context.checking(new Expectations() {{
            oneOf(methodMaker).makeMethod(with(wfsUrl), with("er:MiningFeatureOccurrence"), with(any(String.class)), with(maxFeatures), with(BaseWFSService.DEFAULT_SRS), with(ResultType.Hits)); will(returnValue(mockMethod));

            allowing(httpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(httpServiceCaller).getMethodResponseAsStream(mockMethod, mockHttpClient);will(returnValue(getCountResponse));
        }});

        WFSCountResponse count = mineralOccurrenceService.getMinesCount(wfsUrl, mineName, bbox, maxFeatures);
        Assert.assertNotNull(count);
        Assert.assertEquals(161, count.getNumberOfFeatures());
    }

    /**
     * Tests getting the count of all mineral occurrences
     * @throws Exception
     */
    @Test
    public void testGetMineralOccurrenceCount() throws Exception {
        final String wfsUrl = "http://service/wfs";
        final FilterBoundingBox bbox = new FilterBoundingBox("bboxSrs", new double[] {1.2}, new double[] {3.4});
        final GetMethod mockMethod = context.mock(GetMethod.class);
        final String commodityName = "someName";
        final String measureType = "mt";
        final String minOreAmount = "1";
        final String minOreAmountUOM = "2";
        final String minCommodityAmount = "3";
        final String minCommodityAmountUOM = "4";
        final int maxFeatures = 1214;
        final InputStream getCountResponse = new FileInputStream("src/test/resources/GetWFSFeatureCount.xml");


        context.checking(new Expectations() {{
            oneOf(methodMaker).makeMethod(with(wfsUrl), with("gsml:MappedFeature"), with(any(String.class)), with(maxFeatures), with(BaseWFSService.DEFAULT_SRS), with(ResultType.Hits)); will(returnValue(mockMethod));

            allowing(httpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(httpServiceCaller).getMethodResponseAsStream(mockMethod, mockHttpClient);will(returnValue(getCountResponse));
        }});

        WFSCountResponse count = mineralOccurrenceService.getMineralOccurrenceCount(wfsUrl, commodityName, measureType, minOreAmount, minOreAmountUOM, minCommodityAmount, minCommodityAmountUOM, maxFeatures, bbox);
        Assert.assertNotNull(count);
        Assert.assertEquals(161, count.getNumberOfFeatures());
    }

    /**
     * Tests getting the count of all mining activities
     * @throws Exception
     */
    @Test
    public void testGetMiningActivityCount() throws Exception {
        final String wfsUrl = "http://service/wfs";
        final FilterBoundingBox bbox = new FilterBoundingBox("bboxSrs", new double[] {1.2}, new double[] {3.4});
        final GetMethod mockMethod = context.mock(GetMethod.class);
        final String mineName = "mineName";
        final String endDate = "1986-10-09";
        final String startDate = "1952-11-13";
        final String oreProcessed = "13";
        final String producedMaterial = "materialName";
        final String cutOffGrade = "12314";
        final String production = "production";
        final int maxFeatures = 1214;
        final InputStream getCountResponse = new FileInputStream("src/test/resources/GetWFSFeatureCount.xml");


        context.checking(new Expectations() {{
            oneOf(methodMaker).makeMethod(with(wfsUrl), with("er:MiningFeatureOccurrence"), with(any(String.class)), with(maxFeatures), with(BaseWFSService.DEFAULT_SRS), with(ResultType.Hits)); will(returnValue(mockMethod));

            allowing(httpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(httpServiceCaller).getMethodResponseAsStream(mockMethod, mockHttpClient);will(returnValue(getCountResponse));
        }});

        WFSCountResponse count = mineralOccurrenceService.getMiningActivityCount(wfsUrl, mineName, startDate, endDate, oreProcessed, producedMaterial, cutOffGrade, production, maxFeatures, bbox);
        Assert.assertNotNull(count);
        Assert.assertEquals(161, count.getNumberOfFeatures());
    }
}

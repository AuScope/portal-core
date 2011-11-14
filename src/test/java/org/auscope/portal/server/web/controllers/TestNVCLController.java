package org.auscope.portal.server.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.auscope.portal.csw.record.AbstractCSWOnlineResource;
import org.auscope.portal.csw.record.CSWOnlineResourceImpl;
import org.auscope.portal.csw.record.CSWRecord;
import org.auscope.portal.nvcl.NVCLNamespaceContext;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.domain.nvcldataservice.GetDatasetCollectionResponse;
import org.auscope.portal.server.domain.nvcldataservice.GetLogCollectionResponse;
import org.auscope.portal.server.domain.nvcldataservice.MosaicResponse;
import org.auscope.portal.server.domain.nvcldataservice.PlotScalarResponse;
import org.auscope.portal.server.util.ByteBufferedServletOutputStream;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.NVCLDataServiceMethodMaker.PlotScalarGraphType;
import org.auscope.portal.server.web.KnownLayerWFS;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker;
import org.auscope.portal.server.web.service.BoreholeService;
import org.auscope.portal.server.web.service.CSWCacheService;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.web.service.NVCLDataService;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * The Class TestNVCLController.
 * @version: $Id$
 */
public class TestNVCLController {

    /** The mock http request. */
    private HttpServletRequest mockHttpRequest;

    /** The mock http response. */
    private HttpServletResponse mockHttpResponse;

    /** The mock gml to kml. */
    private GmlToKml mockGmlToKml;

    /** The mock http session. */
    private HttpSession mockHttpSession;

    /** The mock servlet context. */
    private ServletContext mockServletContext;

    /** The mock http service caller. */
    private HttpServiceCaller mockHttpServiceCaller;

    /** The mock http client. */
    private HttpClient mockHttpClient;

    /** The mock csw service. */
    private CSWCacheService mockCSWService;

    /** The mock wfs method maker. */
    private WFSGetFeatureMethodMaker mockWfsMethodMaker;

    /** The mock borehole service. */
    private BoreholeService mockBoreholeService;

    /** The mock dataservice*/
    private NVCLDataService mockDataService;

    /** The nvcl controller. */
    private NVCLController nvclController;

    /** The known boreholes */
    private KnownLayerWFS mockBorehole;

    /** The known boreholes2*/
    private KnownLayerWFS mockBorehole2;

    /** The context. */
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    /**
     * Setup.
     */
    @Before
    public void setUp() {

        this.mockGmlToKml = context.mock(GmlToKml.class);
        this.mockHttpRequest = context.mock(HttpServletRequest.class);
        this.mockHttpResponse = context.mock(HttpServletResponse.class);
        this.mockBoreholeService = context.mock(BoreholeService.class);
        this.mockHttpSession = context.mock(HttpSession.class);
        this.mockServletContext = context.mock(ServletContext.class);
        this.mockHttpServiceCaller = context.mock(HttpServiceCaller.class);
        this.mockCSWService = context.mock(CSWCacheService.class);
        this.mockHttpClient = context.mock(HttpClient.class);
        this.mockDataService = context.mock(NVCLDataService.class);
        this.mockBorehole = context.mock(KnownLayerWFS.class,"first");
        this.mockBorehole2 = context.mock(KnownLayerWFS.class,"second");
        ArrayList<KnownLayerWFS> mockBoreholes=new ArrayList<KnownLayerWFS>();
        mockBoreholes.add(mockBorehole);
        mockBoreholes.add(mockBorehole2);
        this.nvclController = new NVCLController(this.mockGmlToKml, this.mockBoreholeService, this.mockHttpServiceCaller, this.mockCSWService, this.mockDataService,mockBoreholes);
    }

    /**
     * Tests to ensure that a non hylogger request calls the correct functions.
     *
     * @throws Exception the exception
     */
    @Test
    public void testNonHyloggerFilter() throws Exception {
        final String serviceUrl = "http://fake.com/wfs";
        final String nameFilter = "filterBob";
        final String custodianFilter = "filterCustodian";
        final String filterDate = "1986-10-09";
        final int maxFeatures = 10;
        final FilterBoundingBox bbox = new FilterBoundingBox("EPSG:4326", new double[] {1, 2}, new double[] {3,4});
        final String nvclWfsResponse = "wfsResponse";
        final String nvclKmlResponse = "kmlResponse";
        final boolean onlyHylogger = false;
        final HttpMethodBase mockHttpMethodBase = context.mock(HttpMethodBase.class);
        final URI httpMethodURI = new URI("http://example.com", true);

        context.checking(new Expectations() {{
            oneOf(mockBoreholeService).getAllBoreholes(serviceUrl, nameFilter, custodianFilter, filterDate, maxFeatures, bbox, null);
            will(returnValue(mockHttpMethodBase));

            oneOf(mockHttpResponse).setContentType(with(any(String.class)));
            oneOf(mockHttpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(mockHttpServiceCaller).getMethodResponseAsString(mockHttpMethodBase, mockHttpClient);
            will(returnValue(nvclWfsResponse));

            oneOf(mockGmlToKml).convert(with(any(String.class)), with(any(InputStream.class)),with(any(String.class)));
            will(returnValue(nvclKmlResponse));

            allowing(mockHttpMethodBase).getURI();
            will(returnValue(httpMethodURI));

            allowing(mockHttpRequest).getSession();
            will(returnValue(mockHttpSession));

            allowing(mockHttpSession).getServletContext();
            will(returnValue(mockServletContext));

            allowing(mockServletContext).getResourceAsStream(with(any(String.class)));
            will(returnValue(null));
        }});

        ModelAndView response = this.nvclController.doBoreholeFilter(serviceUrl, nameFilter, custodianFilter, filterDate, maxFeatures, bbox, onlyHylogger, mockHttpRequest);
        Assert.assertTrue((Boolean) response.getModel().get("success"));

        Map data = (Map) response.getModel().get("data");
        Assert.assertNotNull(data);
        Assert.assertEquals(nvclWfsResponse, data.get("gml"));
        Assert.assertEquals(nvclKmlResponse, data.get("kml"));
    }

    /**
     * Tests that hylogger filter uses the correct functions.
     *
     * @throws Exception the exception
     */
    @Test
    public void testHyloggerFilter() throws Exception {
        final String serviceUrl = "http://fake.com/wfs";
        final String nameFilter = "filterBob";
        final String custodianFilter = "filterCustodian";
        final String filterDate = "1986-10-09";
        final int maxFeatures = 10;
        final FilterBoundingBox bbox = new FilterBoundingBox("EPSG:4326", new double[] {1, 2}, new double[] {3, 4});
        final String nvclWfsResponse = "wfsResponse";
        final String nvclKmlResponse = "kmlResponse";
        final List<String> restrictedIds = Arrays.asList("ID1", "ID2");
        final boolean onlyHylogger = true;
        final HttpMethodBase mockHttpMethodBase = context.mock(HttpMethodBase.class);
        final URI httpMethodURI = new URI("http://example.com", true);
        final CSWRecord[] cswRecords = new CSWRecord[] {new CSWRecord("q", "w", "e", "r", new AbstractCSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "wfs", NVCLNamespaceContext.PUBLISHED_DATASETS_TYPENAME, "desc")}, null)};

        context.checking(new Expectations() {{
            oneOf(mockCSWService).getWFSRecords();
            will(returnValue(cswRecords));

            oneOf(mockBoreholeService).discoverHyloggerBoreholeIDs(mockCSWService);
            will(returnValue(restrictedIds));

            oneOf(mockBoreholeService).getAllBoreholes(serviceUrl, nameFilter, custodianFilter, filterDate, maxFeatures, bbox, restrictedIds);
            will(returnValue(mockHttpMethodBase));

            oneOf(mockHttpResponse).setContentType(with(any(String.class)));
            oneOf(mockHttpServiceCaller).getHttpClient();
            will(returnValue(mockHttpClient));

            oneOf(mockHttpServiceCaller).getMethodResponseAsString(mockHttpMethodBase, mockHttpClient);
            will(returnValue(nvclWfsResponse));

            oneOf(mockGmlToKml).convert(with(any(String.class)), with(any(InputStream.class)), with(any(String.class)));
            will(returnValue(nvclKmlResponse));

            allowing(mockHttpMethodBase).getURI();
            will(returnValue(httpMethodURI));

            allowing(mockHttpRequest).getSession();
            will(returnValue(mockHttpSession));

            allowing(mockHttpSession).getServletContext();
            will(returnValue(mockServletContext));

            allowing(mockServletContext).getResourceAsStream(with(any(String.class))); will(returnValue(null));
        }});

        ModelAndView response = this.nvclController.doBoreholeFilter(serviceUrl, nameFilter, custodianFilter, filterDate, maxFeatures, bbox, onlyHylogger, mockHttpRequest);
        Assert.assertTrue((Boolean) response.getModel().get("success"));

        Map data = (Map) response.getModel().get("data");
        Assert.assertNotNull(data);
        Assert.assertEquals(nvclWfsResponse, data.get("gml"));
        Assert.assertEquals(nvclKmlResponse, data.get("kml"));
    }

    /**
     * Tests that hylogger filter uses the correct functions when the underlying hylogger lookup fails.
     *
     * @throws Exception the exception
     */
    @Test
    public void testHyloggerFilterError() throws Exception {
        final String serviceUrl = "http://fake.com/wfs";
        final String nameFilter = "filterBob";
        final String custodianFilter = "filterCustodian";
        final String filterDate = "1986-10-09";
        final int maxFeatures = 10;
        final FilterBoundingBox bbox = new FilterBoundingBox("EPSG:4326", new double[] {1, 2}, new double[] {3, 4});
        final String nvclWfsResponse = "wfsResponse";
        final String nvclKmlResponse = "kmlResponse";
        final boolean onlyHylogger = true;
        final HttpMethodBase mockHttpMethodBase = context.mock(HttpMethodBase.class);
        final URI httpMethodURI = new URI("http://example.com", true);
        final CSWRecord[] cswRecords = new CSWRecord[] {new CSWRecord("a", "b", "c", "d", new AbstractCSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "wfs", NVCLNamespaceContext.PUBLISHED_DATASETS_TYPENAME, "desc")}, null)};

        context.checking(new Expectations() {{
            oneOf(mockCSWService).getWFSRecords();
            will(returnValue(cswRecords));

            oneOf(mockBoreholeService).discoverHyloggerBoreholeIDs(mockCSWService);
            will(throwException(new ConnectException()));

            oneOf(mockHttpResponse).setContentType(with(any(String.class)));
            oneOf(mockHttpServiceCaller).getHttpClient();
            will(returnValue(mockHttpClient));

            oneOf(mockHttpServiceCaller).getMethodResponseAsString(mockHttpMethodBase, mockHttpClient);
            will(returnValue(nvclWfsResponse));

            oneOf(mockGmlToKml).convert(with(any(String.class)), with(any(InputStream.class)), with(any(String.class)));
            will(returnValue(nvclKmlResponse));

            allowing(mockHttpMethodBase).getURI();
            will(returnValue(httpMethodURI));

            allowing(mockHttpRequest).getSession();
            will(returnValue(mockHttpSession));

            allowing(mockHttpSession).getServletContext();
            will(returnValue(mockServletContext));

            allowing(mockServletContext).getResourceAsStream(with(any(String.class)));
            will(returnValue(null));
        }});

        ModelAndView response = this.nvclController.doBoreholeFilter(serviceUrl, nameFilter, custodianFilter, filterDate, maxFeatures, bbox, onlyHylogger, mockHttpRequest);
        Assert.assertFalse((Boolean) response.getModel().get("success"));
    }

    /**
     * Tests that hylogger filter uses the correct functions when the underlying hylogger lookup returns no results.
     *
     * @throws Exception the exception
     */
    @Test
    public void testHyloggerFilterNoDatasets() throws Exception {
        final String serviceUrl = "http://fake.com/wfs";
        final String nameFilter = "filterBob";
        final String custodianFilter = "filterCustodian";
        final String filterDate = "1986-10-09";
        final int maxFeatures = 10;
        final FilterBoundingBox bbox = new FilterBoundingBox("EPSG:4326", new double[] {1., 2.}, new double[] {3., 4.});
        final String nvclWfsResponse = "wfsResponse";
        final String nvclKmlResponse = "kmlResponse";
        final boolean onlyHylogger = true;
        final HttpMethodBase mockHttpMethodBase = context.mock(HttpMethodBase.class);
        final URI httpMethodURI = new URI("http://example.com", true);
        final CSWRecord[] cswRecords = new CSWRecord[] {new CSWRecord("a", "b", "c", "d", new AbstractCSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "wfs", NVCLNamespaceContext.PUBLISHED_DATASETS_TYPENAME, "desc")}, null)};

        context.checking(new Expectations() {{
            oneOf(mockCSWService).getWFSRecords();
            will(returnValue(cswRecords));

            oneOf(mockBoreholeService).discoverHyloggerBoreholeIDs(mockCSWService);
            will(returnValue(new ArrayList<String>()));

            oneOf(mockHttpResponse).setContentType(with(any(String.class)));
            oneOf(mockHttpServiceCaller).getHttpClient();
            will(returnValue(mockHttpClient));

            oneOf(mockHttpServiceCaller).getMethodResponseAsString(mockHttpMethodBase, mockHttpClient);
            will(returnValue(nvclWfsResponse));

            oneOf(mockGmlToKml).convert(with(any(String.class)), with(any(InputStream.class)), with(any(String.class)));
            will(returnValue(nvclKmlResponse));

            allowing(mockHttpMethodBase).getURI();
            will(returnValue(httpMethodURI));

            allowing(mockHttpRequest).getSession();
            will(returnValue(mockHttpSession));

            allowing(mockHttpSession).getServletContext();
            will(returnValue(mockServletContext));

            allowing(mockServletContext).getResourceAsStream(with(any(String.class)));
            will(returnValue(null));
        }});

        ModelAndView response = this.nvclController.doBoreholeFilter(serviceUrl, nameFilter, custodianFilter, filterDate, maxFeatures, bbox, onlyHylogger, mockHttpRequest);
        Assert.assertFalse((Boolean) response.getModel().get("success"));
    }

    /**
     * Tests getting dataset collection succeeds if underlying service succeeds.
     * @throws Exception
     */
    @Test
    public void testGetDatasetCollection() throws Exception {
        final String serviceUrl = "http://example/url";
        final String holeIdentifier = "unique-id";
        final List<GetDatasetCollectionResponse> responseObjs = Arrays.asList(context.mock(GetDatasetCollectionResponse.class));

        context.checking(new Expectations() {{
            oneOf(mockDataService).getDatasetCollection(serviceUrl, holeIdentifier);will(returnValue(responseObjs));
        }});

        ModelAndView response = this.nvclController.getNVCLDatasets(serviceUrl, holeIdentifier);
        Assert.assertNotNull(response);
        Assert.assertTrue((Boolean)response.getModel().get("success"));
        Assert.assertSame(responseObjs, response.getModel().get("data"));
    }

    /**
     * Tests getting dataset collection fails if underlying service fails.
     * @throws Exception
     */
    @Test
    public void testGetDatasetCollectionError() throws Exception {
        final String serviceUrl = "http://example/url";
        final String holeIdentifier = "unique-id";

        context.checking(new Expectations() {{
            oneOf(mockDataService).getDatasetCollection(serviceUrl, holeIdentifier);will(throwException(new ConnectException()));
        }});

        ModelAndView response = this.nvclController.getNVCLDatasets(serviceUrl, holeIdentifier);
        Assert.assertNotNull(response);
        Assert.assertFalse((Boolean)response.getModel().get("success"));
    }

    /**
     * Tests getting dataset collection succeeds if underlying service succeeds.
     * @throws Exception
     */
    @Test
    public void testGetLogCollection() throws Exception {
        final String serviceUrl = "http://example/url";
        final String datasetId = "unique-id";
        final Boolean mosaicService = true;
        final List<GetLogCollectionResponse> responseObjs = Arrays.asList(context.mock(GetLogCollectionResponse.class));

        context.checking(new Expectations() {{
            oneOf(mockDataService).getLogCollection(serviceUrl, datasetId, mosaicService);will(returnValue(responseObjs));
        }});

        ModelAndView response = this.nvclController.getNVCLLogs(serviceUrl, datasetId, mosaicService);
        Assert.assertNotNull(response);
        Assert.assertTrue((Boolean)response.getModel().get("success"));
        Assert.assertSame(responseObjs, response.getModel().get("data"));
    }

    /**
     * Tests getting dataset collection fails if underlying service fails.
     * @throws Exception
     */
    @Test
    public void testGetLogCollectionError() throws Exception {
        final String serviceUrl = "http://example/url";
        final String datasetIdentifier = "unique-id";
        final Boolean mosaicService = false;

        context.checking(new Expectations() {{
            oneOf(mockDataService).getLogCollection(serviceUrl, datasetIdentifier, mosaicService);will(throwException(new ConnectException()));
        }});

        ModelAndView response = this.nvclController.getNVCLLogs(serviceUrl, datasetIdentifier, mosaicService);
        Assert.assertNotNull(response);
        Assert.assertFalse((Boolean)response.getModel().get("success"));
    }

    /**
     * Tests getting mosaic.
     * @throws Exception
     */
    @Test
    public void testGetMosaic() throws Exception {
        final String serviceUrl = "http://example/url";
        final String logId = "unique-id";
        final Integer width = 1;
        final Integer start = 2;
        final Integer end = 3;
        final byte[] data = new byte[] {0,1,2,3,4,5,6,7,8,9};
        final String contentType = "image/jpeg";
        final MosaicResponse mockMosaicResponse = context.mock(MosaicResponse.class);

        final ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        final ByteBufferedServletOutputStream outputStream = new ByteBufferedServletOutputStream(data.length);

        context.checking(new Expectations() {{
            oneOf(mockDataService).getMosaic(serviceUrl, logId, width, start, end);will(returnValue(mockMosaicResponse));

            oneOf(mockHttpResponse).setContentType(contentType);
            oneOf(mockHttpResponse).getOutputStream();will(returnValue(outputStream));

            allowing(mockMosaicResponse).getContentType();will(returnValue(contentType));
            allowing(mockMosaicResponse).getResponse();will(returnValue(inputStream));
        }});

        this.nvclController.getNVCLMosaic(serviceUrl, logId, width, start, end, mockHttpResponse);
        Assert.assertArrayEquals(data, outputStream.toByteArray());
    }

    /**
     * Tests getting mosaic fails gracefully when the service fails.
     * @throws Exception
     */
    @Test
    public void testGetMosaicConnectException() throws Exception {
        final String serviceUrl = "http://example/url";
        final String logId = "unique-id";
        final Integer width = 1;
        final Integer start = 2;
        final Integer end = 3;

        context.checking(new Expectations() {{
            oneOf(mockDataService).getMosaic(serviceUrl, logId, width, start, end);will(throwException(new ConnectException()));

            oneOf(mockHttpResponse).sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }});

        this.nvclController.getNVCLMosaic(serviceUrl, logId, width, start, end, mockHttpResponse);
    }

    /**
     * Tests getting PlotScalar.
     * @throws Exception
     */
    @Test
    public void testGetPlotScalar() throws Exception {
        final String serviceUrl = "http://example/url";
        final String logId = "unique-id";
        final Integer width = 1;
        final Integer height = 2;
        final Integer startDepth = 3;
        final Integer endDepth = 4;
        final Double samplingInterval = 1.5;
        final Integer graphTypeInt = 2;
        final PlotScalarGraphType graphType = PlotScalarGraphType.ScatteredChart;
        final byte[] data = new byte[] {0,1,2,3,4,5,6,7,8,9};
        final String contentType = "image/jpeg";
        final PlotScalarResponse mockPlotScalarResponse = context.mock(PlotScalarResponse.class);

        final ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        final ByteBufferedServletOutputStream outputStream = new ByteBufferedServletOutputStream(data.length);

        context.checking(new Expectations() {{
            oneOf(mockDataService).getPlotScalar(serviceUrl, logId, startDepth, endDepth, width, height, samplingInterval, graphType);will(returnValue(mockPlotScalarResponse));

            oneOf(mockHttpResponse).setContentType(contentType);
            oneOf(mockHttpResponse).getOutputStream();will(returnValue(outputStream));

            allowing(mockPlotScalarResponse).getContentType();will(returnValue(contentType));
            allowing(mockPlotScalarResponse).getResponse();will(returnValue(inputStream));
        }});

        this.nvclController.getNVCLPlotScalar(serviceUrl, logId, startDepth, endDepth, width, height, samplingInterval, graphTypeInt, mockHttpResponse);
        Assert.assertArrayEquals(data, outputStream.toByteArray());
    }

    /**
     * Tests getting PlotScalar fails correctly.
     * @throws Exception
     */
    @Test
    public void testGetPlotScalarError() throws Exception {
        final String serviceUrl = "http://example/url";
        final String logId = "unique-id";
        final Integer width = 1;
        final Integer height = 2;
        final Integer startDepth = 3;
        final Integer endDepth = 4;
        final Double samplingInterval = 1.5;
        final Integer graphTypeInt = 1;
        final PlotScalarGraphType graphType = PlotScalarGraphType.StackedBarChart;
        final byte[] data = new byte[] {0,1,2,3,4,5,6,7,8,9};

        context.checking(new Expectations() {{
            oneOf(mockDataService).getPlotScalar(serviceUrl, logId, startDepth, endDepth, width, height, samplingInterval, graphType);will(throwException(new ConnectException()));

            oneOf(mockHttpResponse).sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }});

        this.nvclController.getNVCLPlotScalar(serviceUrl, logId, startDepth, endDepth, width, height, samplingInterval, graphTypeInt, mockHttpResponse);
    }



    /**
     * Tests to ensure that a serviceFilter request returns correctly.
     *
     * @throws Exception the exception
     */
    @Test
    public void testServiceFilterReturns() throws Exception {
        final String serviceUrl = "http://fake.com/wfs";
        final String serviceFilter="fake.com";
        final String nameFilter = "filterBob";
        final String custodianFilter = "filterCustodian";
        final String filterDate = "1986-10-09";
        final int maxFeatures = 10;
        final String nvclWfsResponse = "wfsResponse";
        final String nvclKmlResponse = "kmlResponse";
        final String onlyHylogger = "off";
        final HttpMethodBase mockHttpMethodBase = context.mock(HttpMethodBase.class);
        final URI httpMethodURI = new URI("http://example.com", true);

        context.checking(new Expectations() {{
            oneOf(mockBoreholeService).getAllBoreholes(serviceUrl, nameFilter, custodianFilter, filterDate, maxFeatures, null, null);
            will(returnValue(mockHttpMethodBase));

            oneOf(mockHttpResponse).setContentType(with(any(String.class)));
            oneOf(mockHttpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(mockHttpServiceCaller).getMethodResponseAsString(mockHttpMethodBase, mockHttpClient);
            will(returnValue(nvclWfsResponse));

            oneOf(mockGmlToKml).convert(with(any(String.class)), with(any(InputStream.class)),with(any(String.class)));
            will(returnValue(nvclKmlResponse));

            allowing(mockHttpMethodBase).getURI();
            will(returnValue(httpMethodURI));

            allowing(mockHttpRequest).getSession();
            will(returnValue(mockHttpSession));

            allowing(mockHttpSession).getServletContext();
            will(returnValue(mockServletContext));

            allowing(mockServletContext).getResourceAsStream(with(any(String.class)));
            will(returnValue(null));
        }});

        ModelAndView response = this.nvclController.doBoreholeFilter(serviceUrl, nameFilter, custodianFilter, filterDate, maxFeatures,"", onlyHylogger,serviceFilter, mockHttpRequest);
        Assert.assertTrue((Boolean) response.getModel().get("success"));

        Map data = (Map) response.getModel().get("data");
        Assert.assertNotNull(data);
        Assert.assertEquals(nvclWfsResponse, data.get("gml"));
        Assert.assertEquals(nvclKmlResponse, data.get("kml"));
    }

    @Test
    public void testServiceFilterReturnsEmptyMAV() throws Exception {
        final String serviceUrl = "http://fake.com/wfs";
        final String serviceFilter="fakeNOT.com";
        final String nameFilter = "filterBob";
        final String custodianFilter = "filterCustodian";
        final String filterDate = "1986-10-09";
        final int maxFeatures = 10;
        final String onlyHylogger = "off";

        ModelAndView response = this.nvclController.doBoreholeFilter(serviceUrl, nameFilter, custodianFilter, filterDate, maxFeatures,"", onlyHylogger,serviceFilter, mockHttpRequest);
        Map data = (Map) response.getModel().get("data");
        Assert.assertNull(data);
    }

    @Test
    public void testGetBoreholeServices() throws MalformedURLException{
        final String [] urls={"http://url1.com.au/test","http://url2.com.au/test","http://url3.com.au/test"};
        final String [] wrongUrls={"http://wrong1.com/no/test","http://wrong2.com/no/test"};
        final String title="testTitle";
        context.checking(new Expectations() {{
         oneOf(mockBorehole).getTitle();
         will(returnValue(title));
         oneOf(mockBorehole2).getTitle();
         will(returnValue("Wrong Title"));
         oneOf(mockBorehole).getServiceEndpoints();
         will(returnValue(urls));
         oneOf(mockBorehole2).getServiceEndpoints();
         will(returnValue(wrongUrls));
        }});

        ModelAndView response = this.nvclController.getBoreholeServices(title);
        ArrayList data = (ArrayList) response.getModel().get("data");
        Assert.assertEquals(urls.length,data.size());

    }
}

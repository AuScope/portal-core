package org.auscope.portal.server.web.controllers;

import java.io.InputStream;
import java.net.ConnectException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.URI;
import org.auscope.portal.csw.CSWOnlineResource;
import org.auscope.portal.csw.CSWOnlineResourceImpl;
import org.auscope.portal.csw.CSWRecord;
import org.auscope.portal.nvcl.NVCLNamespaceContext;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.WFSGetFeatureMethodMakerPOST;
import org.auscope.portal.server.web.service.BoreholeService;
import org.auscope.portal.server.web.service.CSWService;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

public class TestNVCLController {
    private HttpServletRequest mockHttpRequest;
    private HttpServletResponse mockHttpResponse;
    private GmlToKml mockGmlToKml;
    private HttpSession mockHttpSession;
    private ServletContext mockServletContext;
    private HttpServiceCaller mockHttpServiceCaller;
    private HttpClient mockHttpClient;
    private CSWService mockCSWService;
    private WFSGetFeatureMethodMakerPOST mockWfsMethodMaker;
    private BoreholeService mockBoreholeService;
    private NVCLController nvclController;

    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    @Before
    public void setup() {
        
        this.mockGmlToKml = context.mock(GmlToKml.class);
        this.mockHttpRequest = context.mock(HttpServletRequest.class);
        this.mockHttpResponse = context.mock(HttpServletResponse.class);
        this.mockBoreholeService = context.mock(BoreholeService.class);
        this.mockHttpSession = context.mock(HttpSession.class);
        this.mockServletContext = context.mock(ServletContext.class);
        this.mockHttpServiceCaller = context.mock(HttpServiceCaller.class);
        this.mockCSWService = context.mock(CSWService.class);
        this.mockHttpClient = context.mock(HttpClient.class);
        
        this.nvclController = new NVCLController(this.mockGmlToKml, this.mockBoreholeService, this.mockHttpServiceCaller, this.mockCSWService);
    }
    
    /**
     * Tests to ensure that a non hylogger request calls the correct functions
     * @throws Exception
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
        final URI httpMethodURI = new URI( "http://example.com");
        
        context.checking(new Expectations() {{
            oneOf(mockBoreholeService).getAllBoreholes(serviceUrl, nameFilter, custodianFilter, filterDate, maxFeatures, bbox, null);will(returnValue(mockHttpMethodBase));
            
            oneOf(mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf (mockHttpServiceCaller).getMethodResponseAsString(mockHttpMethodBase, mockHttpClient); will(returnValue(nvclWfsResponse));
            oneOf (mockGmlToKml).convert(with(any(String.class)), with(any(InputStream.class)),with(any(String.class))); will(returnValue(nvclKmlResponse));
            
            allowing(mockHttpMethodBase).getURI();will(returnValue(httpMethodURI));
            
            allowing(mockHttpRequest).getSession();will(returnValue(mockHttpSession));
            allowing(mockHttpSession).getServletContext();will(returnValue(mockServletContext));
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
     * Tests that hylogger filter uses the correct functions
     */
    @Test
    public void testHyloggerFilter() throws Exception {
        final String serviceUrl = "http://fake.com/wfs";
        final String nameFilter = "filterBob";
        final String custodianFilter = "filterCustodian";
        final String filterDate = "1986-10-09";
        final int maxFeatures = 10;
        final FilterBoundingBox bbox = new FilterBoundingBox("EPSG:4326", new double[] {1, 2}, new double[] {3,4});
        final String nvclWfsResponse = "wfsResponse";
        final String nvclKmlResponse = "kmlResponse";
        final List<String> restrictedIds = Arrays.asList("ID1", "ID2");
        final boolean onlyHylogger = true;
        final HttpMethodBase mockHttpMethodBase = context.mock(HttpMethodBase.class);
        final URI httpMethodURI = new URI( "http://example.com");
        final CSWRecord[] cswRecords = new CSWRecord[] {new CSWRecord("a","b","c","d","e",new CSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "wfs", NVCLNamespaceContext.PUBLISHED_DATASETS_TYPENAME, "desc")}, null)};
        
        context.checking(new Expectations() {{
            oneOf(mockCSWService).getWFSRecords();will(returnValue(cswRecords));
            
            oneOf(mockBoreholeService).discoverHyloggerBoreholeIDs(mockCSWService);will(returnValue(restrictedIds));
            oneOf(mockBoreholeService).getAllBoreholes(serviceUrl, nameFilter, custodianFilter, filterDate, maxFeatures, bbox, restrictedIds);will(returnValue(mockHttpMethodBase));
            
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf (mockHttpServiceCaller).getMethodResponseAsString(mockHttpMethodBase, mockHttpClient); will(returnValue(nvclWfsResponse));
            oneOf (mockGmlToKml).convert(with(any(String.class)), with(any(InputStream.class)),with(any(String.class))); will(returnValue(nvclKmlResponse));
            
            allowing(mockHttpMethodBase).getURI();will(returnValue(httpMethodURI));
            
            allowing(mockHttpRequest).getSession();will(returnValue(mockHttpSession));
            allowing(mockHttpSession).getServletContext();will(returnValue(mockServletContext));
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
     * Tests that hylogger filter uses the correct functions when the underlying hylogger lookup fails
     */
    @Test
    public void testHyloggerFilterError() throws Exception {
        final String serviceUrl = "http://fake.com/wfs";
        final String nameFilter = "filterBob";
        final String custodianFilter = "filterCustodian";
        final String filterDate = "1986-10-09";
        final int maxFeatures = 10;
        final FilterBoundingBox bbox = new FilterBoundingBox("EPSG:4326", new double[] {1, 2}, new double[] {3,4});
        final String nvclWfsResponse = "wfsResponse";
        final String nvclKmlResponse = "kmlResponse";
        final List<String> restrictedIds = Arrays.asList("ID1", "ID2");
        final boolean onlyHylogger = true;
        final HttpMethodBase mockHttpMethodBase = context.mock(HttpMethodBase.class);
        final URI httpMethodURI = new URI( "http://example.com");
        final CSWRecord[] cswRecords = new CSWRecord[] {new CSWRecord("a","b","c","d","e",new CSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "wfs", NVCLNamespaceContext.PUBLISHED_DATASETS_TYPENAME, "desc")}, null)};
        
        context.checking(new Expectations() {{
            oneOf(mockCSWService).getWFSRecords();will(returnValue(cswRecords));
            
            oneOf(mockBoreholeService).discoverHyloggerBoreholeIDs(mockCSWService);will(throwException(new ConnectException()));
            
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf (mockHttpServiceCaller).getMethodResponseAsString(mockHttpMethodBase, mockHttpClient); will(returnValue(nvclWfsResponse));
            oneOf (mockGmlToKml).convert(with(any(String.class)), with(any(InputStream.class)),with(any(String.class))); will(returnValue(nvclKmlResponse));
            
            allowing(mockHttpMethodBase).getURI();will(returnValue(httpMethodURI));
            
            allowing(mockHttpRequest).getSession();will(returnValue(mockHttpSession));
            allowing(mockHttpSession).getServletContext();will(returnValue(mockServletContext));
            allowing(mockServletContext).getResourceAsStream(with(any(String.class))); will(returnValue(null));
        }});
        
        ModelAndView response = this.nvclController.doBoreholeFilter(serviceUrl, nameFilter, custodianFilter, filterDate, maxFeatures, bbox, onlyHylogger, mockHttpRequest);
        Assert.assertFalse((Boolean) response.getModel().get("success"));
    }
    
    /**
     * Tests that hylogger filter uses the correct functions when the underlying hylogger lookup returns no results
     */
    @Test
    public void testHyloggerFilterNoDatasets() throws Exception {
        final String serviceUrl = "http://fake.com/wfs";
        final String nameFilter = "filterBob";
        final String custodianFilter = "filterCustodian";
        final String filterDate = "1986-10-09";
        final int maxFeatures = 10;
        final FilterBoundingBox bbox = new FilterBoundingBox("EPSG:4326", new double[] {1, 2}, new double[] {3,4});
        final String nvclWfsResponse = "wfsResponse";
        final String nvclKmlResponse = "kmlResponse";
        final List<String> restrictedIds = Arrays.asList("ID1", "ID2");
        final boolean onlyHylogger = true;
        final HttpMethodBase mockHttpMethodBase = context.mock(HttpMethodBase.class);
        final URI httpMethodURI = new URI( "http://example.com");
        final CSWRecord[] cswRecords = new CSWRecord[] {new CSWRecord("a","b","c","d","e",new CSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "wfs", NVCLNamespaceContext.PUBLISHED_DATASETS_TYPENAME, "desc")}, null)};
        
        context.checking(new Expectations() {{
            oneOf(mockCSWService).getWFSRecords();will(returnValue(cswRecords));
            
            oneOf(mockBoreholeService).discoverHyloggerBoreholeIDs(mockCSWService);will(returnValue(new ArrayList<String>()));
            
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf (mockHttpServiceCaller).getMethodResponseAsString(mockHttpMethodBase, mockHttpClient); will(returnValue(nvclWfsResponse));
            oneOf (mockGmlToKml).convert(with(any(String.class)), with(any(InputStream.class)),with(any(String.class))); will(returnValue(nvclKmlResponse));
            
            allowing(mockHttpMethodBase).getURI();will(returnValue(httpMethodURI));
            
            allowing(mockHttpRequest).getSession();will(returnValue(mockHttpSession));
            allowing(mockHttpSession).getServletContext();will(returnValue(mockServletContext));
            allowing(mockServletContext).getResourceAsStream(with(any(String.class))); will(returnValue(null));
        }});
        
        ModelAndView response = this.nvclController.doBoreholeFilter(serviceUrl, nameFilter, custodianFilter, filterDate, maxFeatures, bbox, onlyHylogger, mockHttpRequest);
        Assert.assertFalse((Boolean) response.getModel().get("success"));
    }
}

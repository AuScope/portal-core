package org.auscope.portal.server.web.service;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.domain.filter.IFilter;
import org.auscope.portal.server.web.IWFSGetFeatureMethodMaker;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestNvclService {
    
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private NvclService service;
    private IFilter mockFilter = context.mock(IFilter.class);
    private HttpServiceCaller mockHttpServiceCaller = context.mock(HttpServiceCaller.class);
    private IWFSGetFeatureMethodMaker mockMethodMaker = context.mock(IWFSGetFeatureMethodMaker.class);
    
    @Before
    public void setup() throws Exception {
        service = new NvclService();
        service.setFilter(mockFilter);
        service.setHttpServiceCaller(mockHttpServiceCaller);
        service.setWFSGetFeatureMethodMakerPOST(mockMethodMaker);
    }
    
    @Test
    public void testGetAllBoreholesNoBbox() throws Exception {
        final FilterBoundingBox bbox = new FilterBoundingBox("mySrs", new double[] {0, 1}, new double[] {2,3});
        final String serviceURL = "http://foo.bar";
        final String filterString = "myFilter";
        final int maxFeatures = 45;
        final String responseString = "xmlString";
        
        context.checking(new Expectations() {{
            allowing(mockFilter).getFilterStringBoundingBox(bbox);will(returnValue(filterString));
            
            oneOf(mockMethodMaker).makeMethod(serviceURL, "gsml:Borehole", filterString, maxFeatures);
            oneOf(mockHttpServiceCaller).getHttpClient();
            oneOf(mockHttpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));will(returnValue(responseString));
        }});
        
        HttpMethodBase method = service.getAllBoreholes(serviceURL, maxFeatures, bbox);
        String result = mockHttpServiceCaller.getMethodResponseAsString(method, mockHttpServiceCaller.getHttpClient());
        Assert.assertNotNull(result);
        Assert.assertEquals(responseString, result);
    }
    
    @Test
    public void testGetAllBoreholesBbox() throws Exception {
        final String serviceURL = "http://foo.bar";
        final String filterString = "";
        final int maxFeatures = 45;
        final String responseString = "xmlString";
        
        context.checking(new Expectations() {{
            allowing(mockFilter).getFilterStringAllRecords();will(returnValue(filterString));
            
            oneOf(mockMethodMaker).makeMethod(serviceURL, "gsml:Borehole", filterString, maxFeatures);
            oneOf(mockHttpServiceCaller).getHttpClient();
            oneOf(mockHttpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));will(returnValue(responseString));
        }});
        
        HttpMethodBase method = service.getAllBoreholes(serviceURL, maxFeatures, null);
        String result = mockHttpServiceCaller.getMethodResponseAsString(method, mockHttpServiceCaller.getHttpClient());
        Assert.assertNotNull(result);
        Assert.assertEquals(responseString, result);
    }
}

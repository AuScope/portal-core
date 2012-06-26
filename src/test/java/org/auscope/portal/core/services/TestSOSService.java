package org.auscope.portal.core.services;

import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.SOSMethodMaker;
import org.auscope.portal.core.test.PortalTestClass;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestSOSService extends PortalTestClass {
    HttpMethodBase mockHttpMethodBase = context.mock(HttpMethodBase.class);
	HttpServiceCaller mockHttpServiceCaller = context.mock(HttpServiceCaller.class);
	SOSMethodMaker mockSosMethodMaker = context.mock(SOSMethodMaker.class);
	SOSService sosService = null;
	
	@Before
    public void init() {
       sosService = new SOSService(mockHttpServiceCaller, mockSosMethodMaker);
    }

    @Test
    public void testGetObserFOIRequest() throws Exception {
    	final String sosUrl = "http://example.org/sos";
        final String request ="GetObservation";
        final String featureID = "testID";
        
        context.checking(new Expectations() {{
        	oneOf(mockSosMethodMaker).makePostMethod(sosUrl, request, featureID, null,null);will(returnValue(mockHttpMethodBase));
        }});
        
        Assert.assertSame(mockHttpMethodBase, sosService.generateSOSRequest(sosUrl, request, featureID, null, null));
    }
    
    @Test
    public void testGetObsTemporalFilterRequest() throws Exception {
    	final String sosUrl = "http://example.org/sos";
        final String request ="GetObservation";
        final String temporalFilter = "1989-12-31T00:00:00+08/2010-02-21T00:00:00+08";
        
        context.checking(new Expectations() {{
        	oneOf(mockSosMethodMaker).makePostMethod(sosUrl, request, null, temporalFilter,null);will(returnValue(mockHttpMethodBase));
        }});
        
        Assert.assertSame(mockHttpMethodBase, sosService.generateSOSRequest(sosUrl, request, null, temporalFilter, null));
    }
    
    @Test
    public void testGetObsBBOXFilterRequest() throws Exception {
    	final String sosUrl = "http://example.org/sos";
        final String request ="GetObservation";
        final String bboxFilter = "-8.9,-44.0,112.8,154.1,http://www.opengis.net/def/crs/EPSG/0/4283";
        
        context.checking(new Expectations() {{
        	oneOf(mockSosMethodMaker).makePostMethod(sosUrl, request, null, null, bboxFilter);will(returnValue(mockHttpMethodBase));
        }});
        
        Assert.assertSame(mockHttpMethodBase, sosService.generateSOSRequest(sosUrl, request, null, null, bboxFilter));
    }
    
    


}

package org.auscope.portal.server.web.controllers;

import java.util.Map;

import junit.framework.Assert;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * Unit tests for GeotransectsController.
 * 
 * @author jac24m
 */
public class TestGeotransectsController {
	
    /**
     * JMock context
     */
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class);
    
	@Test
	public void testRequestGeotransectsDataSuccess() throws Exception {
        final String serviceUrl = "http://fake.com/bob";
        final String jsonResponse = "{\"items\":[{\"travelTime\":6,\"type\":\"migrated\",\"lineID\":\"07GA_A1\"," +
        		"\"url\":\"http://files.ivec.org/grid-auscope/geotransects/07GA-A1/mig_07gaa1_6s_unbiasedxys.sgy\"}]," +
        		"\"result\":{\"code\":0,\"msg\":\"No error detected\",\"success\":true}}";
        
        context.checking(new Expectations() {{
            oneOf(mockServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), 
            		with(any(HttpClient.class)));will(returnValue(jsonResponse));
            oneOf(mockServiceCaller).getHttpClient();
        }});
        
        GeotransectsController controller = new GeotransectsController(mockServiceCaller);
        ModelAndView mv = controller.requestGeotransectsData(serviceUrl);
        
        Assert.assertNotNull(mv);
        
        Map<String, Object> model = mv.getModel();
        Assert.assertEquals(true, model.get("success"));
        
        Assert.assertNotNull(model.get("json"));
        
        JSONArray dataItems = JSONObject.fromObject(model.get("json")).getJSONArray("items");
        Assert.assertNotNull(dataItems);
        Assert.assertTrue(dataItems.size() == 1);
        Assert.assertNotNull(dataItems.get(0));
	}
}

package org.auscope.portal.server.web.controllers;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.auscope.portal.csw.CSWRecord;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.web.service.CSWService;
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
 * User: Mathew Wyatt
 * Date: 27/08/2009
 * @version $Id$
 */
public class TestCSWController {

    /**
     * JMock context
     */
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    private CSWService mockCSWService = context.mock(CSWService.class);
    private PortalPropertyPlaceholderConfigurer mockPropertyConfigurer = context.mock(PortalPropertyPlaceholderConfigurer.class);

    private HttpServletRequest mockHttpRequest = context.mock(HttpServletRequest.class);
    private HttpServletResponse mockHttpResponse = context.mock(HttpServletResponse.class);
    private ViewCSWRecordFactory mockViewCSWRecordFactory = context.mock(ViewCSWRecordFactory.class);
    private CSWRecord mockCSWRecord1 = context.mock(CSWRecord.class, "mockCSWRecord1");
    private CSWRecord mockCSWRecord2 = context.mock(CSWRecord.class, "mockCSWRecord2");

    private CSWController cswController;

    @Before
    public void setup() throws Exception {
        final String serviceUrl = "somejunk";

        context.checking(new Expectations() {{
            oneOf(mockPropertyConfigurer).resolvePlaceholder(with(any(String.class)));will(returnValue(serviceUrl));
            oneOf(mockCSWService).updateRecordsInBackground();
        }});

        cswController = new CSWController(mockCSWService, mockViewCSWRecordFactory, mockPropertyConfigurer);
    }

    @Test
    public void testGetRecordResponse_Success() throws Exception {
    	final StringWriter actualJSONResponse = new StringWriter();
    	final ModelMap viewCSWRecord1 = new ModelMap();
    	final ModelMap viewCSWRecord2 = new ModelMap();

    	viewCSWRecord1.put("rec1", "val1");
    	viewCSWRecord2.put("rec2", "val2");

    	context.checking(new Expectations() {{
    		oneOf(mockCSWService).updateRecordsInBackground();
    		oneOf(mockCSWService).getAllRecords();will(returnValue(new CSWRecord[] {mockCSWRecord1, mockCSWRecord2}));

    		oneOf(mockViewCSWRecordFactory).toView(mockCSWRecord1);will(returnValue(viewCSWRecord1));
    		oneOf(mockViewCSWRecordFactory).toView(mockCSWRecord2);will(returnValue(viewCSWRecord2));

    		//check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});

    	//Run the method, get our response rendered as a JSONObject
    	ModelAndView mav = cswController.getCSWRecords();
    	mav.getView().render(mav.getModel(), mockHttpRequest, mockHttpResponse);
    	JSONObject jsonObj = JSONObject.fromObject(actualJSONResponse.toString());

    	//Check our response contains useful info...
    	Assert.assertEquals(true, jsonObj.getBoolean("success"));
    	JSONArray records = jsonObj.getJSONArray("records");
    	Assert.assertNotNull(records);
    	Assert.assertEquals(2, records.size());

    	JSONObject jsonRec1 = records.getJSONObject(0);
    	JSONObject jsonRec2 = records.getJSONObject(1);

    	Assert.assertEquals("val1", jsonRec1.get("rec1"));
    	Assert.assertEquals("val2", jsonRec2.get("rec2"));
    }

    @Test
    public void testGetRecordResponse_UpdateError() throws Exception {
    	final StringWriter actualJSONResponse = new StringWriter();

    	context.checking(new Expectations() {{
    		oneOf(mockCSWService).updateRecordsInBackground();will(throwException(new Exception()));

    		//check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});

    	//Run the method, get our response rendered as a JSONObject
    	ModelAndView mav = cswController.getCSWRecords();
    	mav.getView().render(mav.getModel(), mockHttpRequest, mockHttpResponse);
    	JSONObject jsonObj = JSONObject.fromObject(actualJSONResponse.toString());

    	//Check our response contains useful info...
    	Assert.assertEquals(false, jsonObj.getBoolean("success"));
    	JSONArray records = (JSONArray)jsonObj.get("records");
    	Assert.assertNotNull(records);
    	Assert.assertEquals(0, records.size());
    }


    @Test
    public void testGetRecordResponse_TransformError() throws Exception {
    	final StringWriter actualJSONResponse = new StringWriter();
    	final ModelMap viewCSWRecord1 = new ModelMap();
    	final ModelMap viewCSWRecord2 = new ModelMap();

    	viewCSWRecord1.put("rec1", "val1");
    	viewCSWRecord2.put("rec2", "val2");

    	context.checking(new Expectations() {{
    		oneOf(mockCSWService).updateRecordsInBackground();
    		oneOf(mockCSWService).getAllRecords();will(returnValue(new CSWRecord[] {mockCSWRecord1, mockCSWRecord2}));

    		oneOf(mockViewCSWRecordFactory).toView(mockCSWRecord1);will(returnValue(viewCSWRecord1));
    		oneOf(mockViewCSWRecordFactory).toView(mockCSWRecord2);will(throwException(new Exception()));

    		//check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});

    	//Run the method, get our response rendered as a JSONObject
    	ModelAndView mav = cswController.getCSWRecords();
    	mav.getView().render(mav.getModel(), mockHttpRequest, mockHttpResponse);
    	JSONObject jsonObj = JSONObject.fromObject(actualJSONResponse.toString());

    	//Check our response contains useful info...
    	Assert.assertEquals(false, jsonObj.getBoolean("success"));
    	JSONArray records = (JSONArray)jsonObj.get("records");
        Assert.assertNotNull(records);
        Assert.assertEquals(0, records.size());
    }


}

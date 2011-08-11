package org.auscope.portal.server.web.controllers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.auscope.portal.csw.record.CSWRecord;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.web.service.CSWCacheService;
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
public class TestCSWCacheController {

    /**
     * JMock context
     */
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    private CSWCacheService mockCSWService = context.mock(CSWCacheService.class);
    private PortalPropertyPlaceholderConfigurer mockPropertyConfigurer = context.mock(PortalPropertyPlaceholderConfigurer.class);

    private HttpServletRequest mockHttpRequest = context.mock(HttpServletRequest.class);
    private HttpServletResponse mockHttpResponse = context.mock(HttpServletResponse.class);
    private ViewCSWRecordFactory mockViewCSWRecordFactory = context.mock(ViewCSWRecordFactory.class);
    private CSWRecord mockCSWRecord1 = context.mock(CSWRecord.class, "mockCSWRecord1");
    private CSWRecord mockCSWRecord2 = context.mock(CSWRecord.class, "mockCSWRecord2");

    private CSWCacheController cswController;

    @Before
    public void setup() throws Exception {
        final String serviceUrl = "somejunk";

        context.checking(new Expectations() {{
            oneOf(mockPropertyConfigurer).resolvePlaceholder(with(any(String.class)));will(returnValue(serviceUrl));
            oneOf(mockCSWService).updateCache();
        }});

        cswController = new CSWCacheController(mockCSWService, mockViewCSWRecordFactory, mockPropertyConfigurer);
    }

    @Test
    public void testGetRecordResponse_Success() throws Exception {
        final StringWriter actualJSONResponse = new StringWriter();
        final ModelMap viewCSWRecord1 = new ModelMap();
        final ModelMap viewCSWRecord2 = new ModelMap();

        viewCSWRecord1.put("rec1", "val1");
        viewCSWRecord2.put("rec2", "val2");

        context.checking(new Expectations() {{
            oneOf(mockCSWService).getRecordCache();will(returnValue(Arrays.asList(mockCSWRecord1, mockCSWRecord2)));

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
        JSONArray records = jsonObj.getJSONArray("data");
        Assert.assertNotNull(records);
        Assert.assertEquals(2, records.size());

        JSONObject jsonRec1 = records.getJSONObject(0);
        JSONObject jsonRec2 = records.getJSONObject(1);

        Assert.assertEquals("val1", jsonRec1.get("rec1"));
        Assert.assertEquals("val2", jsonRec2.get("rec2"));
    }


    @Test
    public void testGetRecordResponse_TransformError() throws Exception {
        final StringWriter actualJSONResponse = new StringWriter();
        final ModelMap viewCSWRecord1 = new ModelMap();
        final ModelMap viewCSWRecord2 = new ModelMap();

        viewCSWRecord1.put("rec1", "val1");
        viewCSWRecord2.put("rec2", "val2");

        context.checking(new Expectations() {{
            oneOf(mockCSWService).getRecordCache();will(returnValue(Arrays.asList(mockCSWRecord1, mockCSWRecord2)));

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
        JSONArray records = (JSONArray)jsonObj.get("data");
        Assert.assertNotNull(records);
        Assert.assertEquals(0, records.size());
    }

    /**
     * Tests that the underlying services are called correctly and the response
     * is transformed into an appropriate format
     */
    @Test
    public void testGetKeywords() {
        final Map<String, Integer> expectedKeywords = new HashMap<String, Integer>();
        expectedKeywords.put("keyword1", 5);
        expectedKeywords.put("keyword2", 17);

        ModelMap kw1 = new ModelMap();
        kw1.put("keyword", "keyword1");
        kw1.put("count", 5);
        ModelMap kw2 = new ModelMap();
        kw2.put("keyword", "keyword2");
        kw2.put("count", 17);
        final List<ModelMap> expectedDataObj = Arrays.asList(kw1, kw2);

        context.checking(new Expectations() {{
            oneOf(mockCSWService).getKeywordCache();will(returnValue(expectedKeywords));
        }});

        ModelAndView mav = cswController.getCSWKeywords();
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
        Assert.assertEquals(expectedDataObj, mav.getModel().get("data"));
    }

}

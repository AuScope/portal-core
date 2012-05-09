package org.auscope.portal.server.web.controllers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.auscope.portal.PortalTestClass;
import org.auscope.portal.csw.record.CSWRecord;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.web.service.CSWCacheService;
import org.auscope.portal.server.web.view.ViewCSWRecordFactory;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

/**
 * User: Mathew Wyatt
 * Date: 27/08/2009.
 *
 * @version $Id$
 */
public class TestCSWCacheController extends PortalTestClass {

    /** The Constant SUCCESSJSON. */
    private static final String SUCCESSJSON = "success";

    /** The mock csw service. */
    private CSWCacheService mockCSWService = context.mock(CSWCacheService.class);

    /** The mock property configurer. */
    private PortalPropertyPlaceholderConfigurer mockPropertyConfigurer = context.mock(PortalPropertyPlaceholderConfigurer.class);

    /** The mock http request. */
    private HttpServletRequest mockHttpRequest = context.mock(HttpServletRequest.class);

    /** The mock http response. */
    private HttpServletResponse mockHttpResponse = context.mock(HttpServletResponse.class);

    /** The mock view csw record factory. */
    private ViewCSWRecordFactory mockViewCSWRecordFactory = context.mock(ViewCSWRecordFactory.class);

    /** The mock csw record1. */
    private CSWRecord mockCSWRecord1 = context.mock(CSWRecord.class, "mockCSWRecord1");

    /** The mock csw record2. */
    private CSWRecord mockCSWRecord2 = context.mock(CSWRecord.class, "mockCSWRecord2");

    /** The csw controller. */
    private CSWCacheController cswController;

    /**
     * Setup.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        final String serviceUrl = "somejunk";

        context.checking(new Expectations() {{
            oneOf(mockCSWService).updateCache();
        }});

        cswController = new CSWCacheController(mockCSWService, mockViewCSWRecordFactory, mockPropertyConfigurer);
    }

    /**
     * Test get record response_ success.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetRecordResponse_Success() throws Exception {
        final StringWriter actualJSONResponse = new StringWriter();
        final ModelMap viewCSWRecord1 = new ModelMap();
        final ModelMap viewCSWRecord2 = new ModelMap();

        viewCSWRecord1.put("rec1", "val1");
        viewCSWRecord2.put("rec2", "val2");

        context.checking(new Expectations() {{
            oneOf(mockCSWService).getRecordCache();
            will(returnValue(Arrays.asList(mockCSWRecord1, mockCSWRecord2)));

            oneOf(mockViewCSWRecordFactory).toView(mockCSWRecord1);
            will(returnValue(viewCSWRecord1));
            oneOf(mockViewCSWRecordFactory).toView(mockCSWRecord2);
            will(returnValue(viewCSWRecord2));

            //check that the correct response is getting output
            oneOf(mockHttpResponse).setContentType(with(any(String.class)));
            oneOf(mockHttpResponse).getWriter();
            will(returnValue(new PrintWriter(actualJSONResponse)));
        }});

        //Run the method, get our response rendered as a JSONObject
        ModelAndView mav = cswController.getCSWRecords();
        mav.getView().render(mav.getModel(), mockHttpRequest, mockHttpResponse);
        JSONObject jsonObj = JSONObject.fromObject(actualJSONResponse.toString());

        //Check our response contains useful info...
        Assert.assertEquals(true, jsonObj.getBoolean(SUCCESSJSON));
        JSONArray records = jsonObj.getJSONArray("data");
        Assert.assertNotNull(records);
        Assert.assertEquals(2, records.size());

        JSONObject jsonRec1 = records.getJSONObject(0);
        JSONObject jsonRec2 = records.getJSONObject(1);

        Assert.assertEquals("val1", jsonRec1.get("rec1"));
        Assert.assertEquals("val2", jsonRec2.get("rec2"));
    }


    /**
     * Test get record response_ transform error.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetRecordResponse_TransformError() throws Exception {
        final StringWriter actualJSONResponse = new StringWriter();
        final ModelMap viewCSWRecord1 = new ModelMap();
        final ModelMap viewCSWRecord2 = new ModelMap();

        viewCSWRecord1.put("rec1", "val1");
        viewCSWRecord2.put("rec2", "val2");

        context.checking(new Expectations() {{
            oneOf(mockCSWService).getRecordCache();
            will(returnValue(Arrays.asList(mockCSWRecord1, mockCSWRecord2)));

            oneOf(mockViewCSWRecordFactory).toView(mockCSWRecord1);
            will(returnValue(viewCSWRecord1));
            oneOf(mockViewCSWRecordFactory).toView(mockCSWRecord2);
            will(throwException(new Exception()));

            //check that the correct response is getting output
            oneOf(mockHttpResponse).setContentType(with(any(String.class)));
            oneOf(mockHttpResponse).getWriter();
            will(returnValue(new PrintWriter(actualJSONResponse)));
        }});

        //Run the method, get our response rendered as a JSONObject
        ModelAndView mav = cswController.getCSWRecords();
        mav.getView().render(mav.getModel(), mockHttpRequest, mockHttpResponse);
        JSONObject jsonObj = JSONObject.fromObject(actualJSONResponse.toString());

        //Check our response contains useful info...
        Assert.assertEquals(false, jsonObj.getBoolean(SUCCESSJSON));
        JSONArray records = (JSONArray)jsonObj.get("data");
        Assert.assertNotNull(records);
        Assert.assertEquals(0, records.size());
    }

    /**
     * Tests that the underlying services are called correctly and the response
     * is transformed into an appropriate format.
     */
    @Test
    public void testGetKeywords() {
        final Map<String, Set<CSWRecord>> expectedKeywords = new HashMap<String, Set<CSWRecord>>();
        expectedKeywords.put("keyword1", new HashSet<CSWRecord>(Arrays.asList(new CSWRecord("a"), new CSWRecord("b"))));
        expectedKeywords.put("keyword1", new HashSet<CSWRecord>(Arrays.asList(new CSWRecord("c"), new CSWRecord("b"), new CSWRecord("a"))));

        ModelMap kw1 = new ModelMap();
        kw1.put("keyword", "keyword1");
        kw1.put("count", 2);
        ModelMap kw2 = new ModelMap();
        kw2.put("keyword", "keyword2");
        kw2.put("count", 3);

        context.checking(new Expectations() {{
            oneOf(mockCSWService).getKeywordCache();
            will(returnValue(expectedKeywords));
        }});

        ModelAndView mav = cswController.getCSWKeywords();
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean)mav.getModel().get(SUCCESSJSON));

        List<ModelMap> data = (List<ModelMap>) mav.getModel().get("data");
        Assert.assertEquals(expectedKeywords.size(), data.size());
        for (ModelMap kwResponse : data) {

            String keyword = (String) kwResponse.get("keyword");
            Integer count = (Integer)kwResponse.get("count");

            Assert.assertEquals(expectedKeywords.get(keyword).size(), count.intValue());
        }
    }

}

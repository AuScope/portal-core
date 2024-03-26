package org.auscope.portal.core.server.controllers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import org.auscope.portal.core.services.CSWCacheService;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.test.ByteBufferedServletOutputStream;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.view.ViewCSWRecordFactory;
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
     */
    @Before
    public void setUp() {
        context.checking(new Expectations() {{
         //   oneOf(mockCSWService).updateCache();
        }});

        cswController = new CSWCacheController(mockCSWService, mockViewCSWRecordFactory);
    }

    private String renderMav(ModelAndView mav) throws Exception {
        final ByteBufferedServletOutputStream rawResponse = new ByteBufferedServletOutputStream(1024 * 200);
        context.checking(new Expectations() {
            {
                allowing(mockHttpResponse).setContentType(with(any(String.class)));
                allowing(mockHttpResponse).setCharacterEncoding(with(any(String.class)));
                allowing(mockHttpResponse).addHeader(with(any(String.class)), with(any(String.class)));
                allowing(mockHttpResponse).getOutputStream();will(returnValue(rawResponse));

                allowing(mockHttpRequest).getParameter(with(any(String.class)));will(returnValue(null));
                allowing(mockHttpRequest).getAttribute(with(any(String.class)));will(returnValue(null));
            }
        });
        mav.getView().render(mav.getModel(), mockHttpRequest, mockHttpResponse);

        return rawResponse.getStream().toString("UTF-8");
    }

    /**
     * Test get record response_ success.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetRecordResponse_Success() throws Exception {
        ModelMap viewCSWRecord1 = new ModelMap();
        ModelMap viewCSWRecord2 = new ModelMap();

        viewCSWRecord1.put("rec1", "val1");
        viewCSWRecord2.put("rec2", "val2");

        context.checking(new Expectations() {{
            oneOf(mockCSWService).getRecordCache();
            will(returnValue(Arrays.asList(mockCSWRecord1, mockCSWRecord2)));

            oneOf(mockViewCSWRecordFactory).toView(mockCSWRecord1);
            will(returnValue(viewCSWRecord1));
            oneOf(mockViewCSWRecordFactory).toView(mockCSWRecord2);
            will(returnValue(viewCSWRecord2));
        }});

        //Run the method, get our response rendered as a JSONObject
        ModelAndView mav = cswController.getCSWRecords();
        String json = renderMav(mav);
        JSONObject jsonObj = new JSONObject(json);

        //Check our response contains useful info...
        Assert.assertEquals(true, jsonObj.getBoolean(SUCCESSJSON));
        JSONArray records = jsonObj.getJSONArray("data");
        Assert.assertNotNull(records);
        Assert.assertEquals(2, records.length());

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
        ModelMap viewCSWRecord1 = new ModelMap();
        ModelMap viewCSWRecord2 = new ModelMap();

        viewCSWRecord1.put("rec1", "val1");
        viewCSWRecord2.put("rec2", "val2");

        context.checking(new Expectations() {{
            oneOf(mockCSWService).getRecordCache();
            will(returnValue(Arrays.asList(mockCSWRecord1, mockCSWRecord2)));

            oneOf(mockViewCSWRecordFactory).toView(mockCSWRecord1);
            will(returnValue(viewCSWRecord1));
            oneOf(mockViewCSWRecordFactory).toView(mockCSWRecord2);
            will(throwException(new Exception()));
        }});

        //Run the method, get our response rendered as a JSONObject
        ModelAndView mav = cswController.getCSWRecords();
        String json = renderMav(mav);
        JSONObject jsonObj = new JSONObject(json);

        //Check our response contains useful info...
        Assert.assertEquals(false, jsonObj.getBoolean(SUCCESSJSON));
        JSONArray records = jsonObj.getJSONArray("data");
        Assert.assertNotNull(records);
        Assert.assertEquals(0, records.length());
    }

    /**
     * Tests that the underlying services are called correctly and the response
     * is transformed into an appropriate format.
     */
    @Test
    public void testGetKeywords() {
        Map<String, Set<CSWRecord>> expectedKeywords = new HashMap<>();
        expectedKeywords.put("keyword1", new HashSet<>(Arrays.asList(new CSWRecord("a"), new CSWRecord("b"))));
        expectedKeywords.put("keyword1", new HashSet<>(Arrays.asList(new CSWRecord("c"), new CSWRecord("b"), new CSWRecord("a"))));

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

        @SuppressWarnings("unchecked")
        final
        List<ModelMap> data = (List<ModelMap>) mav.getModel().get("data");
        Assert.assertEquals(expectedKeywords.size(), data.size());
        for (ModelMap kwResponse : data) {

            String keyword = (String) kwResponse.get("keyword");
            Integer count = (Integer)kwResponse.get("count");

            Assert.assertEquals(expectedKeywords.get(keyword).size(), count.intValue());
        }
    }

}

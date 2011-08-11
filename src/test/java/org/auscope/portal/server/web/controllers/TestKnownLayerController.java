package org.auscope.portal.server.web.controllers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.auscope.portal.server.web.KnownLayer;
import org.auscope.portal.server.web.view.ViewKnownLayerFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

public class TestKnownLayerController {
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    private ArrayList knownLayerList;
    private KnownLayer mockDefn1 = context.mock(KnownLayer.class, "defn1");
    private KnownLayer mockDefn2 = context.mock(KnownLayer.class, "defn2");
    private ViewKnownLayerFactory mockViewFactory = context.mock(ViewKnownLayerFactory.class);
    private HttpServletRequest mockHttpRequest = context.mock(HttpServletRequest.class);
    private HttpServletResponse mockHttpResponse = context.mock(HttpServletResponse.class);



    @Before
    public void setup() throws Exception {
        knownLayerList = new ArrayList();

        knownLayerList.add(mockDefn1);
        knownLayerList.add(mockDefn2);
    }

    @Test
    public void testGet_Success() throws Exception {
        KnownLayerController kftController = new KnownLayerController(knownLayerList, mockViewFactory);
        final StringWriter actualJSONResponse = new StringWriter();
        final ModelMap record1 = new ModelMap();
        final ModelMap record2 = new ModelMap();

        record1.put("rec1", "val1");
        record2.put("rec2", "val2");

        context.checking(new Expectations() {{
            oneOf(mockViewFactory).toView(mockDefn1);will(returnValue(record1));
            oneOf(mockViewFactory).toView(mockDefn2);will(returnValue(record2));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});

        //Run the method, get our response rendered as a JSONObject
        ModelAndView mav = kftController.getKnownLayers();
        mav.getView().render(mav.getModel(), mockHttpRequest, mockHttpResponse);
        JSONObject jsonObj = JSONObject.fromObject(actualJSONResponse.toString());

        Assert.assertEquals(true, jsonObj.getBoolean("success"));
        JSONArray records = jsonObj.getJSONArray("data");
        Assert.assertNotNull(records);
        Assert.assertEquals(2, records.size());

        JSONObject jsonRec1 = records.getJSONObject(0);
        JSONObject jsonRec2 = records.getJSONObject(1);

        Assert.assertEquals("val1", jsonRec1.get("rec1"));
        Assert.assertEquals("val2", jsonRec2.get("rec2"));
    }
}

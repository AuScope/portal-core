package org.auscope.portal.core.util;

import java.io.IOException;
import java.util.Hashtable;

import org.junit.Assert;
import org.junit.Test;

public class TestSLDLoader{

    @Test
    public void testLoadTenement() throws IOException {
        Hashtable<String,String> map = new Hashtable<String,String>();
        map.put("name", "testName");
        map.put("fillColor", "testfillColor");
        map.put("fillOpacity", "testFillOpacity");
        map.put("strokeColor", "testStrokeColor");
        map.put("strokeWidth", "1234");


        String s = SLDLoader.loadSLD("org/auscope/portal/core/slds/MineralTenementTest.sld", map,true);

        Assert.assertTrue(s.contains("<Name>testName</Name>"));
        Assert.assertTrue(s.contains("<CssParameter name=\"fill\">testfillColor</CssParameter>"));
        Assert.assertTrue(s.contains("<CssParameter name=\"fill-opacity\">testFillOpacity</CssParameter>"));
        Assert.assertTrue(s.contains("<CssParameter name=\"stroke\">testStrokeColor</CssParameter>"));
        Assert.assertTrue(s.contains("<CssParameter name=\"stroke-width\">1234</CssParameter>"));
    }

}

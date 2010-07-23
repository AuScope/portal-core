package org.auscope.portal.server.web;

import org.apache.commons.httpclient.HttpMethodBase;
import org.junit.Assert;
import org.junit.Test;

public class TestWCSDescribeCoverageMethodMakerGet {
    @Test(expected=IllegalArgumentException.class)
    public void testBadLayer() throws Exception {
        new WCSDescribeCoverageMethodMakerGET().makeMethod("http://fake.com", "");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullLayer() throws Exception {
        new WCSDescribeCoverageMethodMakerGET().makeMethod("http://fake.com", null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullUrl() throws Exception {
        new WCSDescribeCoverageMethodMakerGET().makeMethod(null, "layer_name");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testBadUrl() throws Exception {
        new WCSDescribeCoverageMethodMakerGET().makeMethod("", "layer_name");
    }
    
    @Test
    public void testSimple() throws Exception {
        HttpMethodBase method = new WCSDescribeCoverageMethodMakerGET().makeMethod("http://fake.com/bob", "layer_name");
        
        Assert.assertNotNull(method);
        Assert.assertTrue(method.getQueryString().contains("request=DescribeCoverage"));
        Assert.assertTrue(method.getQueryString().contains("coverage=layer_name"));
        Assert.assertTrue(method.getURI().toString().startsWith("http://fake.com/bob"));
    }
}

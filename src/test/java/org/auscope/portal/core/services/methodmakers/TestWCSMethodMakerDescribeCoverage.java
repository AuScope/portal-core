package org.auscope.portal.core.services.methodmakers;

import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Test;

public class TestWCSMethodMakerDescribeCoverage extends PortalTestClass {
    @Test(expected = IllegalArgumentException.class)
    public void testBadLayer() throws URISyntaxException  {
        new WCSMethodMaker().describeCoverageMethod("http://fake.com", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullLayer() throws URISyntaxException  {
        new WCSMethodMaker().describeCoverageMethod("http://fake.com", null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullUrl() throws URISyntaxException  {
        new WCSMethodMaker().describeCoverageMethod(null, "layer_name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadUrl() throws URISyntaxException  {
        new WCSMethodMaker().describeCoverageMethod("", "layer_name");
    }

    @Test
    public void testSimple() throws URISyntaxException  {
        HttpRequestBase method = new WCSMethodMaker().describeCoverageMethod("http://fake.com/bob", "layer_name");

        Assert.assertNotNull(method);
        Assert.assertTrue(method.getURI().getQuery().contains("request=DescribeCoverage"));
        Assert.assertTrue(method.getURI().getQuery().contains("coverage=layer_name"));
        Assert.assertTrue(method.getURI().toString().startsWith("http://fake.com/bob"));
    }
}

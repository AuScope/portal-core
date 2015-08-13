package org.auscope.portal.core.services.methodmakers.sissvoc;

import java.net.URLDecoder;
import junit.framework.Assert;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Before;
import org.junit.Test;

public class TestSISSVoc2MethodMaker extends PortalTestClass {
    SISSVoc2MethodMaker mm;

    @Before
    public void setup() {
        mm = new SISSVoc2MethodMaker();
    }

    /**
     * Tests the getConceptByLabelMethod performs correctly
     * 
     * @throws Exception
     */
    @Test
    public void testGetConceptByLabelMethod() throws Exception {
        HttpRequestBase method = mm.getConceptByLabelMethod("http://example.org", "repo", "label");

        Assert.assertEquals("http://example.org/getConceptByLabel?repo/label",
                URLDecoder.decode(method.getURI().toString(), "UTF-8"));
    }

    /**
     * Tests the getConceptByLabelMethod performs correctly
     * 
     * @throws Exception
     */
    @Test
    public void testGetConceptByUriMethod() throws Exception {
        HttpRequestBase method = mm.getConceptByUriMethod("http://example.org", "repo", "uri");

        Assert.assertEquals("http://example.org/getConceptByURI?repo/uri",
                URLDecoder.decode(method.getURI().toString(), "UTF-8"));
    }

    /**
     * Tests the getCommodityMethod performs correctly
     * 
     * @throws Exception
     */
    @Test
    public void testGetCommodityMethod() throws Exception {
        HttpRequestBase method = mm.getCommodityMethod("http://example.org", "repo", "label");

        Assert.assertEquals("http://example.org/getCommodity?repo/label",
                URLDecoder.decode(method.getURI().toString(), "UTF-8"));
    }

    /**
     * Tests the getRepositoryInfoMethod performs correctly
     * 
     * @throws Exception
     */
    @Test
    public void testRepoInfo() throws Exception {
        HttpRequestBase method = mm.getRepositoryInfoMethod("http://example.org");

        Assert.assertEquals("http://example.org/RepositoryInfo", method.getURI().toString());
    }
}

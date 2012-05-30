package org.auscope.portal.core.services.methodmakers.sissvoc;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpMethodBase;
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
     * @throws Exception
     */
    @Test
    public void testGetConceptByLabelMethod() throws Exception {
        HttpMethodBase method = mm.getConceptByLabelMethod("http://example.org", "repo", "label");

        Assert.assertEquals("http://example.org/getConceptByLabel?repo/label", method.getURI().toString());
    }

    /**
     * Tests the getConceptByLabelMethod performs correctly
     * @throws Exception
     */
    @Test
    public void testGetConceptByUriMethod() throws Exception {
        HttpMethodBase method = mm.getConceptByUriMethod("http://example.org", "repo", "uri");

        Assert.assertEquals("http://example.org/getConceptByURI?repo/uri", method.getURI().toString());
    }

    /**
     * Tests the getCommodityMethod performs correctly
     * @throws Exception
     */
    @Test
    public void testGetCommodityMethod() throws Exception {
        HttpMethodBase method = mm.getCommodityMethod("http://example.org", "repo", "label");

        Assert.assertEquals("http://example.org/getCommodity?repo/label", method.getURI().toString());
    }

    /**
     * Tests the getRepositoryInfoMethod performs correctly
     * @throws Exception
     */
    @Test
    public void testRepoInfo() throws Exception {
        HttpMethodBase method = mm.getRepositoryInfoMethod("http://example.org");

        Assert.assertEquals("http://example.org/RepositoryInfo", method.getURI().toString());
    }
}

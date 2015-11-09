package org.auscope.portal.core.services.methodmakers;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.services.methodmakers.CSWMethodMakerGetDataRecords.ResultType;
import org.auscope.portal.core.services.methodmakers.filter.csw.CSWGetDataRecordsFilter;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.DOMUtil;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for CSWMethodMakerGetDataRecords
 * 
 * @author Josh Vote
 *
 */
public class TestCSWMethodMakerGetDataRecords extends PortalTestClass {

    private static final String uri = "http://test.com";

    private CSWMethodMakerGetDataRecords methodMaker;
    private CSWGetDataRecordsFilter mockFilter;

    /**
     * Setup each unit test
     */
    @Before
    public void init() {
        mockFilter = context.mock(CSWGetDataRecordsFilter.class);
        methodMaker = new CSWMethodMakerGetDataRecords();
    }

    /**
     * Simple test to ensure that makeMethod includes all appropriate fields
     */
    @Test
    public void testMakeMethodFilter() throws Exception {
        final int maxRecords = 1234;
        final String filterStr = "<filter/>";

        context.checking(new Expectations() {
            {
                allowing(mockFilter).getSortType();
                oneOf(mockFilter).getFilterStringAllRecords();
                will(returnValue(filterStr));
            }
        });

        HttpRequestBase method = methodMaker.makeMethod(uri, mockFilter, ResultType.Results, maxRecords);
        Assert.assertNotNull(method);

        Assert.assertTrue(method instanceof HttpPost); //we want this to be sent via post in case we get a large filter
        String postBody = IOUtils.toString(((HttpPost) method).getEntity().getContent());

        Assert.assertTrue(postBody.contains(String.format("maxRecords=\"%1$s\"", maxRecords)));
        Assert.assertTrue(postBody.contains(String.format("resultType=\"results\"")));
        Assert.assertTrue(postBody.contains(filterStr));

        Assert.assertNotNull(DOMUtil.buildDomFromString(postBody));//this should NOT throw an exception
    }

    /**
     * Simple test to ensure that makeMethod includes all appropriate fields
     */
    @Test
    public void testMakeMethodNoFilter() throws Exception {
        final int maxRecords = 14;

        context.checking(new Expectations() {
            {

            }
        });

        HttpRequestBase method = methodMaker.makeMethod(uri, null, ResultType.Hits, maxRecords);
        Assert.assertNotNull(method);

        Assert.assertTrue(method instanceof HttpPost); //we want this to be sent via post in case we get a large filter

        String postBody = IOUtils.toString(((HttpPost) method).getEntity().getContent());

        Assert.assertTrue(postBody.contains(String.format("maxRecords=\"%1$s\"", maxRecords)));
        Assert.assertTrue(postBody.contains(String.format("resultType=\"hits\"")));
        Assert.assertFalse(postBody.contains("csw:Constraint"));

        Assert.assertNotNull(DOMUtil.buildDomFromString(postBody));//this should NOT throw an exception
    }

    /**
     * Simple test to ensure that some of the 'mandatory' parameters are set correctly
     */
    @Test
    public void testKeyParameters() throws Exception {
        final int maxRecords = 1234;
        final String filterStr = "<filter/>";

        context.checking(new Expectations() {
            {
                allowing(mockFilter).getSortType();
                allowing(mockFilter).getFilterStringAllRecords();
                will(returnValue(filterStr));
            }
        });

        //Test POST
        HttpRequestBase method = methodMaker.makeMethod(uri, mockFilter, ResultType.Results, maxRecords);
        Assert.assertNotNull(method);
        String postBody = IOUtils.toString(((HttpPost) method).getEntity().getContent());
        Assert.assertTrue(postBody.contains(String.format("version=\"2.0.2\"")));
        Assert.assertTrue(postBody.contains(String.format("outputSchema=\"csw:IsoRecord\"")));
        Assert.assertTrue(postBody.contains(String.format("typeNames=\"gmd:MD_Metadata\"")));

        //Test GET
        method = methodMaker.makeGetMethod(uri, ResultType.Results, maxRecords, 0);
        Assert.assertNotNull(method);
        String queryString = ((HttpGet) method).getURI().getQuery();
        Assert.assertTrue(queryString, queryString.contains("version=2.0.2"));
        Assert.assertTrue(queryString, queryString.contains("outputSchema=csw:IsoRecord"));
        Assert.assertTrue(queryString, queryString.contains("typeNames=gmd:MD_Metadata"));

    }
}

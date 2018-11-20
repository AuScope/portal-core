package org.auscope.portal.core.services;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class TestNamespaceService extends PortalTestClass {

    private NamespaceService namespaceService;

    private WFSGetFeatureMethodMaker mockMethodMaker = context.mock(WFSGetFeatureMethodMaker.class, "mockMethodMaker");
    private HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class, "mockServiceCaller");

    private HttpRequestBase mockMethod = context.mock(HttpRequestBase.class, "mockMethod");

    private String mockServiceUrl = "http://mock.wfs";
    private String prefix = "gml";

    @Before
    public void setUp() throws Exception {

        this.namespaceService = new NamespaceService(mockServiceCaller, mockMethodMaker);
    }

    @Test
    public void testGetURI() throws URISyntaxException, IOException {

        final String getCapabilitiesResponse = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/wfs/GetCapabilitiesResponse.xml");

        context.checking(new Expectations() {
            {
             oneOf(mockMethodMaker).makeGetCapabilitiesMethod(mockServiceUrl);
             will(returnValue(mockMethod));
             oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod);
             will(returnValue(getCapabilitiesResponse));
             oneOf(mockMethod).releaseConnection();
            }
        });

        String gmlNamespace = this.namespaceService.getNamespaceURI(mockServiceUrl,prefix);
        Assert.assertEquals(gmlNamespace, "http://www.opengis.net/gml");
    }
    

}
package org.auscope.portal.core.services;

import java.util.Date;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.OgcServiceProviderType;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.SOSMethodMaker;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.test.PortalTestClass;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestSOSService extends PortalTestClass {
    HttpRequestBase mockHttpMethodBase = context.mock(HttpRequestBase.class);
    HttpServiceCaller mockHttpServiceCaller = context.mock(HttpServiceCaller.class);
    SOSMethodMaker mockSosMethodMaker = context.mock(SOSMethodMaker.class);
    SOSService sosService = null;

    @Before
    public void init() {
        sosService = new SOSService(mockHttpServiceCaller, mockSosMethodMaker);
    }

    @Test
    public void testGetObserFOIRequest() {
        final String sosUrl = "http://example.org/sos";
        final String request = "GetObservation";
        final String featureID = "testID";

        context.checking(new Expectations() {
            {
                oneOf(mockSosMethodMaker).makePostMethod(sosUrl, request, featureID, null, null, null);
                will(returnValue(mockHttpMethodBase));
            }
        });

        Assert.assertSame(mockHttpMethodBase,
                sosService.generateSOSRequest(sosUrl, request, featureID, null, null, null));
    }

    @Test
    public void testGetObsTemporalFilterRequest() {
        final String sosUrl = "http://example.org/sos";
        final String request = "GetObservation";
        final long oneDay = (long) 1000.0 * 60 * 60 * 24;
        final Date today = new Date(System.currentTimeMillis());
        final Date yesterday = new Date(System.currentTimeMillis() - oneDay);

        context.checking(new Expectations() {
            {
                oneOf(mockSosMethodMaker).makePostMethod(sosUrl, request, null, yesterday, today, null);
                will(returnValue(mockHttpMethodBase));
            }
        });

        Assert.assertSame(mockHttpMethodBase,
                sosService.generateSOSRequest(sosUrl, request, null, yesterday, today, null));
    }

    @Test
    public void testGetObsBBOXFilterRequest() {
        final String sosUrl = "http://example.org/sos";
        final String request = "GetObservation";
        final String bboxFilter = "{\"crs\":\"EPSG:4326\",\"eastBoundLongitude\":154.1,\"westBoundLongitude\":112.8,\"southBoundLatitude\":-44.0,\"northBoundLatitude\":-8.9}";
        final OgcServiceProviderType dummyOgcServiceProviderType = OgcServiceProviderType.ArcGis;
        final FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxFilter, dummyOgcServiceProviderType);

        context.checking(new Expectations() {
            {
                oneOf(mockSosMethodMaker).makePostMethod(sosUrl, request, null, null, null, bbox);
                will(returnValue(mockHttpMethodBase));
            }
        });

        Assert.assertSame(mockHttpMethodBase, sosService.generateSOSRequest(sosUrl, request, null, null, null, bbox));
    }

}

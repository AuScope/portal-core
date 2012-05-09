package org.auscope.portal.server.web.service;

import java.io.InputStream;
import java.net.ConnectException;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.auscope.portal.PortalTestClass;
import org.auscope.portal.csw.record.CSWGeographicBoundingBox;
import org.auscope.portal.server.domain.ows.GetCapabilitiesRecord;
import org.auscope.portal.server.domain.ows.GetCapabilitiesWMSLayerRecord;
import org.auscope.portal.server.web.WMSMethodMaker;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for GetCapabilitiesService
 * @author Josh Vote
 *
 */
public class TestGetCapabilitiesService extends PortalTestClass {

    private GetCapabilitiesService service;
    private HttpServiceCaller mockServiceCaller;
    private HttpClient mockHttpClient;

    @Before
    public void setup() throws Exception {
        mockServiceCaller = context.mock(HttpServiceCaller.class);
        mockHttpClient = context.mock(HttpClient.class);
        service = new GetCapabilitiesService(mockServiceCaller);
    }

    /**
     * Tests parsing of a standard WMS GetCapabilities response
     * @throws Exception
     */
    @Test
    public void testParsingWMS111() throws Exception {
        final String serviceUrl = "http://service/wms";
        final InputStream is = this.getClass().getResourceAsStream("/GetCapabilitiesControllerWMSResponse_1_1_1.xml");
        final String wmsGetCapRequestUrl = new WMSMethodMaker(serviceUrl).getCapabilitiesMethod().getURI().toString();

        context.checking(new Expectations() {{
            oneOf(mockServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(mockServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, wmsGetCapRequestUrl, null)), with(mockHttpClient));will(returnValue(is));
        }});

        GetCapabilitiesRecord record = service.getWmsCapabilities(serviceUrl);
        Assert.assertNotNull(record);
        Assert.assertEquals("wms", record.getServiceType());
        Assert.assertEquals("Test organization", record.getOrganisation());
        Assert.assertEquals("http://localhost:8080/geoserver/wms?SERVICE=WMS&", record.getMapUrl());
        Assert.assertArrayEquals(new String[] {
                "EPSG:4326",
                "epsg:4326",
                "AUTO:42003",
                "AUTO:42004",
                "EPSG:WGS84(DD)"}, record.getLayerSRS());


        List<GetCapabilitiesWMSLayerRecord> layers = record.getLayers();
        Assert.assertNotNull(layers);
        Assert.assertEquals(22, layers.size());

        //Test our second
        GetCapabilitiesWMSLayerRecord layer = layers.get(1);
        CSWGeographicBoundingBox bbox = layer.getBoundingBox();
        Assert.assertEquals("An Abstract", layer.getAbstract());
        Assert.assertEquals(3, bbox.getEastBoundLongitude(), 0.01);
        Assert.assertEquals(-2, bbox.getSouthBoundLatitude(), 0.01);
        Assert.assertEquals(-1, bbox.getWestBoundLongitude(), 0.01);
        Assert.assertEquals(4, bbox.getNorthBoundLatitude(), 0.01);
        Assert.assertEquals("nurc:Arc_Sample", layer.getName());
        Assert.assertArrayEquals(new String[] {"EPSG:4326"}, layer.getChildLayerSRS());
        Assert.assertEquals("A sample ArcGrid file", layer.getTitle());

        //And our last record
        layer = layers.get(21);
        bbox = layer.getBoundingBox();
        Assert.assertEquals("Layer-Group type layer: tiger-ny", layer.getAbstract());
        Assert.assertEquals(-73.907005, bbox.getEastBoundLongitude(), 0.00001);
        Assert.assertEquals(40.679648, bbox.getSouthBoundLatitude(), 0.00001);
        Assert.assertEquals(-74.047185, bbox.getWestBoundLongitude(), 0.00001);
        Assert.assertEquals(40.882078, bbox.getNorthBoundLatitude(), 0.00001);
        Assert.assertEquals("tiger-ny", layer.getName());
        Assert.assertArrayEquals(new String[] {"EPSG:4326"}, layer.getChildLayerSRS());
        Assert.assertEquals("tiger-ny", layer.getTitle());
    }

    /**
     * Tests that failure is passed up the chain as expected
     */
    @Test(expected=ConnectException.class)
    public void testRequestFailure() throws Exception {
        final String serviceUrl = "http://service/wms";
        final String wmsGetCapRequestUrl = new WMSMethodMaker(serviceUrl).getCapabilitiesMethod().getURI().toString();

        context.checking(new Expectations() {{
            oneOf(mockServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(mockServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, wmsGetCapRequestUrl, null)), with(mockHttpClient));will(throwException(new ConnectException()));
        }});

        service.getWmsCapabilities(serviceUrl);
    }
}

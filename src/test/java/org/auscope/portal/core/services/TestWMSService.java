package org.auscope.portal.core.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.WMSMethodMaker;
import org.auscope.portal.core.services.methodmakers.WMSMethodMakerInterface;
import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesRecord;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesRecord_1_1_1;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesWMSLayerRecord;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Unit tests for GetCapabilitiesService
 * 
 * @author Josh Vote
 *
 */
public class TestWMSService extends PortalTestClass {

    private WMSService service;
    private HttpServiceCaller mockServiceCaller;
    private WMSMethodMaker mockMethodMaker;
    private HttpRequestBase mockMethod;

    @Before
    public void setup() {
        List<WMSMethodMakerInterface> methodMaker = new ArrayList<>();
        mockServiceCaller = context.mock(HttpServiceCaller.class);
        mockMethodMaker = context.mock(WMSMethodMaker.class);
        mockMethod = context.mock(HttpRequestBase.class);
        methodMaker.add(mockMethodMaker);
        service = new WMSService(mockServiceCaller, methodMaker);
    }

    /**
     * Tests parsing of a standard WMS GetCapabilities response
     * @throws XPathExpressionException 
     * @throws IOException 
     * @throws URISyntaxException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     * @throws PortalServiceException 
     */
    @Test
    public void testParsingWMS111() throws XPathExpressionException, URISyntaxException, IOException, ParserConfigurationException, SAXException, PortalServiceException {
        final String serviceUrl = "http://service/wms";
        try (final InputStream is = ResourceUtil
                .loadResourceAsStream(
                        "org/auscope/portal/core/test/responses/wms/GetCapabilitiesControllerWMSResponse_1_1_1.xml")) {

            final GetCapabilitiesRecord getCapRecord = new GetCapabilitiesRecord_1_1_1(is);

            context.checking(new Expectations() {
                {
                    oneOf(mockMethodMaker).accepts(with(any(String.class)), with((String) null),
                            with(any(StringBuilder.class)));
                    will(returnValue(true));

                    oneOf(mockMethodMaker).getCapabilitiesMethod(serviceUrl);
                    will(returnValue(mockMethod));

                    oneOf(mockMethodMaker).getGetCapabilitiesRecord(mockMethod);
                    will(returnValue(getCapRecord));

                    oneOf(mockMethod).releaseConnection();
                }
            });

            GetCapabilitiesRecord record = service.getWmsCapabilities(serviceUrl, null);
            Assert.assertNotNull(record);
            Assert.assertEquals("wms", record.getServiceType());
            Assert.assertEquals("Test organization", record.getOrganisation());
            Assert.assertEquals("http://localhost:8080/geoserver/wms?SERVICE=WMS&", record.getMapUrl());
            Assert.assertArrayEquals(new String[] {
                    "EPSG:4326",
                    "EPSG:3857",
                    "epsg:4326",
                    "AUTO:42003",
                    "AUTO:42004",
                    "EPSG:WGS84(DD)" }, record.getLayerSRS());

            List<GetCapabilitiesWMSLayerRecord> layers = record.getLayers();
            Assert.assertNotNull(layers);
            Assert.assertEquals(22, layers.size());

            // Test our second
            GetCapabilitiesWMSLayerRecord layer = layers.get(1);
            CSWGeographicBoundingBox bbox = layer.getBoundingBox();
            Assert.assertEquals("An Abstract", layer.getAbstract());
            Assert.assertEquals("http://www.metadataURL.com", layer.getMetadataURL());
            Assert.assertEquals(3, bbox.getEastBoundLongitude(), 0.01);
            Assert.assertEquals(-2, bbox.getSouthBoundLatitude(), 0.01);
            Assert.assertEquals(-1, bbox.getWestBoundLongitude(), 0.01);
            Assert.assertEquals(4, bbox.getNorthBoundLatitude(), 0.01);
            Assert.assertEquals("nurc:Arc_Sample", layer.getName());
            Assert.assertArrayEquals(new String[] { "EPSG:4326" }, layer.getChildLayerSRS());
            Assert.assertEquals("A sample ArcGrid file", layer.getTitle());

            // And our last record
            layer = layers.get(21);
            bbox = layer.getBoundingBox();
            Assert.assertEquals("Layer-Group type layer: tiger-ny", layer.getAbstract());
            Assert.assertEquals(-73.907005, bbox.getEastBoundLongitude(), 0.00001);
            Assert.assertEquals(40.679648, bbox.getSouthBoundLatitude(), 0.00001);
            Assert.assertEquals(-74.047185, bbox.getWestBoundLongitude(), 0.00001);
            Assert.assertEquals(40.882078, bbox.getNorthBoundLatitude(), 0.00001);
            Assert.assertEquals("tiger-ny", layer.getName());
            Assert.assertArrayEquals(new String[] { "EPSG:4326" }, layer.getChildLayerSRS());
            Assert.assertEquals("tiger-ny", layer.getTitle());
        }
    }

    /**
     * Tests that failure is passed up the chain as expected
     * @throws PortalServiceException 
     * @throws IOException 
     * @throws URISyntaxException 
     */
    @Test(expected = PortalServiceException.class)
    public void testRequestFailure() throws PortalServiceException, URISyntaxException, IOException {
        final String serviceUrl = "http://service/wms";
        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).accepts(with(any(String.class)), with(aNull(String.class)), with(any(StringBuilder.class)));
                will(returnValue(true));

                oneOf(mockMethodMaker).getCapabilitiesMethod(serviceUrl);
                will(returnValue(mockMethod));

                oneOf(mockMethodMaker).getGetCapabilitiesRecord(mockMethod);
                will(returnValue(throwException(new ConnectException())));

                oneOf(mockMethod).releaseConnection();
            }
        });

        service.getWmsCapabilities(serviceUrl, null);
    }

    @Test
    public void testGetFeatureInfo() throws PortalServiceException, URISyntaxException, IOException {
        final String wmsUrl = "http://example.org/wms";
        final String format = "format";
        final String layer = "layer";
        final String srs = "srs";
        final double westBoundLongitude = 1;
        final double southBoundLatitude = 2;
        final double eastBoundLongitude = 3;
        final double northBoundLatitude = 4;
        final int width = 5;
        final int height = 6;
        final double pointLng = 7;
        final double pointLat = 8;
        final int pointX = 9;
        final int pointY = 10;
        final String styles = "styles";

        final String response = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/wfs/EmptyWFSResponse.xml");

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).accepts(with(any(String.class)), with(aNull(String.class)), with(any(StringBuilder.class)));
                will(returnValue(true));

                oneOf(mockMethodMaker).getFeatureInfo(wmsUrl, format, layer, srs, westBoundLongitude,
                        southBoundLatitude, eastBoundLongitude, northBoundLatitude, width, height, pointLng, pointLat,
                        pointX, pointY, styles, null, "0");
                will(returnValue(mockMethod));

                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod);
                will(returnValue(response));
            }
        });

        Assert.assertEquals(response, service.getFeatureInfo(wmsUrl, format, layer, srs, westBoundLongitude,
                southBoundLatitude, eastBoundLongitude, northBoundLatitude, width, height, pointLng, pointLat, pointX,
                pointY, styles, null, false, null, "0", false));
    }

    @Test
    public void testGetFeatureInfoPost() throws PortalServiceException, URISyntaxException, IOException {
        final String wmsUrl = "http://example.org/wms";
        final String format = "format";
        final String layer = "layer";
        final String srs = "srs";
        final double westBoundLongitude = 1;
        final double southBoundLatitude = 2;
        final double eastBoundLongitude = 3;
        final double northBoundLatitude = 4;
        final int width = 5;
        final int height = 6;
        final double pointLng = 7;
        final double pointLat = 8;
        final int pointX = 9;
        final int pointY = 10;
        final String styles = "styles";

        final String response = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/wfs/EmptyWFSResponse.xml");

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).accepts(with(any(String.class)), with(aNull(String.class)), with(any(StringBuilder.class)));
                will(returnValue(true));

                oneOf(mockMethodMaker).getFeatureInfoPost(wmsUrl, format, layer, srs, westBoundLongitude,
                        southBoundLatitude, eastBoundLongitude, northBoundLatitude, width, height, pointLng, pointLat,
                        pointX, pointY, styles, null, "0");
                will(returnValue(mockMethod));

                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod);
                will(returnValue(response));
            }
        });

        Assert.assertEquals(response, service.getFeatureInfo(wmsUrl, format, layer, srs, westBoundLongitude,
                southBoundLatitude, eastBoundLongitude, northBoundLatitude, width, height, pointLng, pointLat, pointX,
                pointY, styles, null, true, null, "0", false));
    }

    @Test(expected = PortalServiceException.class)
    public void testGetFeatureInfoError() throws PortalServiceException, URISyntaxException, IOException {
        final String wmsUrl = "http://example.org/wms";
        final String format = "format";
        final String layer = "layer";
        final String srs = "srs";
        final double westBoundLongitude = 1;
        final double southBoundLatitude = 2;
        final double eastBoundLongitude = 3;
        final double northBoundLatitude = 4;
        final int width = 5;
        final int height = 6;
        final double pointLng = 7;
        final double pointLat = 8;
        final int pointX = 9;
        final int pointY = 10;
        final String styles = "styles";

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).accepts(with(any(String.class)), with(aNull(String.class)), with(any(StringBuilder.class)));
                will(returnValue(true));

                oneOf(mockMethodMaker).getFeatureInfo(wmsUrl, format, layer, srs, westBoundLongitude,
                        southBoundLatitude, eastBoundLongitude, northBoundLatitude, width, height, pointLng, pointLat,
                        pointX, pointY, styles, null, "0");
                will(returnValue(mockMethod));

                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod);
                will(throwException(new IOException()));
            }
        });

        service.getFeatureInfo(wmsUrl, format, layer, srs, westBoundLongitude, southBoundLatitude, eastBoundLongitude,
                northBoundLatitude, width, height, pointLng, pointLat, pointX, pointY, styles, null, false, null, "0",
                false);
    }

    @Test(expected = PortalServiceException.class)
    public void testGetFeatureInfoOwsError() throws PortalServiceException, URISyntaxException, IOException {
        final String wmsUrl = "http://example.org/wms";
        final String format = "format";
        final String layer = "layer";
        final String srs = "srs";
        final double westBoundLongitude = 1;
        final double southBoundLatitude = 2;
        final double eastBoundLongitude = 3;
        final double northBoundLatitude = 4;
        final int width = 5;
        final int height = 6;
        final double pointLng = 7;
        final double pointLat = 8;
        final int pointX = 9;
        final int pointY = 10;
        final String styles = "styles";

        final String response = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/ows/OWSExceptionSample1.xml");

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).accepts(with(any(String.class)), with(aNull(String.class)), with(any(StringBuilder.class)));
                will(returnValue(true));

                oneOf(mockMethodMaker).getFeatureInfo(wmsUrl, format, layer, srs, westBoundLongitude,
                        southBoundLatitude, eastBoundLongitude, northBoundLatitude, width, height, pointLng, pointLat,
                        pointX, pointY, styles, null, "0");
                will(returnValue(mockMethod));

                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod);
                will(returnValue(response));
            }
        });

        service.getFeatureInfo(wmsUrl, format, layer, srs, westBoundLongitude, southBoundLatitude, eastBoundLongitude,
                northBoundLatitude, width, height, pointLng, pointLat, pointX, pointY, styles, null, false, null, "0",
                false);
    }
}

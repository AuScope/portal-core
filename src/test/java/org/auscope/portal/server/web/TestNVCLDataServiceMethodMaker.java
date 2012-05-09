package org.auscope.portal.server.web;

import junit.framework.Assert;

import org.apache.commons.httpclient.URI;
import org.auscope.portal.PortalTestClass;
import org.auscope.portal.server.web.NVCLDataServiceMethodMaker.PlotScalarGraphType;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the NVCLDataServiceMethodMaker
 *
 * Tests according to the specification at https://twiki.auscope.org/wiki/CoreLibrary/WebServicesDevelopment
 *
 * @author Josh Vote
 *
 */
public class TestNVCLDataServiceMethodMaker extends PortalTestClass {

    final String serviceUrl = "https://test.url/NVCL-data-service";
    final String holeIdentifier = "hole-identifier";
    final String logIdentifier = "log-identifier";
    final String datasetId = "dataset-id";
    final String email = "user@email-notadomain.com";
    private NVCLDataServiceMethodMaker methodMaker;

    @Before
    public void setup() {
        methodMaker = new NVCLDataServiceMethodMaker();
    }

    private void assertContainsURLParam(URI uri, String paramName, String paramValue) throws Exception {
        Assert.assertTrue(uri.getQuery().contains(String.format("%1$s=%2$s", paramName, paramValue)));
    }

    private void assertDoesntContainURLParam(URI uri, String paramName) throws Exception {
        Assert.assertFalse(uri.getQuery().contains(paramName));
    }

    @Test
    public void testParamValidity_GetDatasetCollection() throws Exception {
        URI uri = methodMaker.getDatasetCollectionMethod(serviceUrl, holeIdentifier).getURI();
        assertContainsURLParam(uri, "holeidentifier", holeIdentifier);
    }

    @Test
    public void testParamValidity_GetLogCollection() throws Exception {
        //Mandatory only
        URI uri = methodMaker.getLogCollectionMethod(serviceUrl, datasetId, null).getURI();
        assertContainsURLParam(uri, "datasetid", datasetId);
        assertDoesntContainURLParam(uri, "mosaicsvc");

        //Optional Params
        uri = methodMaker.getLogCollectionMethod(serviceUrl, datasetId, true).getURI();
        assertContainsURLParam(uri, "datasetid", datasetId);
        assertContainsURLParam(uri, "mosaicsvc", "yes");

        uri = methodMaker.getLogCollectionMethod(serviceUrl, datasetId, false).getURI();
        assertContainsURLParam(uri, "datasetid", datasetId);
        assertContainsURLParam(uri, "mosaicsvc", "no");
    }

    @Test
    public void testParamValidity_GetPlotScalar() throws Exception {
        //Mandatory
        URI uri = methodMaker.getPlotScalarMethod(serviceUrl, logIdentifier, null, null, null, null, null, null).getURI();
        assertContainsURLParam(uri, "logid", logIdentifier);
        assertDoesntContainURLParam(uri, "startdepth");
        assertDoesntContainURLParam(uri, "enddepth");
        assertDoesntContainURLParam(uri, "samplinginterval");
        assertDoesntContainURLParam(uri, "width");
        assertDoesntContainURLParam(uri, "height");
        assertDoesntContainURLParam(uri, "graphtype");

        //Optional (also test the various PlotScalarGraphType mappins)
        uri = methodMaker.getPlotScalarMethod(serviceUrl, logIdentifier, 10, 20, 30, 40, 4.5, PlotScalarGraphType.LineChart).getURI();
        assertContainsURLParam(uri, "logid", logIdentifier);
        assertContainsURLParam(uri, "startdepth", "10");
        assertContainsURLParam(uri, "enddepth", "20");
        assertContainsURLParam(uri, "samplinginterval", "4.5");
        assertContainsURLParam(uri, "width", "30");
        assertContainsURLParam(uri, "height", "40");
        assertContainsURLParam(uri, "graphtype", "3");

        uri = methodMaker.getPlotScalarMethod(serviceUrl, logIdentifier, 10, 20, 30, 40, 4.5, PlotScalarGraphType.ScatteredChart).getURI();
        assertContainsURLParam(uri, "logid", logIdentifier);
        assertContainsURLParam(uri, "startdepth", "10");
        assertContainsURLParam(uri, "enddepth", "20");
        assertContainsURLParam(uri, "samplinginterval", "4.5");
        assertContainsURLParam(uri, "width", "30");
        assertContainsURLParam(uri, "height", "40");
        assertContainsURLParam(uri, "graphtype", "2");

        uri = methodMaker.getPlotScalarMethod(serviceUrl, logIdentifier, 10, 20, 30, 40, 4.5, PlotScalarGraphType.StackedBarChart).getURI();
        assertContainsURLParam(uri, "logid", logIdentifier);
        assertContainsURLParam(uri, "startdepth", "10");
        assertContainsURLParam(uri, "enddepth", "20");
        assertContainsURLParam(uri, "samplinginterval", "4.5");
        assertContainsURLParam(uri, "width", "30");
        assertContainsURLParam(uri, "height", "40");
        assertContainsURLParam(uri, "graphtype", "1");
    }

    @Test
    public void testParamValidity_GetMosaic() throws Exception {
        //Mandatory only
        URI uri = methodMaker.getMosaicMethod(serviceUrl, logIdentifier, null, null, null).getURI();
        assertContainsURLParam(uri, "logid", logIdentifier);
        assertDoesntContainURLParam(uri, "width");
        assertDoesntContainURLParam(uri, "startsampleno");
        assertDoesntContainURLParam(uri, "endsampleno");

        uri = methodMaker.getMosaicMethod(serviceUrl, logIdentifier, 10, 20, 30).getURI();
        assertContainsURLParam(uri, "logid", logIdentifier);
        assertContainsURLParam(uri, "width", "10");
        assertContainsURLParam(uri, "startsampleno", "20");
        assertContainsURLParam(uri, "endsampleno", "30");
    }

    /**
     * Ensure we don't allow a download request with no ID/filter
     * @throws Exception
     */
    @Test(expected=IllegalArgumentException.class)
    public void testGetTSGDownloadNoID() throws Exception {
        methodMaker.getDownloadTSGMethod(serviceUrl, email, null, null, null, null, null, null, null, null);
    }

    /**
     * Ensure we don't allow a download request with both an ID/filter
     * @throws Exception
     */
    @Test(expected=IllegalArgumentException.class)
    public void testGetTSGDownloadBothIDs() throws Exception {
        methodMaker.getDownloadTSGMethod(serviceUrl, email, "test", "test", null, null, null, null, null, null);
    }

    @Test
    public void testParamValidity_TSGDownload() throws Exception {
        //Mandatory only
        URI uri = methodMaker.getDownloadTSGMethod(serviceUrl, email, datasetId, null, null, null, null, null, null, null).getURI();
        assertContainsURLParam(uri, "datasetid", datasetId);
        assertContainsURLParam(uri, "email", email);
        assertDoesntContainURLParam(uri, "match_string");
        assertDoesntContainURLParam(uri, "linescan");
        assertDoesntContainURLParam(uri, "spectra");
        assertDoesntContainURLParam(uri, "profilometer");
        assertDoesntContainURLParam(uri, "traypics");
        assertDoesntContainURLParam(uri, "mospic");
        assertDoesntContainURLParam(uri, "mappics");

        //Optional
        uri = methodMaker.getDownloadTSGMethod(serviceUrl, email, datasetId, null, false, true, false, true, false, true).getURI();
        assertContainsURLParam(uri, "datasetid", datasetId);
        assertContainsURLParam(uri, "email", email);
        assertDoesntContainURLParam(uri, "match_string");
        assertContainsURLParam(uri, "linescan", "no");
        assertContainsURLParam(uri, "spectra", "yes");
        assertContainsURLParam(uri, "profilometer", "no");
        assertContainsURLParam(uri, "traypics", "yes");
        assertContainsURLParam(uri, "mospic", "no");
        assertContainsURLParam(uri, "mappics", "yes");

        uri = methodMaker.getDownloadTSGMethod(serviceUrl, email, datasetId, null, true, false, true, false, true, false).getURI();
        assertContainsURLParam(uri, "datasetid", datasetId);
        assertContainsURLParam(uri, "email", email);
        assertDoesntContainURLParam(uri, "match_string");
        assertContainsURLParam(uri, "linescan", "yes");
        assertContainsURLParam(uri, "spectra", "no");
        assertContainsURLParam(uri, "profilometer", "yes");
        assertContainsURLParam(uri, "traypics", "no");
        assertContainsURLParam(uri, "mospic", "yes");
        assertContainsURLParam(uri, "mappics", "no");
    }

    @Test
    public void testParamValidity_CheckTSG() throws Exception {
        //Mandatory only
        URI uri = methodMaker.getCheckTSGStatusMethod(serviceUrl, email).getURI();
        assertContainsURLParam(uri, "email", email);
    }

    @Test
    public void testParamValidity_WFSDownload() throws Exception {
        //Mandatory only
        URI uri = methodMaker.getDownloadWFSMethod(serviceUrl, email, "borehole-id", "http://om.service.url", "type:Name").getURI();
        assertContainsURLParam(uri, "email", email);
        assertContainsURLParam(uri, "boreholeid", "borehole-id");
        assertContainsURLParam(uri, "serviceurl", "http://om.service.url");
        assertContainsURLParam(uri, "typename", "type:Name");
    }

    @Test
    public void testParamValidity_CheckWFS() throws Exception {
        //Mandatory only
        URI uri = methodMaker.getCheckWFSStatusMethod(serviceUrl, email).getURI();
        assertContainsURLParam(uri, "email", email);
    }
}

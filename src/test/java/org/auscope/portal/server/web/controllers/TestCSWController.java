package org.auscope.portal.server.web.controllers;

import java.awt.Dimension;
import java.awt.Point;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.auscope.portal.csw.CSWGeographicBoundingBox;
import org.auscope.portal.csw.CSWOnlineResource;
import org.auscope.portal.csw.CSWOnlineResourceImpl;
import org.auscope.portal.csw.CSWRecord;
import org.auscope.portal.csw.CSWOnlineResource.OnlineResourceType;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.web.KnownFeatureTypeDefinition;
import org.auscope.portal.server.web.service.CSWService;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * User: Mathew Wyatt
 * Date: 27/08/2009
 * @version $Id$
 */
public class TestCSWController {

    /**
     * JMock context
     */
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    /**
     * Main object we are testing
     */
    private CSWService cswService = context.mock(CSWService.class);

     /**
     * Mock property configurer
     */
    private PortalPropertyPlaceholderConfigurer propertyConfigurer = context.mock(PortalPropertyPlaceholderConfigurer.class);

    /**
    * Mock KnownTypes arraylist
    */
    @SuppressWarnings("unchecked")
    private ArrayList<KnownFeatureTypeDefinition> knownTypes = context.mock(ArrayList.class);

    /**
     * The controller
     */
    private CSWController cswController;

    private HttpServletRequest mockHttpRequest = context.mock(HttpServletRequest.class);
    private HttpServletResponse mockHttpResponse = context.mock(HttpServletResponse.class);

    @Before
    public void setup() throws Exception {
        final String serviceUrl = "somejunk";

        context.checking(new Expectations() {{
            oneOf(propertyConfigurer).resolvePlaceholder(with(any(String.class)));will(returnValue(serviceUrl));
            oneOf(cswService).setServiceUrl(serviceUrl);
            oneOf(cswService).updateRecordsInBackground();
        }});

        cswController = new CSWController(cswService, propertyConfigurer, knownTypes);
    }

    /**
     * Compares a string JSON response with a parsed equivelant array of objects
     * @param actualJsonResponse An array of array objects of which the first will be tested
     * @param expectedResponse 
     */
    private void compareJSONArrayResponse(String actualJsonResponse, Object[] expectedResponse) {
        JSONArray actualJson = JSONArray.fromObject(actualJsonResponse);
        
        //We return an array of arrays, we only test the first returned array
        Object[] actual = actualJson.getJSONArray(0).toArray();
        
        Assert.assertEquals(expectedResponse.length, actual.length);
        for (int i = 0; i < expectedResponse.length; i++) {
            Assert.assertEquals(expectedResponse[i], actual[i]);
        }
    }
    
    /**
     * Test with valid records
     * @throws Exception
     */
    @Test
    public void testGetComplexFeatures() throws Exception {
        final String orgName = "testOrg";
        final KnownFeatureTypeDefinition def = new KnownFeatureTypeDefinition("0", "1", "2", "3", "4", new Point(1, 2),  new Point(1, 2), new Dimension(3, 4));
        final Iterator<KnownFeatureTypeDefinition> mockIterator = context.mock(Iterator.class);
        final StringWriter actualJSONResponse = new StringWriter();
        final CSWRecord mockRecord = context.mock(CSWRecord.class);
        final String onlineResourceName = "name";
        final CSWOnlineResource mockResource = context.mock(CSWOnlineResource.class);
        final CSWGeographicBoundingBox geographicResponse = new CSWGeographicBoundingBox(1,2,3,4);
        final String serviceUrl = "http://www.service.url";

        context.checking(new Expectations() {{
            oneOf(cswService).updateRecordsInBackground();
            oneOf(knownTypes).iterator();will(returnValue(mockIterator));
            oneOf(mockIterator).hasNext();will(returnValue(true));
            oneOf(mockIterator).next();will(returnValue(def));
            oneOf(cswService).getWFSRecordsForTypename(def.getFeatureTypeName());will(returnValue(new CSWRecord[]{mockRecord}));

            allowing(mockRecord).getOnlineResourcesByType(OnlineResourceType.WFS);will(returnValue(new CSWOnlineResource[] {mockResource}));
            allowing(mockResource).getName();will(returnValue(onlineResourceName));
            allowing(mockResource).getLinkage();will(returnValue(new URL(serviceUrl)));
            
            allowing(mockRecord).getCSWGeographicElement();will(returnValue(geographicResponse));
            allowing(mockRecord).getContactOrganisation();will(returnValue(orgName));

            oneOf(mockIterator).hasNext();will(returnValue(false));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});

        JSONObject expectedIconAnchor = new JSONObject();
        expectedIconAnchor.put("x", 1);
        expectedIconAnchor.put("y", 2);
        JSONObject expectedInfoWindowAnchor = new JSONObject();
        expectedInfoWindowAnchor.put("x", 1);
        expectedInfoWindowAnchor.put("y", 2);        
        JSONObject expectedIconSize = new JSONObject();
        expectedIconSize.put("width", 3);
        expectedIconSize.put("height", 4);
        
        final Object[] expectedJSONResponse = new Object[] {
                "1",
                "2 Institutions: " + orgName + ", ",
                JSONArray.fromObject(new String[] {orgName}),
                "3",
                "wfs",
                def.hashCode(),
                "0",
                JSONArray.fromObject(new String[] {serviceUrl}),
                true,
                "<img src='js/external/extjs/resources/images/default/grid/done.gif'>",
                "<img width='16' heigh='16' src='4'>",
                "4",
                "<a href='http://portal.auscope.org' id='mylink' target='_blank'><img src='img/page_code.png'></a>",
                JSONArray.fromObject(new Object[] {geographicResponse}),
                expectedIconAnchor,
                expectedInfoWindowAnchor,
                expectedIconSize
        };
        
        ModelAndView modelAndView = cswController.getComplexFeatures();

        //check that our JSON response has been nicely populated
        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        compareJSONArrayResponse(actualJSONResponse.getBuffer().toString(), expectedJSONResponse);
    }

    /**
     * Test for when there are no services for a given feature type
     * @throws Exception
     */
    @Test
    public void testGetComplexFeaturesNoServices() throws Exception {
        final KnownFeatureTypeDefinition def = new KnownFeatureTypeDefinition("0", "1", "2", "3", "4");
        @SuppressWarnings("unchecked")
        final Iterator<KnownFeatureTypeDefinition> mockIterator = context.mock(Iterator.class);
        final StringWriter actualJSONResponse = new StringWriter();

        context.checking(new Expectations() {{
            oneOf(cswService).updateRecordsInBackground();
            oneOf(knownTypes).iterator();will(returnValue(mockIterator));
            oneOf(mockIterator).hasNext();will(returnValue(true));
            oneOf(mockIterator).next();will(returnValue(def));
            oneOf(cswService).getWFSRecordsForTypename(def.getFeatureTypeName());will(returnValue(new CSWRecord[]{}));

            oneOf(mockIterator).hasNext();will(returnValue(false));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});

        ModelAndView modelAndView = cswController.getComplexFeatures();

        //check that our JSON response has been nicely populated
        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        Assert.assertEquals("[]", actualJSONResponse.getBuffer().toString());
    }

    /**
     * Test that the JSON is formatted correctly for WMS layers
     */
    @Test
    public void testGetWMSLayers() throws Exception {
        final String orgName = "testOrg";
        final CSWRecord mockRecord = context.mock(CSWRecord.class);
        final CSWOnlineResource mockResource = context.mock(CSWOnlineResource.class);
        final StringWriter actualJSONResponse = new StringWriter();
        final String serviceName = "foobar";
        final String dataAbstract = "harharh";
        final String name = "resName";
        final String serviceUrl = "http://fake.com";
        final CSWGeographicBoundingBox geographicResponse = new CSWGeographicBoundingBox(4,2,3,8);

        context.checking(new Expectations() {{
            oneOf(cswService).updateRecordsInBackground();
            oneOf(cswService).getWMSRecords();will(returnValue(new CSWRecord[]{mockRecord}));

            allowing(mockRecord).getServiceName();will(returnValue(serviceName));
            allowing(mockRecord).getDataIdentificationAbstract();will(returnValue(dataAbstract));
            allowing(mockRecord).getOnlineResourcesByType(OnlineResourceType.WMS);will(returnValue(new CSWOnlineResource[] {mockResource}));
            allowing(mockResource).getName();will(returnValue(name));
            allowing(mockResource).getLinkage();will(returnValue(new URL(serviceUrl)));
            allowing(mockRecord).getContactOrganisation();will(returnValue(orgName));
            allowing(mockRecord).getCSWGeographicElement();will(returnValue(geographicResponse));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});
        
        final Object[] expectedJSONResponse = new Object[] {
                serviceName,
                dataAbstract,
                orgName,
                "",
                "wms",
                mockRecord.hashCode(),
                name,
                JSONArray.fromObject(new String[] {serviceUrl}),
                true,
                "<img src='js/external/extjs/resources/images/default/grid/done.gif'>",
                "<a href='http://portal.auscope.org' id='mylink' target='_blank'><img src='img/picture_link.png'></a>",
                "1.0",
                JSONArray.fromObject(new Object[] {geographicResponse})
        };
        
        ModelAndView modelAndView = cswController.getWMSLayers();

        //check that our JSON response has been nicely populated
        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        compareJSONArrayResponse(actualJSONResponse.getBuffer().toString(), expectedJSONResponse);
    }
    
    
    /**
     * Test that the JSON is formatted correctly for WMS layers
     */
    @Test
    public void testGetWCSLayers() throws Exception {
        final String orgName = "tesatOrg";
        final CSWRecord mockRecord = context.mock(CSWRecord.class);
        final CSWOnlineResource mockResource = context.mock(CSWOnlineResource.class);
        final StringWriter actualJSONResponse = new StringWriter();
        final String serviceName = "foo2bar";
        final String dataAbstract = "ha4rharh";
        final String name = "resN4ame";
        final String serviceUrl = "http://fake2.com";
        final CSWOnlineResource mockOpenDapResource = context.mock(CSWOnlineResourceImpl.class, "openDapResource");
        final CSWOnlineResource mockWmsResource = context.mock(CSWOnlineResourceImpl.class, "wmsResource");
        final CSWGeographicBoundingBox geographicResponse = new CSWGeographicBoundingBox(6.2,3.4,1.0,8);
        

        context.checking(new Expectations() {{
            oneOf(cswService).updateRecordsInBackground();
            oneOf(cswService).getWCSRecords();will(returnValue(new CSWRecord[]{mockRecord}));
            
            allowing(mockOpenDapResource).getDescription();will(returnValue("od-desc"));
            allowing(mockOpenDapResource).getLinkage();will(returnValue(new URL("http://opendap.com")));
            allowing(mockOpenDapResource).getName();will(returnValue("od-name"));
            allowing(mockOpenDapResource).getProtocol();will(returnValue("od-protocol"));
            allowing(mockOpenDapResource).getType();will(returnValue(OnlineResourceType.OpenDAP));
            
            allowing(mockWmsResource).getDescription();will(returnValue("wms-desc"));
            allowing(mockWmsResource).getLinkage();will(returnValue(new URL("http://wms.com")));
            allowing(mockWmsResource).getName();will(returnValue("wms-name"));
            allowing(mockWmsResource).getProtocol();will(returnValue("wms-protocol"));
            allowing(mockWmsResource).getType();will(returnValue(OnlineResourceType.WMS));

            allowing(mockRecord).getServiceName();will(returnValue(serviceName));
            allowing(mockRecord).getDataIdentificationAbstract();will(returnValue(dataAbstract));
            allowing(mockRecord).getOnlineResourcesByType(OnlineResourceType.WCS);will(returnValue(new CSWOnlineResource[] {mockResource}));
            allowing(mockRecord).getOnlineResourcesByType(OnlineResourceType.OpenDAP);will(returnValue(new CSWOnlineResource[] {mockOpenDapResource}));
            allowing(mockRecord).getOnlineResourcesByType(OnlineResourceType.WMS);will(returnValue(new CSWOnlineResource[] {mockWmsResource}));
            allowing(mockResource).getName();will(returnValue(name));
            allowing(mockResource).getLinkage();will(returnValue(new URL(serviceUrl)));
            allowing(mockRecord).getContactOrganisation();will(returnValue(orgName));
            allowing(mockRecord).getCSWGeographicElement();will(returnValue(geographicResponse));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});
        
        JSONObject jsonOpenDapResource = new JSONObject();
        jsonOpenDapResource.put("url", "http://opendap.com");
        jsonOpenDapResource.put("name", "od-name");
        jsonOpenDapResource.put("onlineResourceType", OnlineResourceType.OpenDAP.name());
        jsonOpenDapResource.put("description", "od-desc");
        
        JSONObject jsonWMSResource = new JSONObject();
        jsonWMSResource.put("url", "http://wms.com");
        jsonWMSResource.put("name", "wms-name");
        jsonWMSResource.put("onlineResourceType", OnlineResourceType.WMS.name());
        jsonWMSResource.put("description", "wms-desc");
        
        final Object[] expectedJSONResponse = new Object[] {
                serviceName,
                dataAbstract,
                orgName,
                "",
                "wcs",
                mockRecord.hashCode(),
                name,
                JSONArray.fromObject(new String[] {serviceUrl}),
                JSONArray.fromObject(jsonOpenDapResource),
                JSONArray.fromObject(jsonWMSResource),
                1,
                true,
                "<img src='js/external/extjs/resources/images/default/grid/done.gif'>",
                "<a href='http://portal.auscope.org' id='mylink' target='_blank'><img src='img/picture_link.png'></a>",
                JSONArray.fromObject(new Object[] {geographicResponse})
        };
        
        ModelAndView modelAndView = cswController.getWCSLayers();

        //check that our JSON response has been nicely populated
        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        compareJSONArrayResponse(actualJSONResponse.getBuffer().toString(), expectedJSONResponse);
    }

    

}

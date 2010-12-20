package org.auscope.portal.server.web.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.auscope.portal.csw.CSWGeographicBoundingBox;
import org.auscope.portal.csw.CSWGeographicElement;
import org.auscope.portal.csw.CSWOnlineResource;
import org.auscope.portal.csw.CSWOnlineResource.OnlineResourceType;
import org.auscope.portal.csw.CSWRecord;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.springframework.ui.ModelMap;

public class TestViewCSWRecordFactory {
	private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    private CSWRecord mockCSWRecord = context.mock(CSWRecord.class);
    private CSWOnlineResource mockOnlineRes = context.mock(CSWOnlineResource.class);
    private CSWGeographicBoundingBox mockBbox = context.mock(CSWGeographicBoundingBox.class);

    @Test
    public void testToView() throws Exception {
    	ViewCSWRecordFactory factory = new ViewCSWRecordFactory();

    	final String serviceName = "sn";
    	final String contactOrg = "co";
    	final String resourceProvider = "MDU";
    	final String fileId = "asb";
    	final String recordInfoUrl = "http://bob.xom";
    	final String dataAbstract = "assda";
    	final String descriptiveKeyword1 = "kw1";
    	final String descriptiveKeyword2 = "kw1";
    	final String constraint1 = "c1";
    	final String constraint2 = "c2";

    	final URL orUrl = new URL("http://hah.com");
    	final String orName = "ascom";
    	final String orDesc = "desc";
    	final OnlineResourceType orType = OnlineResourceType.WFS;

    	final double bboxNorth = 10;
    	final double bboxSouth = 5;
    	final double bboxEast = 7;
    	final double bboxWest = 6;

    	final ModelMap expectation = new ModelMap();
    	final ModelMap onlineResExpectation = new ModelMap();
    	final ModelMap geoExpectation = new ModelMap();

    	expectation.put("serviceName", serviceName);
    	expectation.put("contactOrganisation", contactOrg);
    	expectation.put("resourceProvider", resourceProvider);
    	expectation.put("fileIdentifier", fileId);
    	expectation.put("recordInfoUrl", recordInfoUrl);
    	expectation.put("dataIdentificationAbstract", dataAbstract);
    	expectation.put("onlineResources", Arrays.asList(onlineResExpectation));
    	expectation.put("geographicElements", Arrays.asList(geoExpectation));
    	expectation.put("geographicElements", Arrays.asList(geoExpectation));
    	expectation.put("descriptiveKeywords", Arrays.asList(descriptiveKeyword1, descriptiveKeyword2));
    	expectation.put("constraints", Arrays.asList(constraint1, constraint2));

    	onlineResExpectation.put("url", orUrl.toString());
    	onlineResExpectation.put("name", orName);
    	onlineResExpectation.put("description", orDesc);
    	onlineResExpectation.put("onlineResourceType", orType.name());

    	geoExpectation.put("type", "bbox");
    	geoExpectation.put("eastBoundLongitude", bboxEast);
    	geoExpectation.put("westBoundLongitude", bboxWest);
    	geoExpectation.put("northBoundLatitude", bboxNorth);
    	geoExpectation.put("southBoundLatitude", bboxSouth);

    	context.checking(new Expectations() {{
    		allowing(mockCSWRecord).getServiceName();will(returnValue(serviceName));
    		allowing(mockCSWRecord).getContactOrganisation();will(returnValue(contactOrg));
    		allowing(mockCSWRecord).getResourceProvider();will(returnValue(resourceProvider));
    		allowing(mockCSWRecord).getFileIdentifier();will(returnValue(fileId));
    		allowing(mockCSWRecord).getRecordInfoUrl();will(returnValue(recordInfoUrl));
    		allowing(mockCSWRecord).getDataIdentificationAbstract();will(returnValue(dataAbstract));
    		allowing(mockCSWRecord).getOnlineResources();will(returnValue(new CSWOnlineResource[] {mockOnlineRes}));
    		allowing(mockCSWRecord).getCSWGeographicElements();will(returnValue(new CSWGeographicElement[] {mockBbox}));
    		allowing(mockCSWRecord).getDescriptiveKeywords();will(returnValue(new String[] {descriptiveKeyword1, descriptiveKeyword2}));
    		allowing(mockCSWRecord).getConstraints();will(returnValue(new String[] {constraint1, constraint2}));
    		
    		allowing(mockBbox).getEastBoundLongitude();will(returnValue(bboxEast));
    		allowing(mockBbox).getWestBoundLongitude();will(returnValue(bboxWest));
    		allowing(mockBbox).getNorthBoundLatitude();will(returnValue(bboxNorth));
    		allowing(mockBbox).getSouthBoundLatitude();will(returnValue(bboxSouth));

    		allowing(mockOnlineRes).getDescription();will(returnValue(orDesc));
    		allowing(mockOnlineRes).getName();will(returnValue(orName));
    		allowing(mockOnlineRes).getType();will(returnValue(orType));
    		allowing(mockOnlineRes).getLinkage();will(returnValue(orUrl));
        }});

    	ModelMap result = factory.toView(mockCSWRecord);

    	AssertViewUtility.assertModelMapsEqual(expectation,result);
    }

    @Test
    public void testToViewBadOnlineResources() throws Exception {
        ViewCSWRecordFactory factory = new ViewCSWRecordFactory();

        final String serviceName = "sn";
        final String contactOrg = "co";
        final String resourceProvider = "MDU";
        final String fileId = "asb";
        final String recordInfoUrl = "http://bob.xom";
        final String dataAbstract = "assda";
        final String descriptiveKeyword1 = "kw1";
        final String descriptiveKeyword2 = "kw1";
    	final String constraint1 = "c1";
    	final String constraint2 = "c2";

        final URL orUrl = null;
        final String orName = "ascom";
        final String orDesc = "desc";
        final OnlineResourceType orType = OnlineResourceType.WFS;

        final double bboxNorth = 10;
        final double bboxSouth = 5;
        final double bboxEast = 7;
        final double bboxWest = 6;

        final ModelMap expectation = new ModelMap();
        final ModelMap geoExpectation = new ModelMap();

        expectation.put("serviceName", serviceName);
        expectation.put("contactOrganisation", contactOrg);
        expectation.put("resourceProvider", resourceProvider);
        expectation.put("fileIdentifier", fileId);
        expectation.put("recordInfoUrl", recordInfoUrl);
        expectation.put("dataIdentificationAbstract", dataAbstract);
        expectation.put("onlineResources", new ArrayList<ModelMap>());
        expectation.put("geographicElements", Arrays.asList(geoExpectation));
        expectation.put("descriptiveKeywords", Arrays.asList(descriptiveKeyword1, descriptiveKeyword2));
    	expectation.put("constraints", Arrays.asList(constraint1, constraint2));
    	
        geoExpectation.put("type", "bbox");
        geoExpectation.put("eastBoundLongitude", bboxEast);
        geoExpectation.put("westBoundLongitude", bboxWest);
        geoExpectation.put("northBoundLatitude", bboxNorth);
        geoExpectation.put("southBoundLatitude", bboxSouth);

        context.checking(new Expectations() {{
            allowing(mockCSWRecord).getServiceName();will(returnValue(serviceName));
            allowing(mockCSWRecord).getContactOrganisation();will(returnValue(contactOrg));
            allowing(mockCSWRecord).getResourceProvider();will(returnValue(resourceProvider));
            allowing(mockCSWRecord).getFileIdentifier();will(returnValue(fileId));
            allowing(mockCSWRecord).getRecordInfoUrl();will(returnValue(recordInfoUrl));
            allowing(mockCSWRecord).getDataIdentificationAbstract();will(returnValue(dataAbstract));
            allowing(mockCSWRecord).getOnlineResources();will(returnValue(new CSWOnlineResource[] {mockOnlineRes}));
            allowing(mockCSWRecord).getCSWGeographicElements();will(returnValue(new CSWGeographicElement[] {mockBbox}));
            allowing(mockCSWRecord).getDescriptiveKeywords();will(returnValue(new String[] {descriptiveKeyword1, descriptiveKeyword2}));
    		allowing(mockCSWRecord).getConstraints();will(returnValue(new String[] {constraint1, constraint2}));
            
            allowing(mockBbox).getEastBoundLongitude();will(returnValue(bboxEast));
            allowing(mockBbox).getWestBoundLongitude();will(returnValue(bboxWest));
            allowing(mockBbox).getNorthBoundLatitude();will(returnValue(bboxNorth));
            allowing(mockBbox).getSouthBoundLatitude();will(returnValue(bboxSouth));

            allowing(mockOnlineRes).getDescription();will(returnValue(orDesc));
            allowing(mockOnlineRes).getName();will(returnValue(orName));
            allowing(mockOnlineRes).getType();will(returnValue(orType));
            allowing(mockOnlineRes).getLinkage();will(returnValue(orUrl));
        }});

        ModelMap result = factory.toView(mockCSWRecord);

        AssertViewUtility.assertModelMapsEqual(expectation,result);
    }
}

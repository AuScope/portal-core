package org.auscope.portal.core.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource.OnlineResourceType;
import org.auscope.portal.core.services.responses.csw.CSWContact;
import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;
import org.auscope.portal.core.services.responses.csw.CSWGeographicElement;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.csw.CSWResponsibleParty;
import org.auscope.portal.core.test.AssertViewUtility;
import org.auscope.portal.core.test.PortalTestClass;
import org.jmock.Expectations;
import org.junit.Test;
import org.springframework.ui.ModelMap;

public class TestViewCSWRecordFactory extends PortalTestClass {
    private CSWRecord mockCSWRecord = context.mock(CSWRecord.class);
    private CSWRecord mockCSWChildRecord1 = context.mock(CSWRecord.class, "childRecord1");
    private AbstractCSWOnlineResource mockOnlineRes = context.mock(AbstractCSWOnlineResource.class);
    private AbstractCSWOnlineResource mockOnlineRes_1 = context
            .mock(AbstractCSWOnlineResource.class, "mockOnlineRes_1");
    private CSWGeographicBoundingBox mockBbox = context.mock(CSWGeographicBoundingBox.class);
    private CSWResponsibleParty mockResponsibleParty = context.mock(CSWResponsibleParty.class);
    private CSWContact mockContact = context.mock(CSWContact.class);

    @Test
    public void testToView() throws Exception {
        ViewCSWRecordFactory factory = new ViewCSWRecordFactory();

        //for mockCSWRecord
        final String serviceName = "sn";
        final String administrativeArea = "CSIRO";
        final String contactOrg = "co";
        final String resourceProvider = "MDU";
        final String fileId = "asb";
        final String recordInfoUrl = "http://bob.xom";
        final String dataAbstract = "assda";
        final String descriptiveKeyword1 = "kw1";
        final String descriptiveKeyword2 = "kw1";
        final String constraint1 = "c1";
        final String constraint2 = "c2";
        final String version = "1.1.1";

        final URL orUrl = new URL("http://hah.com");
        final String orName = "ascom";
        final String orDesc = "desc";
        final OnlineResourceType orType = OnlineResourceType.WFS;

        final double bboxNorth = 10;
        final double bboxSouth = 5;
        final double bboxEast = 7;
        final double bboxWest = 6;

        //The ModelMap expectation objects for a parent CSWRecord object (mockCSWRecord)
        final ModelMap expectation = new ModelMap();
        final ModelMap onlineResExpectation = new ModelMap();
        final ModelMap geoExpectation = new ModelMap();
        final ModelMap childRecordExpectation = new ModelMap();

        expectation.put("name", serviceName);
        expectation.put("adminArea", administrativeArea);
        expectation.put("contactOrg", contactOrg);
        expectation.put("resourceProvider", resourceProvider);
        expectation.put("id", fileId);
        expectation.put("recordInfoUrl", recordInfoUrl);
        expectation.put("description", dataAbstract);
        expectation.put("onlineResources", Arrays.asList(onlineResExpectation));
        expectation.put("geographicElements", Arrays.asList(geoExpectation));
        expectation.put("descriptiveKeywords", Arrays.asList(descriptiveKeyword1, descriptiveKeyword2));
        expectation.put("constraints", Arrays.asList(constraint1, constraint2));
        expectation.put("childRecords", Arrays.asList(childRecordExpectation));
        expectation.put("noCache", false);
        expectation.put("date", "1970-01-01T00:00:00 UTC");

        onlineResExpectation.put("url", orUrl.toString());
        onlineResExpectation.put("name", orName);
        onlineResExpectation.put("description", orDesc);
        onlineResExpectation.put("type", orType.name());
        onlineResExpectation.put("version", version);

        geoExpectation.put("type", "bbox");
        geoExpectation.put("eastBoundLongitude", bboxEast);
        geoExpectation.put("westBoundLongitude", bboxWest);
        geoExpectation.put("northBoundLatitude", bboxNorth);
        geoExpectation.put("southBoundLatitude", bboxSouth);

        //for mockCSWChildRecord1
        final String serviceName_1 = "sn_1";
        final String contactOrg_1 = "Unknown";
        final String resourceProvider_1 = "CESRE";
        final String fileId_1 = "asb_1";
        final String recordInfoUrl_1 = "http://bob.xom";
        final String dataAbstract_1 = "assda_1";
        final String descriptiveKeyword1_1 = "kw1_1";
        final String descriptiveKeyword2_1 = "kw2_1";
        final String constraint1_1 = "c1_1";
        final String constraint2_1 = "c2_1";

        final URL orUrl_1 = new URL("http://hah_1.com");
        final String orName_1 = "ascom_1";
        final String orDesc_1 = "desc_1";
        final OnlineResourceType orType_1 = OnlineResourceType.WCS;

        //The ModelMap expectation objects for the child of mockCSWRecord object (mockCSWChildRecord1)
        final ModelMap onlineResExpectation_1 = new ModelMap();

        childRecordExpectation.put("name", serviceName_1);
        childRecordExpectation.put("adminArea", null);
        childRecordExpectation.put("contactOrg", contactOrg_1);
        childRecordExpectation.put("resourceProvider", resourceProvider_1);
        childRecordExpectation.put("id", fileId_1);
        childRecordExpectation.put("recordInfoUrl", recordInfoUrl_1);
        childRecordExpectation.put("description", dataAbstract_1);
        childRecordExpectation.put("onlineResources", Arrays.asList(onlineResExpectation_1));
        childRecordExpectation.put("geographicElements", Arrays.asList());
        childRecordExpectation.put("descriptiveKeywords", Arrays.asList(descriptiveKeyword1_1, descriptiveKeyword2_1));
        childRecordExpectation.put("constraints", Arrays.asList(constraint1_1, constraint2_1));
        childRecordExpectation.put("childRecords", Arrays.asList());
        childRecordExpectation.put("noCache", false);
        childRecordExpectation.put("date", "");

        onlineResExpectation_1.put("url", orUrl_1.toString());
        onlineResExpectation_1.put("name", orName_1);
        onlineResExpectation_1.put("description", orDesc_1);
        onlineResExpectation_1.put("type", orType_1.name());
        onlineResExpectation_1.put("version", version);

        context.checking(new Expectations() {
            {
                allowing(mockCSWRecord).getServiceName();
                will(returnValue(serviceName));
                allowing(mockCSWRecord).getNoCache();
                will(returnValue(false));
                allowing(mockCSWRecord).getContact();
                will(returnValue(mockResponsibleParty));
                allowing(mockCSWRecord).getResourceProvider();
                will(returnValue(resourceProvider));
                allowing(mockCSWRecord).getFileIdentifier();
                will(returnValue(fileId));
                allowing(mockCSWRecord).getRecordInfoUrl();
                will(returnValue(recordInfoUrl));
                allowing(mockCSWRecord).getDataIdentificationAbstract();
                will(returnValue(dataAbstract));
                allowing(mockCSWRecord).getOnlineResources();
                will(returnValue(new AbstractCSWOnlineResource[] {mockOnlineRes}));
                allowing(mockCSWRecord).getCSWGeographicElements();
                will(returnValue(new CSWGeographicElement[] {mockBbox}));
                allowing(mockCSWRecord).getDescriptiveKeywords();
                will(returnValue(new String[] {descriptiveKeyword1, descriptiveKeyword2}));
                allowing(mockCSWRecord).getConstraints();
                will(returnValue(new String[] {constraint1, constraint2}));
                allowing(mockCSWRecord).hasChildRecords();
                will(returnValue(true));
                allowing(mockCSWRecord).getChildRecords();
                will(returnValue(new CSWRecord[] {mockCSWChildRecord1}));
                allowing(mockCSWRecord).getDate();
                will(returnValue(new Date(0L)));

                allowing(mockCSWChildRecord1).getServiceName();
                will(returnValue(serviceName_1));
                allowing(mockCSWChildRecord1).getNoCache();
                will(returnValue(false));
                allowing(mockCSWChildRecord1).getContact();
                will(returnValue(null));
                allowing(mockCSWChildRecord1).getResourceProvider();
                will(returnValue(resourceProvider_1));
                allowing(mockCSWChildRecord1).getFileIdentifier();
                will(returnValue(fileId_1));
                allowing(mockCSWChildRecord1).getRecordInfoUrl();
                will(returnValue(recordInfoUrl_1));
                allowing(mockCSWChildRecord1).getDataIdentificationAbstract();
                will(returnValue(dataAbstract_1));
                allowing(mockCSWChildRecord1).getOnlineResources();
                will(returnValue(new AbstractCSWOnlineResource[] {mockOnlineRes_1}));
                allowing(mockCSWChildRecord1).getCSWGeographicElements();
                will(returnValue(null));
                allowing(mockCSWChildRecord1).getDescriptiveKeywords();
                will(returnValue(new String[] {descriptiveKeyword1_1, descriptiveKeyword2_1}));
                allowing(mockCSWChildRecord1).getConstraints();
                will(returnValue(new String[] {constraint1_1, constraint2_1}));
                allowing(mockCSWChildRecord1).hasChildRecords();
                will(returnValue(false));
                allowing(mockCSWChildRecord1).getDate();
                will(returnValue(null));

                allowing(mockBbox).getEastBoundLongitude();
                will(returnValue(bboxEast));
                allowing(mockBbox).getWestBoundLongitude();
                will(returnValue(bboxWest));
                allowing(mockBbox).getNorthBoundLatitude();
                will(returnValue(bboxNorth));
                allowing(mockBbox).getSouthBoundLatitude();
                will(returnValue(bboxSouth));

                allowing(mockOnlineRes).getDescription();
                will(returnValue(orDesc));
                allowing(mockOnlineRes).getName();
                will(returnValue(orName));
                allowing(mockOnlineRes).getType();
                will(returnValue(orType));
                allowing(mockOnlineRes).getLinkage();
                will(returnValue(orUrl));
                allowing(mockOnlineRes).getVersion();
                will(returnValue(version));

                allowing(mockOnlineRes_1).getDescription();
                will(returnValue(orDesc_1));
                allowing(mockOnlineRes_1).getName();
                will(returnValue(orName_1));
                allowing(mockOnlineRes_1).getType();
                will(returnValue(orType_1));
                allowing(mockOnlineRes_1).getLinkage();
                will(returnValue(orUrl_1));
                allowing(mockOnlineRes_1).getVersion();
                will(returnValue(version));

                allowing(mockResponsibleParty).getOrganisationName();
                will(returnValue(contactOrg));
                allowing(mockResponsibleParty).getContactInfo();
                will(returnValue(mockContact));
                oneOf(mockContact).getAddressAdministrativeArea();
                will(returnValue(administrativeArea));
            }
        });

        ModelMap result = factory.toView(mockCSWRecord);

        AssertViewUtility.assertModelMapsEqual(expectation, result);
    }

    @Test
    public void testToViewBadOnlineResources() throws Exception {
        ViewCSWRecordFactory factory = new ViewCSWRecordFactory();

        final String serviceName = "sn";
        final String administrativeArea = "CSIRO";
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

        expectation.put("name", serviceName);
        expectation.put("adminArea", administrativeArea);
        expectation.put("contactOrg", contactOrg);
        expectation.put("resourceProvider", resourceProvider);
        expectation.put("id", fileId);
        expectation.put("recordInfoUrl", recordInfoUrl);
        expectation.put("description", dataAbstract);
        expectation.put("onlineResources", new ArrayList<ModelMap>());
        expectation.put("geographicElements", Arrays.asList(geoExpectation));
        expectation.put("descriptiveKeywords", Arrays.asList(descriptiveKeyword1, descriptiveKeyword2));
        expectation.put("constraints", Arrays.asList(constraint1, constraint2));
        expectation.put("childRecords", new ArrayList<ModelMap>());
        expectation.put("noCache", false);
        expectation.put("date", "1970-01-01T00:00:00 UTC");

        geoExpectation.put("type", "bbox");
        geoExpectation.put("eastBoundLongitude", bboxEast);
        geoExpectation.put("westBoundLongitude", bboxWest);
        geoExpectation.put("northBoundLatitude", bboxNorth);
        geoExpectation.put("southBoundLatitude", bboxSouth);

        context.checking(new Expectations() {
            {
                allowing(mockCSWRecord).getServiceName();
                will(returnValue(serviceName));
                allowing(mockCSWRecord).getNoCache();
                will(returnValue(false));
                allowing(mockCSWRecord).getContact();
                will(returnValue(mockResponsibleParty));
                allowing(mockCSWRecord).getResourceProvider();
                will(returnValue(resourceProvider));
                allowing(mockCSWRecord).getFileIdentifier();
                will(returnValue(fileId));
                allowing(mockCSWRecord).getRecordInfoUrl();
                will(returnValue(recordInfoUrl));
                allowing(mockCSWRecord).getDataIdentificationAbstract();
                will(returnValue(dataAbstract));
                allowing(mockCSWRecord).getOnlineResources();
                will(returnValue(new AbstractCSWOnlineResource[] {mockOnlineRes}));
                allowing(mockCSWRecord).getCSWGeographicElements();
                will(returnValue(new CSWGeographicElement[] {mockBbox}));
                allowing(mockCSWRecord).getDescriptiveKeywords();
                will(returnValue(new String[] {descriptiveKeyword1, descriptiveKeyword2}));
                allowing(mockCSWRecord).getConstraints();
                will(returnValue(new String[] {constraint1, constraint2}));
                allowing(mockCSWRecord).hasChildRecords();
                will(returnValue(false));
                allowing(mockCSWRecord).getDate();
                will(returnValue(new Date(0L)));

                allowing(mockBbox).getEastBoundLongitude();
                will(returnValue(bboxEast));
                allowing(mockBbox).getWestBoundLongitude();
                will(returnValue(bboxWest));
                allowing(mockBbox).getNorthBoundLatitude();
                will(returnValue(bboxNorth));
                allowing(mockBbox).getSouthBoundLatitude();
                will(returnValue(bboxSouth));

                allowing(mockOnlineRes).getDescription();
                will(returnValue(orDesc));
                allowing(mockOnlineRes).getName();
                will(returnValue(orName));
                allowing(mockOnlineRes).getType();
                will(returnValue(orType));
                allowing(mockOnlineRes).getLinkage();
                will(returnValue(orUrl));

                allowing(mockResponsibleParty).getOrganisationName();
                will(returnValue(contactOrg));
                allowing(mockResponsibleParty).getContactInfo();
                will(returnValue(mockContact));
                oneOf(mockContact).getAddressAdministrativeArea();
                will(returnValue(administrativeArea));
            }
        });

        ModelMap result = factory.toView(mockCSWRecord);

        AssertViewUtility.assertModelMapsEqual(expectation, result);
    }

    /**
     * Tests that the view correctly handles a null responsible party contact.
     *
     * @throws Exception
     */
    @Test
    public void testToViewNoResponsibleParty() throws Exception {
        ViewCSWRecordFactory factory = new ViewCSWRecordFactory();

        final String serviceName = "sn";
        final String contactOrg = ""; //this should be the empty string
        final String resourceProvider = "MDU";
        final String fileId = "asb";
        final String recordInfoUrl = "http://bob.xom";
        final String dataAbstract = "assda";
        final String descriptiveKeyword1 = "kw1";
        final String descriptiveKeyword2 = "kw1";
        final String constraint1 = "c1";
        final String constraint2 = "c2";

        final URL orUrl = new URL("http://hah.com");
        final String version = "1.3.0";
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

        expectation.put("name", serviceName);
        expectation.put("adminArea", null);
        expectation.put("contactOrg", "Unknown");
        expectation.put("resourceProvider", resourceProvider);
        expectation.put("id", fileId);
        expectation.put("recordInfoUrl", recordInfoUrl);
        expectation.put("description", dataAbstract);
        expectation.put("onlineResources", Arrays.asList(onlineResExpectation));
        expectation.put("geographicElements", Arrays.asList(geoExpectation));
        expectation.put("descriptiveKeywords", Arrays.asList(descriptiveKeyword1, descriptiveKeyword2));
        expectation.put("constraints", Arrays.asList(constraint1, constraint2));
        expectation.put("childRecords", new ArrayList<ModelMap>());
        expectation.put("noCache", false);
        expectation.put("date", "1986-10-09T00:00:00 UTC");

        onlineResExpectation.put("url", orUrl.toString());
        onlineResExpectation.put("name", orName);
        onlineResExpectation.put("description", orDesc);
        onlineResExpectation.put("type", orType.name());
        onlineResExpectation.put("version", version);

        geoExpectation.put("type", "bbox");
        geoExpectation.put("eastBoundLongitude", bboxEast);
        geoExpectation.put("westBoundLongitude", bboxWest);
        geoExpectation.put("northBoundLatitude", bboxNorth);
        geoExpectation.put("southBoundLatitude", bboxSouth);

        context.checking(new Expectations() {
            {
                allowing(mockCSWRecord).getServiceName();
                will(returnValue(serviceName));
                allowing(mockCSWRecord).getNoCache();
                will(returnValue(false));
                allowing(mockCSWRecord).getContact();
                will(returnValue(null));
                allowing(mockCSWRecord).getResourceProvider();
                will(returnValue(resourceProvider));
                allowing(mockCSWRecord).getFileIdentifier();
                will(returnValue(fileId));
                allowing(mockCSWRecord).getRecordInfoUrl();
                will(returnValue(recordInfoUrl));
                allowing(mockCSWRecord).getDataIdentificationAbstract();
                will(returnValue(dataAbstract));
                allowing(mockCSWRecord).getOnlineResources();
                will(returnValue(new AbstractCSWOnlineResource[] {mockOnlineRes}));
                allowing(mockCSWRecord).getCSWGeographicElements();
                will(returnValue(new CSWGeographicElement[] {mockBbox}));
                allowing(mockCSWRecord).getDescriptiveKeywords();
                will(returnValue(new String[] {descriptiveKeyword1, descriptiveKeyword2}));
                allowing(mockCSWRecord).getConstraints();
                will(returnValue(new String[] {constraint1, constraint2}));
                allowing(mockCSWRecord).hasChildRecords();
                will(returnValue(false));
                allowing(mockCSWRecord).getDate();
                will(returnValue(new Date(529200000000L)));

                allowing(mockBbox).getEastBoundLongitude();
                will(returnValue(bboxEast));
                allowing(mockBbox).getWestBoundLongitude();
                will(returnValue(bboxWest));
                allowing(mockBbox).getNorthBoundLatitude();
                will(returnValue(bboxNorth));
                allowing(mockBbox).getSouthBoundLatitude();
                will(returnValue(bboxSouth));

                allowing(mockOnlineRes).getDescription();
                will(returnValue(orDesc));
                allowing(mockOnlineRes).getName();
                will(returnValue(orName));
                allowing(mockOnlineRes).getType();
                will(returnValue(orType));
                allowing(mockOnlineRes).getLinkage();
                will(returnValue(orUrl));
                allowing(mockOnlineRes).getVersion();
                will(returnValue(version));

                allowing(mockResponsibleParty).getOrganisationName();
                will(returnValue(contactOrg));
            }
        });

        ModelMap result = factory.toView(mockCSWRecord);

        AssertViewUtility.assertModelMapsEqual(expectation, result);
    }
}

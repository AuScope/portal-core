package org.auscope.portal.core.services.responses.csw;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.auscope.portal.core.server.OgcServiceProviderType;
import org.auscope.portal.core.services.namespaces.CSWNamespaceContext;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource.OnlineResourceType;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.DOMUtil;
import org.auscope.portal.core.util.ResourceUtil;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TestCSWRecordTransformer extends PortalTestClass {
    private CSWRecord[] records;
    private Document doc;

    private XPathExpression exprGetAllMetadataNodes;

    private void setUpForResponse(final String responseResourceName) throws ParserConfigurationException, SAXException,
    IOException, XPathException {

        final CSWNamespaceContext nc = new CSWNamespaceContext();
        exprGetAllMetadataNodes = DOMUtil.compileXPathExpr("/csw:GetRecordsResponse/csw:SearchResults/gmd:MD_Metadata",
                nc);

        // load CSW record response document
        doc = DOMUtil.buildDomFromStream(ResourceUtil.loadResourceAsStream(responseResourceName));

        final XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new CSWNamespaceContext());

        final NodeList nodes = (NodeList) exprGetAllMetadataNodes.evaluate(doc, XPathConstants.NODESET);

        records = new CSWRecord[nodes.getLength()];
        for (int i = 0; i < nodes.getLength(); i++) {
            final Node metadataNode = nodes.item(i);
            final CSWRecordTransformer transformer = new CSWRecordTransformer(metadataNode, OgcServiceProviderType.Default);
            records[i] = transformer.transformToCSWRecord();
        }
    }

    @Test
    public void testGetServiceName() throws XPathException, ParserConfigurationException, SAXException,
    IOException {
        setUpForResponse("org/auscope/portal/core/test/responses/csw/cswRecordResponse.xml");

        Assert.assertEquals(
                "GSV GeologicUnit WFS",
                this.records[0].getServiceName());

        Assert.assertEquals(
                "PIRSA EarthResource GeoServer WFS",
                this.records[2].getServiceName());
    }

    @Test
    public void testGetServiceUrl() throws XPathException, ParserConfigurationException, SAXException, IOException  {
        setUpForResponse("org/auscope/portal/core/test/responses/csw/cswRecordResponse.xml");

        List<AbstractCSWOnlineResource> resources = this.records[4].getOnlineResourcesByType(OnlineResourceType.WFS);
        Assert.assertEquals(1, resources.size());
        Assert.assertEquals(
                "http://auscope-services-test.arrc.csiro.au/deegree-wfs/services?",
                resources.get(0).getLinkage().toString());

        resources = this.records[7].getOnlineResourcesByType(OnlineResourceType.WFS);
        Assert.assertEquals(1, resources.size());
        Assert.assertEquals(
                "http://auscope-services-test.arrc.csiro.au:80/geodesy/wfs?",
                resources.get(0).getLinkage().toString());
    }

    @Test
    public void testDescriptiveKeywords() throws XPathException, ParserConfigurationException, SAXException, IOException  {
        setUpForResponse("org/auscope/portal/core/test/responses/csw/cswRecordResponse.xml");

        final String[] actual = this.records[0].getDescriptiveKeywords();
        final String[] expected = new String[] {"WFS", "GeologicUnit", "MappedFeature", "gsml:GeologicUnit",
        "gsml:MappedFeature"};
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testMultipleOnlineResources() throws XPathException, ParserConfigurationException, SAXException, IOException  {
        setUpForResponse("org/auscope/portal/core/test/responses/csw/cswRecordResponse.xml");

        List<AbstractCSWOnlineResource> resources = this.records[14].getOnlineResources();
        Assert.assertEquals(4, resources.size());

        resources = this.records[14].getOnlineResourcesByType(OnlineResourceType.WCS);
        Assert.assertEquals(1, resources.size());
        Assert.assertEquals("http://apacsrv6/thredds/wcs/galeon/ocean.nc", resources.get(0).getLinkage().toString());

        resources = this.records[14].getOnlineResourcesByType(OnlineResourceType.WMS);
        Assert.assertEquals(1, resources.size());
        Assert.assertEquals("http://apacsrv6/thredds/wms/galeon/ocean.nc", resources.get(0).getLinkage().toString());

        resources = this.records[14].getOnlineResourcesByType(OnlineResourceType.FTP);
        Assert.assertEquals(1, resources.size());
        Assert.assertEquals("ftp://example.org/ftp", resources.get(0).getLinkage().toString());

        resources = this.records[14].getOnlineResourcesByType(OnlineResourceType.NCSS);
        Assert.assertEquals(1, resources.size());
        Assert.assertEquals("http://apacsrv6/thredds/ncss/galeon/ocean.nc", resources.get(0).getLinkage().toString());

        resources = this.records[14].getOnlineResourcesByType();
        Assert.assertEquals(0, resources.size());
        resources = this.records[14].getOnlineResourcesByType(OnlineResourceType.WCS, OnlineResourceType.WMS);
        Assert.assertEquals(2, resources.size());
        resources = this.records[14].getOnlineResourcesByType(OnlineResourceType.WCS, OnlineResourceType.WMS,
                OnlineResourceType.WFS);
        Assert.assertEquals(2, resources.size());
        resources = this.records[14].getOnlineResourcesByType(OnlineResourceType.Unsupported);
        Assert.assertEquals(0, resources.size());

        Assert.assertTrue(this.records[14].containsAnyOnlineResource(OnlineResourceType.WCS));
        Assert.assertTrue(this.records[14].containsAnyOnlineResource(OnlineResourceType.WMS));
        Assert.assertTrue(this.records[14].containsAnyOnlineResource(OnlineResourceType.FTP));
        Assert.assertTrue(this.records[14].containsAnyOnlineResource(OnlineResourceType.WCS, OnlineResourceType.WMS));
        Assert.assertTrue(this.records[14].containsAnyOnlineResource(OnlineResourceType.WCS, OnlineResourceType.WMS,
                OnlineResourceType.WFS));
        Assert.assertFalse(this.records[14].containsAnyOnlineResource(OnlineResourceType.WFS));
        Assert.assertFalse(this.records[14].containsAnyOnlineResource(OnlineResourceType.WFS,
                OnlineResourceType.Unsupported));

        //Now test another record with a slightly different schema
        resources = this.records[3].getOnlineResources();
        Assert.assertEquals(1, resources.size());
        resources = this.records[3].getOnlineResourcesByType(OnlineResourceType.FTP);
        Assert.assertEquals(1, resources.size());
        Assert.assertEquals("ftp://ftp.bom.gov.au/anon/home/geofabric/", resources.get(0).getLinkage().toString());
    }

    @Test
    public void testGeographicBoundingBoxParsing() throws XPathException, ParserConfigurationException, SAXException, IOException  {
        setUpForResponse("org/auscope/portal/core/test/responses/csw/cswRecordResponse.xml");

        final CSWGeographicElement[] geoEls = this.records[0].getCSWGeographicElements();

        Assert.assertNotNull(geoEls);
        Assert.assertEquals(1, geoEls.length);
        Assert.assertTrue(geoEls[0] instanceof CSWGeographicBoundingBox);

        final CSWGeographicBoundingBox bbox = (CSWGeographicBoundingBox) geoEls[0];

        Assert.assertEquals(145.00, bbox.getEastBoundLongitude(), 0.001);
        Assert.assertEquals(143.00, bbox.getWestBoundLongitude(), 0.001);
        Assert.assertEquals(-35.00, bbox.getNorthBoundLatitude(), 0.001);
        Assert.assertEquals(-39.00, bbox.getSouthBoundLatitude(), 0.001);
    }

    @Test
    public void testContactInfo() throws XPathException, ParserConfigurationException, SAXException, IOException  {
        setUpForResponse("org/auscope/portal/core/test/responses/csw/cswRecordResponse.xml");

        final CSWRecord rec = this.records[0];

        final CSWResponsibleParty respParty = rec.getContact();
        Assert.assertNotNull(respParty);

        Assert.assertEquals("Michael Stegherr", respParty.getIndividualName());
        Assert.assertEquals("CSIRO Exploration & Mining", respParty.getOrganisationName());
        Assert.assertEquals("Software Developer", respParty.getPositionName());

        final CSWContact contact = respParty.getContactInfo();
        Assert.assertNotNull(contact);

        Assert.assertEquals("Michael.Stegherr@csiro.au", contact.getAddressEmail());
        Assert.assertEquals("+61 2 2138961", contact.getTelephone());
        Assert.assertEquals("+61 2 314717304219", contact.getFacsimile());
        Assert.assertEquals("GPO Box 378", contact.getAddressDeliveryPoint());
        Assert.assertEquals("Canberra", contact.getAddressCity());
        Assert.assertEquals("ACT", contact.getAddressAdministrativeArea());
        Assert.assertEquals("2601", contact.getAddressPostalCode());

        final AbstractCSWOnlineResource contactResource = contact.getOnlineResource();
        Assert.assertNotNull(contactResource);

        Assert.assertEquals("http://www.em.csiro.au/", contactResource.getLinkage().toString());
        Assert.assertEquals("WWW:LINK-1.0-http--link", contactResource.getProtocol());
        Assert.assertEquals("CSIRO Exploration and Mining Web Site", contactResource.getName());
    }

    /**
     * Tests that the data quality info is correctly parsed
     * @throws IOException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     * @throws XPathExpressionException 
     */
    @Test
    public void testDataQualityInfo() throws XPathException, ParserConfigurationException, SAXException, IOException  {
        setUpForResponse("org/auscope/portal/core/test/responses/csw/cswRecordResponse.xml");

        Assert.assertEquals("Data Quality Statment 1", this.records[0].getDataQualityStatement());
        Assert.assertEquals("", this.records[1].getDataQualityStatement());
    }

    /**
     * Tests that the record is correctly parsed with empty dataSetURI.
     * field should not be null. but should be empty
     * @throws IOException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     * @throws XPathExpressionException 
     */
    @Test
    public void testNoDatasetURI() throws XPathException, ParserConfigurationException, SAXException, IOException  {
        setUpForResponse("org/auscope/portal/core/test/responses/csw/cswRecordResponse.xml");
        Assert.assertNotNull(this.records[0].getDataSetURIs());
        Assert.assertTrue(this.records[0].getDataSetURIs().length == 0);

    }

    /**
     * Tests that the record is correctly parsed with a single dataSetURI.
     * @throws IOException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     * @throws XPathExpressionException 
     */
    @Test
    public void testSingleDatasetURI() throws XPathException, ParserConfigurationException, SAXException, IOException  {
        setUpForResponse("org/auscope/portal/core/test/responses/csw/cswRecordResponse_SingleDatasetURI.xml");
        Assert.assertNotNull(this.records[0].getDataSetURIs());
        Assert.assertEquals("http://geology.data.vic.gov.au/searchAssistant/reference.html?q=record_id:26150",
                this.records[0].getDataSetURIs()[0]);
    }

    /**
     * Tests that the record is correctly parsed with a single dataSetURI.
     * @throws IOException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     * @throws XPathExpressionException 
     */
    @Test
    public void testMultipleDatasetURIs() throws XPathException, ParserConfigurationException, SAXException, IOException  {
        setUpForResponse("org/auscope/portal/core/test/responses/csw/cswRecordResponse_MultipleDatasetURIs.xml");
        Assert.assertNotNull(this.records[0].getDataSetURIs());
        Assert.assertEquals("http://geology.data.vic.gov.au/searchAssistant/reference.html?q=record_id:26150",
                this.records[0].getDataSetURIs()[0]);
        Assert.assertEquals("http://geology.data.vic.gov.au/searchAssistant/reference.html?q=record_id:26151",
                this.records[0].getDataSetURIs()[1]);
    }

    @Test
    public void testConstraints() throws XPathException, ParserConfigurationException, SAXException, IOException {
        setUpForResponse("org/auscope/portal/core/test/responses/csw/cswRecordResponse.xml");

        Assert.assertArrayEquals(new String[] {"CopyrightConstraint1", "CopyrightConstraint2"},
                this.records[0].getConstraints());
        Assert.assertArrayEquals(new String[] {}, this.records[1].getConstraints());
    }

    @Test
    public void testUploadedResourceParsing() throws XPathException, ParserConfigurationException, SAXException, IOException {
        setUpForResponse("org/auscope/portal/core/test/responses/csw/cswRecordResponse_UploadedResources.xml");

        final List<AbstractCSWOnlineResource> ors = this.records[0].getOnlineResources();

        Assert.assertNotNull(ors);
        Assert.assertEquals(1, ors.size());

        Assert.assertEquals(OnlineResourceType.WWW, ors.get(0).getType());
        Assert.assertEquals("Cooper_Basin_3D_Map_geology.vo", ors.get(0).getName());
    }

    @Test
    public void testCSWGeographicBoundboxConversion() {
        CSWGeographicBoundingBox box = new CSWGeographicBoundingBox(Double.NaN, Double.NaN, Double.NaN, Double.NaN);

        Assert.assertEquals(180, box.getEastBoundLongitude(), 0.1);
        Assert.assertEquals(-180, box.getWestBoundLongitude(), 0.1);
        Assert.assertEquals(90, box.getNorthBoundLatitude(), 0.1);
        Assert.assertEquals(-90, box.getSouthBoundLatitude(), 0.1);

        box = new CSWGeographicBoundingBox();
        box.setEastBoundLongitude(Double.NaN);
        box.setWestBoundLongitude(Double.NaN);
        box.setNorthBoundLatitude(Double.NaN);
        box.setSouthBoundLatitude(Double.NaN);

        Assert.assertEquals(180, box.getEastBoundLongitude(), 0.1);
        Assert.assertEquals(-180, box.getWestBoundLongitude(), 0.1);
        Assert.assertEquals(90, box.getNorthBoundLatitude(), 0.1);
        Assert.assertEquals(-90, box.getSouthBoundLatitude(), 0.1);
    }
    
    @Test
    public void testTransformToCSWRecordFromPyCSW() throws ParserConfigurationException, IOException, SAXException, XPathException {
        List<CSWRecord> records = parseCSWRecordFromGetRecordResponse("org/auscope/portal/core/test/responses/csw/cswRecordResponse_Pycsw_Thredds.xml", OgcServiceProviderType.PyCSW);
        Assert.assertEquals(5, records.size());        
        CSWRecord record = records.get(0);
        List<AbstractCSWOnlineResource> onlineRes = record.getOnlineResources();
        Assert.assertEquals(7, onlineRes.size());        
        Assert.assertEquals("Band1", onlineRes.get(2).getName());
        Assert.assertEquals(OnlineResourceType.WMS, onlineRes.get(2).getType());
        CSWGeographicElement[] bboxes = record.getCSWGeographicElements();
        Assert.assertEquals(1, bboxes.length);       
        Assert.assertEquals(151.92188, bboxes[0].getEastBoundLongitude(), 0.001);       
    }
    
    @Test
    public void testTransformToCSWRecordFromGeoserver() throws ParserConfigurationException, IOException, SAXException, XPathException {
        List<CSWRecord> records = parseCSWRecordFromGetRecordResponse("org/auscope/portal/core/test/responses/csw/cswRecordResponse_Geoserver.xml", OgcServiceProviderType.GeoServer);
        Assert.assertEquals(10, records.size());        
        CSWRecord record = records.get(0);
        Assert.assertEquals("9031SE_loweEE_dom", record.getLayerName());
        
        List<AbstractCSWOnlineResource> onlineRes = record.getOnlineResources();
        Assert.assertEquals(2, onlineRes.size());        
        Assert.assertEquals("9031SE_loweEE_dom", onlineRes.get(0).getName());
        Assert.assertEquals(OnlineResourceType.WMS, onlineRes.get(0).getType());
        
        CSWGeographicElement[] bboxes = record.getCSWGeographicElements();
        Assert.assertEquals(1, bboxes.length);       
        Assert.assertEquals(151.00606, bboxes[0].getEastBoundLongitude(), 0.001);       
    }
    
    
    private List<CSWRecord> parseCSWRecordFromGetRecordResponse(String resourceUrl, OgcServiceProviderType serviceType) throws ParserConfigurationException, IOException, SAXException, XPathException {
    	Document tmpdoc = DOMUtil.buildDomFromStream(ResourceUtil.loadResourceAsStream(resourceUrl));
        CSWNamespaceContext nc = new CSWNamespaceContext();
        XPathExpression exprRecordMetadata = DOMUtil.compileXPathExpr(
                "/csw:GetRecordsResponse/csw:SearchResults/(gmd:MD_Metadata|gmi:MI_Metadata)", nc);
        
        NodeList nodes = (NodeList) exprRecordMetadata.evaluate(tmpdoc, XPathConstants.NODESET);
        LinkedList<CSWRecord> records = new LinkedList<CSWRecord>();
        CSWRecordTransformerFactory transformerFactory = new CSWRecordTransformerFactory();
        
        for (int i = 0; i < nodes.getLength(); i++) {
            Node metadataNode = nodes.item(i);
            CSWRecordTransformer transformer = transformerFactory.newCSWRecordTransformer(metadataNode, serviceType);
            CSWRecord newRecord = transformer.transformToCSWRecord();
            records.add(newRecord);
        }
        return records;
    }
}

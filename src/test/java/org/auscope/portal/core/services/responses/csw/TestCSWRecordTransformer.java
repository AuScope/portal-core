package org.auscope.portal.core.services.responses.csw;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.auscope.portal.core.server.OgcServiceProviderType;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.namespaces.CSWNamespaceContext;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource.OnlineResourceType;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.auscope.portal.core.util.DOMUtil;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class TestCSWRecordTransformer extends PortalTestClass {
    private CSWRecord[] records;
    private Document doc;

    private static final String xmlnsUri = "http://www.w3.org/2000/xmlns/";

    private XPathExpression exprGetAllMetadataNodes;
    private XPathExpression exprGetFirstMetadataNode;

    private void setUpForResponse(final String responseResourceName) throws ParserConfigurationException, SAXException,
    IOException, XPathExpressionException {

        final CSWNamespaceContext nc = new CSWNamespaceContext();
        exprGetAllMetadataNodes = DOMUtil.compileXPathExpr("/csw:GetRecordsResponse/csw:SearchResults/gmd:MD_Metadata",
                nc);
        exprGetFirstMetadataNode = DOMUtil.compileXPathExpr(
                "/csw:GetRecordsResponse/csw:SearchResults/gmd:MD_Metadata[1]", nc);

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
    public void testGetServiceName() throws XPathExpressionException, ParserConfigurationException, SAXException,
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
    public void testGetServiceUrl() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException  {
        setUpForResponse("org/auscope/portal/core/test/responses/csw/cswRecordResponse.xml");

        AbstractCSWOnlineResource[] resources = this.records[4].getOnlineResourcesByType(OnlineResourceType.WFS);
        Assert.assertEquals(1, resources.length);
        Assert.assertEquals(
                "http://auscope-services-test.arrc.csiro.au/deegree-wfs/services?",
                resources[0].getLinkage().toString());

        resources = this.records[7].getOnlineResourcesByType(OnlineResourceType.WFS);
        Assert.assertEquals(1, resources.length);
        Assert.assertEquals(
                "http://auscope-services-test.arrc.csiro.au:80/geodesy/wfs?",
                resources[0].getLinkage().toString());
    }

    @Test
    public void testDescriptiveKeywords() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException  {
        setUpForResponse("org/auscope/portal/core/test/responses/csw/cswRecordResponse.xml");

        final String[] actual = this.records[0].getDescriptiveKeywords();
        final String[] expected = new String[] {"WFS", "GeologicUnit", "MappedFeature", "gsml:GeologicUnit",
        "gsml:MappedFeature"};
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testMultipleOnlineResources() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException  {
        setUpForResponse("org/auscope/portal/core/test/responses/csw/cswRecordResponse.xml");

        AbstractCSWOnlineResource[] resources = this.records[14].getOnlineResources();
        Assert.assertEquals(4, resources.length);

        resources = this.records[14].getOnlineResourcesByType(OnlineResourceType.WCS);
        Assert.assertEquals(1, resources.length);
        Assert.assertEquals("http://apacsrv6/thredds/wcs/galeon/ocean.nc", resources[0].getLinkage().toString());

        resources = this.records[14].getOnlineResourcesByType(OnlineResourceType.WMS);
        Assert.assertEquals(1, resources.length);
        Assert.assertEquals("http://apacsrv6/thredds/wms/galeon/ocean.nc", resources[0].getLinkage().toString());

        resources = this.records[14].getOnlineResourcesByType(OnlineResourceType.FTP);
        Assert.assertEquals(1, resources.length);
        Assert.assertEquals("ftp://example.org/ftp", resources[0].getLinkage().toString());

        resources = this.records[14].getOnlineResourcesByType(OnlineResourceType.NCSS);
        Assert.assertEquals(1, resources.length);
        Assert.assertEquals("http://apacsrv6/thredds/ncss/galeon/ocean.nc", resources[0].getLinkage().toString());

        resources = this.records[14].getOnlineResourcesByType();
        Assert.assertEquals(0, resources.length);
        resources = this.records[14].getOnlineResourcesByType(OnlineResourceType.WCS, OnlineResourceType.WMS);
        Assert.assertEquals(2, resources.length);
        resources = this.records[14].getOnlineResourcesByType(OnlineResourceType.WCS, OnlineResourceType.WMS,
                OnlineResourceType.WFS);
        Assert.assertEquals(2, resources.length);
        resources = this.records[14].getOnlineResourcesByType(OnlineResourceType.Unsupported);
        Assert.assertEquals(0, resources.length);

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
        Assert.assertEquals(1, resources.length);
        resources = this.records[3].getOnlineResourcesByType(OnlineResourceType.FTP);
        Assert.assertEquals(1, resources.length);
        Assert.assertEquals("ftp://ftp.bom.gov.au/anon/home/geofabric/", resources[0].getLinkage().toString());
    }

    @Test
    public void testGeographicBoundingBoxParsing() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException  {
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
    public void testContactInfo() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException  {
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
    public void testDataQualityInfo() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException  {
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
    public void testNoDatasetURI() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException  {
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
    public void testSingleDatasetURI() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException  {
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
    public void testMultipleDatasetURIs() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException  {
        setUpForResponse("org/auscope/portal/core/test/responses/csw/cswRecordResponse_MultipleDatasetURIs.xml");
        Assert.assertNotNull(this.records[0].getDataSetURIs());
        Assert.assertEquals("http://geology.data.vic.gov.au/searchAssistant/reference.html?q=record_id:26150",
                this.records[0].getDataSetURIs()[0]);
        Assert.assertEquals("http://geology.data.vic.gov.au/searchAssistant/reference.html?q=record_id:26151",
                this.records[0].getDataSetURIs()[1]);
    }

    /**
     * Generates an xpath-esque location for the current node for debug purposes
     *
     * @param node
     * @return
     */
    private static String debugLocation(final Node node) {
        final Stack<String> stack = new Stack<>();

        Node current = node;
        do {
            stack.push(String.format("%1$s", current.getLocalName()));

            current = current.getParentNode();
        } while (current != null);

        String result = "";
        while (!stack.isEmpty()) {
            result += stack.pop() + "/";
        }

        return result;
    }

    private static List<Node> getNonTextChildNodes(final Node node) {
        final List<Node> nonTextChildren = new ArrayList<>();

        final NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (!(children.item(i) instanceof Text)) {
                nonTextChildren.add(children.item(i));
            }
        }

        return nonTextChildren;
    }

    /**
     * Asserts that 2 nodes and child nodes are equal
     *
     * @param expected
     * @param actual
     */
    private void assertNodeTreesEqual(final Node expected, final Node actual) {
        final String debugLocationString = String.format("expected='%1$s'\nactual='%2$s'\n", debugLocation(expected),
                debugLocation(actual));

        //Compare node URI + name
        String expectedUri = expected.getNamespaceURI();
        if (expectedUri == null) {
            expectedUri = "";
        }
        String actualUri = actual.getNamespaceURI();
        if (actualUri == null) {
            actualUri = "";
        }
        //To deal with ambiguity of xmlns namespaces (http://xerces.apache.org/xerces2-j/faq-sax.html#faq-5)
        //we have to be a little tricky with our XML namespaces... (how annoying)
        if (expectedUri.equals(xmlnsUri) && actualUri.isEmpty()) {
            actualUri = xmlnsUri;
        } else if (actualUri.equals(xmlnsUri) && expectedUri.isEmpty()) {
            expectedUri = xmlnsUri;
        }
        Assert.assertEquals(debugLocationString, expectedUri, actualUri);
        Assert.assertEquals(debugLocationString, expected.getLocalName(), actual.getLocalName());

        //getNonNamespaceAttributes(expected);

        //Compare attributes (if any)
        final NamedNodeMap expectedAttr = expected.getAttributes();
        final NamedNodeMap actualAttr = actual.getAttributes();
        if (expectedAttr != null) {
            Assert.assertNotNull(debugLocationString, actualAttr);

            Assert.assertEquals(debugLocationString, expectedAttr.getLength(), actualAttr.getLength());

            for (int i = 0; i < expectedAttr.getLength(); i++) {
                assertNodeTreesEqual(expectedAttr.item(i), actualAttr.item(i));
            }
        } else {
            Assert.assertNull(debugLocationString, actualAttr);
        }

        //Compare children (if any)
        final List<Node> expectedChildren = getNonTextChildNodes(expected);
        final List<Node> actualChildren = getNonTextChildNodes(actual);
        Assert.assertEquals(debugLocationString, expectedChildren.size(), actualChildren.size());
        for (int i = 0; i < expectedChildren.size(); i++) {
            assertNodeTreesEqual(expectedChildren.get(i), actualChildren.get(i));
        }

        //And of course ensure the contents are equal
        String expectedValue = expected.getNodeValue();
        if (expectedValue != null) {
            expectedValue = expectedValue.replaceAll("\\s+", "");
        }
        String actualValue = actual.getNodeValue();
        if (actualValue != null) {
            actualValue = actualValue.replaceAll("\\s+", "");
        }

        Assert.assertEquals(debugLocationString, expectedValue, actualValue);
    }

    @Test
    public void testConstraints() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        setUpForResponse("org/auscope/portal/core/test/responses/csw/cswRecordResponse.xml");

        Assert.assertArrayEquals(new String[] {"CopyrightConstraint1", "CopyrightConstraint2"},
                this.records[0].getConstraints());
        Assert.assertArrayEquals(new String[] {}, this.records[1].getConstraints());
    }

    @Test
    public void testReverseTransformation() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException, PortalServiceException {
        setUpForResponse("org/auscope/portal/core/test/responses/csw/cswRecordResponse.xml");

        final CSWRecordTransformer transformer = new CSWRecordTransformer();

        final Node original = (Node) exprGetFirstMetadataNode.evaluate(doc, XPathConstants.NODE);
        final Node actual = transformer.transformToNode(this.records[0]);

        assertNodeTreesEqual(original, actual);
    }

    @Test
    public void testUploadedResourceParsing() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        setUpForResponse("org/auscope/portal/core/test/responses/csw/cswRecordResponse_UploadedResources.xml");

        final AbstractCSWOnlineResource[] ors = this.records[0].getOnlineResources();

        Assert.assertNotNull(ors);
        Assert.assertEquals(1, ors.length);

        Assert.assertEquals(OnlineResourceType.WWW, ors[0].getType());
        Assert.assertEquals("Cooper_Basin_3D_Map_geology.vo", ors[0].getName());
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
}

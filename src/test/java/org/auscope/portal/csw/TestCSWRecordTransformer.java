package org.auscope.portal.csw;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.auscope.portal.csw.CSWOnlineResource.OnlineResourceType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class TestCSWRecordTransformer {
	private CSWRecord[] records;
	private Document doc;

	private static final XPathExpression exprGetAllMetadataNodes = CSWXPathUtil.attemptCompileXpathExpr("/csw:GetRecordsResponse/csw:SearchResults/gmd:MD_Metadata");
	private static final XPathExpression exprGetFirstMetadataNode = CSWXPathUtil.attemptCompileXpathExpr("/csw:GetRecordsResponse/csw:SearchResults/gmd:MD_Metadata[1]");
	
    @Before
    public void setup() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        // load CSW record response document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        doc = builder.parse( "src/test/resources/cswRecordResponse.xml" );

        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new CSWNamespaceContext());
        
        NodeList nodes = (NodeList) exprGetAllMetadataNodes.evaluate(doc, XPathConstants.NODESET);
		
		records = new CSWRecord[nodes.getLength()];
		for(int i=0; i<nodes.getLength(); i++ ) {
			Node metadataNode = nodes.item(i);
			CSWRecordTransformer transformer = new CSWRecordTransformer(metadataNode);
		    records[i] = transformer.transformToCSWRecord();
		}
    }

    @Test
    public void testGetServiceName() throws XPathExpressionException {

        Assert.assertEquals(
                "GSV GeologicUnit WFS",
                this.records[0].getServiceName());

        Assert.assertEquals(
                "PIRSA EarthResource GeoServer WFS",
                this.records[2].getServiceName());
    }

    @Test
    public void testGetServiceUrl() throws XPathExpressionException {

        CSWOnlineResource[] resources = this.records[4].getOnlineResourcesByType(OnlineResourceType.WFS);
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
    public void testMultipleOnlineResources() throws Exception {
        CSWOnlineResource[] resources = this.records[14].getOnlineResources();
        Assert.assertEquals(2, resources.length);

        resources = this.records[14].getOnlineResourcesByType(OnlineResourceType.WCS);
        Assert.assertEquals(1, resources.length);
        Assert.assertEquals("http://apacsrv6/thredds/wcs/galeon/ocean.nc", resources[0].getLinkage().toString());

        resources = this.records[14].getOnlineResourcesByType(OnlineResourceType.WMS);
        Assert.assertEquals(1, resources.length);
        Assert.assertEquals("http://apacsrv6/thredds/wms/galeon/ocean.nc", resources[0].getLinkage().toString());

        resources = this.records[14].getOnlineResourcesByType();
        Assert.assertEquals(0, resources.length);
        resources = this.records[14].getOnlineResourcesByType(OnlineResourceType.WCS, OnlineResourceType.WMS);
        Assert.assertEquals(2, resources.length);
        resources = this.records[14].getOnlineResourcesByType(OnlineResourceType.WCS, OnlineResourceType.WMS, OnlineResourceType.WFS);
        Assert.assertEquals(2, resources.length);
        resources = this.records[14].getOnlineResourcesByType(OnlineResourceType.Unsupported);
        Assert.assertEquals(0, resources.length);

        Assert.assertTrue(this.records[14].containsAnyOnlineResource(OnlineResourceType.WCS));
        Assert.assertTrue(this.records[14].containsAnyOnlineResource(OnlineResourceType.WMS));
        Assert.assertTrue(this.records[14].containsAnyOnlineResource(OnlineResourceType.WCS, OnlineResourceType.WMS));
        Assert.assertTrue(this.records[14].containsAnyOnlineResource(OnlineResourceType.WCS, OnlineResourceType.WMS, OnlineResourceType.WFS));
        Assert.assertFalse(this.records[14].containsAnyOnlineResource(OnlineResourceType.WFS));
        Assert.assertFalse(this.records[14].containsAnyOnlineResource(OnlineResourceType.WFS, OnlineResourceType.Unsupported));
    }

    @Test
    public void testGeographicBoundingBoxParsing() throws Exception {
    	CSWGeographicElement[] geoEls = this.records[0].getCSWGeographicElements();

    	Assert.assertNotNull(geoEls);
    	Assert.assertEquals(1, geoEls.length);
    	Assert.assertTrue(geoEls[0] instanceof CSWGeographicBoundingBox);

    	CSWGeographicBoundingBox bbox = (CSWGeographicBoundingBox)geoEls[0];

    	Assert.assertEquals(145.00, bbox.getEastBoundLongitude(), 0.001);
    	Assert.assertEquals(143.00, bbox.getWestBoundLongitude(), 0.001);
    	Assert.assertEquals(-35.00, bbox.getNorthBoundLatitude(), 0.001);
    	Assert.assertEquals(-39.00, bbox.getSouthBoundLatitude(), 0.001);
    }
    
    @Test
    public void testContactInfo() throws Exception {
    	CSWRecord rec = this.records[0];
    	
    	Assert.assertEquals("Michael Stegherr", rec.getContactIndividual());
    	Assert.assertEquals("CSIRO Exploration & Mining", rec.getContactOrganisation());
    	Assert.assertEquals("Michael.Stegherr@csiro.au", rec.getContactEmail());
    	
    	Assert.assertEquals("http://www.em.csiro.au/", rec.getContactResource().getLinkage().toString());
    	Assert.assertEquals("WWW:LINK-1.0-http--link", rec.getContactResource().getProtocol());
    	Assert.assertEquals("CSIRO Exploration and Mining Web Site", rec.getContactResource().getName());
    }
    
    /**
     * Generates an xpath-esque location for the current node for debug purposes 
     * @param node
     * @return
     */
    private String debugLocation(Node node) {
    	Stack<String> stack = new Stack<String>();
    	
    	Node current = node;
    	do {
    		stack.push(String.format("%1$s", current.getLocalName()));
    		
    		current = current.getParentNode();
    	} while(current != null);
    	
    	String result = "";
    	while (!stack.isEmpty()) {
    		result += stack.pop() + "/";
    	}
    	
    	return result;
    }
    
    private List<Node> getNonTextChildNodes(Node node) {
    	List<Node> nonTextChildren = new ArrayList<Node>();
    	
    	NodeList children = node.getChildNodes();
    	for (int i = 0; i < children.getLength(); i++) {
    		if (!(children.item(i) instanceof Text)) {
    			nonTextChildren.add(children.item(i));
    		}
    	}
    	
    	return nonTextChildren;
    }
    
    /**
     * Asserts that 2 nodes and child nodes are equal
     * @param expected
     * @param actual
     */
    private void assertNodeTreesEqual(Node expected, Node actual) {
    	String debugLocationString = String.format("expected='%1$s'\nactual='%2$s'\n", debugLocation(expected), debugLocation(actual));
    	
    	//Compare node URI + name
    	String expectedUri = expected.getNamespaceURI();
    	if (expectedUri == null) {
    	    expectedUri = "";
        }
    	String actualUri = actual.getNamespaceURI();
    	if (actualUri == null) {
    	    actualUri = "";
        }
    	Assert.assertEquals(debugLocationString, expectedUri, actualUri);
    	Assert.assertEquals(debugLocationString, expected.getLocalName(), actual.getLocalName());
    	
    	//Compare attributes (if any)
    	NamedNodeMap expectedAttr = expected.getAttributes();
    	NamedNodeMap actualAttr = actual.getAttributes();
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
    	List<Node> expectedChildren = getNonTextChildNodes(expected);
    	List<Node> actualChildren = getNonTextChildNodes(actual);
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
    public void testConstraints() throws Exception {
        Assert.assertArrayEquals(new String[] {"CopyrightConstraint1", "CopyrightConstraint2"}, this.records[0].getConstraints());
        Assert.assertArrayEquals(new String[] {}, this.records[1].getConstraints());
    }
    
    @Test
    public void testReverseTransformation() throws Exception {
    	CSWRecordTransformer transformer = new CSWRecordTransformer();
    	
    	Node original = (Node) exprGetFirstMetadataNode.evaluate(doc, XPathConstants.NODE);
    	Node actual = transformer.transformToNode(this.records[0]);
    	
    	assertNodeTreesEqual(original, actual);
    }
}

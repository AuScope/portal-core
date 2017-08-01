package org.auscope.portal.core.xslt;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Unit tests for PortalXSLTTransformer
 * 
 * @author Josh Vote
 *
 */
public class TestPortalXSLTTransformer extends PortalTestClass {

    private PortalXSLTTransformer transformer;

    @Before
    public void setUp() {
        transformer = new PortalXSLTTransformer("/org/auscope/portal/core/xslt/wfsToKml.xsl");
    }

    /**
     * Unit test for testing the basic features of the transformer with the kml.xsl XSLT
     * @throws XPathExpressionException 
     * @throws IOException 
     * @throws ParserConfigurationException 
     * @throws SAXException 
     */
    @Test
    public void testGenericFeatureParser() throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
        final String testXml = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/wfs/GetUndefinedFeatureSet.xml");
        final Properties properties = new Properties();

        properties.setProperty("serviceUrl", "fake-service-url");

        String convertedText = transformer.convert(testXml, properties);

        // Check we have data
        Assert.assertNotNull(convertedText);
        Assert.assertTrue(convertedText.length() > 0);

        // Pull the converted data back as XML (It is now technically KML)
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(convertedText));
        Document document = builder.parse(inputSource);
        Element root = document.getDocumentElement();

        // Lets query the transformed data to make sure its correct
        XPath xPath = XPathFactory.newInstance().newXPath();

        Double counter = (Double) xPath.evaluate("count(Document)", root, XPathConstants.NUMBER);
        Assert.assertEquals(1.0, counter.doubleValue(), 0);

        counter = (Double) xPath.evaluate("count(Document/Placemark)", root, XPathConstants.NUMBER);
        Assert.assertEquals(8.0, counter.doubleValue(), 0);

        counter = (Double) xPath.evaluate("count(Document/Placemark/MultiGeometry/Point/Style/IconStyle/Icon/href)",
                root, XPathConstants.NUMBER);
        Assert.assertEquals(8.0, counter.doubleValue(), 0);
    }

    /**
     * GPT-74 - Unit test a specific wfs for the Oil Pipeline thsat is failing the current XSLT transformation.
     * @throws XPathExpressionException 
     * @throws IOException 
     * @throws ParserConfigurationException 
     * @throws SAXException 
     */
    @Test
    public void testOilPipelineFeatureParser() throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
        final String testXml = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/xslt/oilPipeline.xml");
        final Properties properties = new Properties();

        properties.setProperty("serviceUrl", "fake-service-url");

        String convertedText = transformer.convert(testXml, properties);
        System.out.println("testOilPipelineFeatureParser - transformed XML:\n" + convertedText + "\n");

        // Check we have data
        Assert.assertNotNull(convertedText);
        Assert.assertTrue(convertedText.length() > 0);

        // Pull the converted data back as XML (It is now technically KML)
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(convertedText));
        Document document = builder.parse(inputSource);
        Element root = document.getDocumentElement();

        System.setProperty("javax.xml.xpath.XPathFactory", "net.sf.saxon.xpath.XPathFactoryImpl");

        // Lets query the transformed data to make sure its correct
        XPath xPath = XPathFactory.newInstance().newXPath(); // NamespaceConstant.OBJECT_MODEL_DOM4J
        // XPath xPath = null;//new XPathFactoryImpl().newXPath();

        String docs = (String) xPath.evaluate("Document", root, XPathConstants.STRING);
        System.out.println("Docs: " + docs);

        Double counter = (Double) xPath.evaluate("count(Document)", root, XPathConstants.NUMBER);
        Assert.assertEquals(1.0, counter.doubleValue(), 0);

        counter = (Double) xPath.evaluate("count(Document/Placemark)", root, XPathConstants.NUMBER);
        Assert.assertEquals(1.0, counter.doubleValue(), 0);

        String coordinates = (String) xPath.evaluate("Document/Placemark/MultiGeometry/LineString/coordinates/text()",
                root,
                XPathConstants.STRING);
        System.out.println("Coordinates text: " + coordinates);
        Assert.assertNotNull(coordinates);
        Assert.assertTrue(coordinates.length() > 0);

        // I want to count the number of coordinates to assert that, but I cannot get 'tokenize()' to work even though it looks like XPATH2 is being used
        // counter = (Double) xPath.evaluate(
        // "tokenize(Document/Placemark/MultiGeometry/LineString/coordinates/text(), '\\s+,')", root,
        // XPathConstants.NUMBER);
        // Assert.assertEquals(28.0, counter.doubleValue(), 0);
    }
}

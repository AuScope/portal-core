package org.auscope.portal.server.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.auscope.portal.PortalTestClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Unit tests for PortalXSLTTransformer
 * @author Josh Vote
 *
 */
public class TestPortalXSLTTransformer extends PortalTestClass {

    private PortalXSLTTransformer transformer;

    @Before
    public void setUp() throws Exception {
        transformer = new PortalXSLTTransformer();
    }

    /**
     * Unit test for testing the basic features of the transformer with the kml.xsl XSLT
     * @throws Exception
     */
    @Test
    public void testGenericFeatureParser() throws Exception {
        final String testXml = org.auscope.portal.Util.loadXML("src/test/resources/GetUndefinedFeatureSet.xml");
        final InputStream inputStream = new FileInputStream("src/main/webapp/WEB-INF/xsl/kml.xsl");
        final Properties properties = new Properties();

        properties.setProperty("serviceURL", "fake-service-url");

        String convertedText = transformer.convert(testXml, inputStream, properties);

        //Check we have data
        Assert.assertNotNull(convertedText);
        Assert.assertTrue(convertedText.length() > 0);

        //Pull the converted data back as XML (It is now technically KML)
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false); // never forget this!
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(convertedText));
            Document document = builder.parse(inputSource);
            Element root = document.getDocumentElement();

        //Lets query the transformed data to make sure its correct
        XPath xPath = XPathFactory.newInstance().newXPath();

        Double counter = (Double) xPath.evaluate("count(Document)", root, XPathConstants.NUMBER);
        Assert.assertEquals(1.0, counter.doubleValue(),0);

        counter = (Double) xPath.evaluate("count(Document/Placemark)", root, XPathConstants.NUMBER);
        Assert.assertEquals(8.0, counter.doubleValue(),0);

        counter = (Double) xPath.evaluate("count(Document/Placemark/MultiGeometry/Point/Style/IconStyle/Icon/href)", root, XPathConstants.NUMBER);
        Assert.assertEquals(8.0, counter.doubleValue(),0);
    }


    /**
     * Unit test for testing the basic features of the transformer with the kml.xsl XSLT
     * @throws Exception
     */
    @Test
    public void testFeatureMemberEncoding() throws Exception {
        final String testXml = org.auscope.portal.Util.loadXML("src/test/resources/commodityGetFeatureResponse.xml");
        final InputStream inputStream = new FileInputStream("src/main/webapp/WEB-INF/xsl/WfsToHtml.xsl");
        final Properties properties = new Properties();

        properties.setProperty("serviceURL", "fake-service-url");

        String convertedText = transformer.convert(testXml, inputStream, properties);

        //Check we have data
        Assert.assertNotNull(convertedText);
        Assert.assertTrue(convertedText.length() > 0);

        Assert.assertTrue(convertedText.contains("urn:cgi:feature:GSV:Commodity:361169:AU"));
        Assert.assertTrue(convertedText.contains("urn:cgi:feature:GSV:Commodity:361170:AU"));

    }

    @Test
    public void testMiningFeatures() throws Exception {
        final String testXml = org.auscope.portal.Util.loadXML("src/test/resources/InvalidResultForTestingWfsToHtml.xml");
        final InputStream inputStream = new FileInputStream("src/main/webapp/WEB-INF/xsl/WfsToHtml.xsl");
        final Properties properties = new Properties();

        properties.setProperty("serviceURL", "fake-service-url");

        String convertedText = transformer.convert(testXml, inputStream, properties);
        System.out.println(convertedText);

        //Check we have data
        Assert.assertNotNull(convertedText);
        Assert.assertTrue(convertedText.length() > 0);

        Assert.assertTrue(!convertedText.contains("test.123"));
        Assert.assertTrue(!convertedText.contains("test name"));

        Assert.assertTrue(convertedText.contains("http://geology.data.nt.gov.au/resource/feature/ntgs/miningfeatureoccurrence/1113"));
        Assert.assertTrue(convertedText.contains("Mining Feature Occurrence Id"));
        Assert.assertTrue(convertedText.contains("http://geology.data.vic.gov.au/feature/gsv/commodity/922597-cu"));
        Assert.assertTrue(convertedText.contains("Commodity Id"));
        Assert.assertTrue(convertedText.contains("http://geology.data.vic.gov.au/feature/gsv/mineraloccurrence/922597"));
        Assert.assertTrue(convertedText.contains("http://geology.data.nt.gov.au/resource/feature/ntgs/mine/2368"));
        Assert.assertTrue(convertedText.contains("http://geology.data.nt.gov.au/resource/feature/ntgs/miningfeatureoccurrence/2368"));
        Assert.assertTrue(convertedText.contains("Mine Id"));
        Assert.assertTrue(convertedText.contains("http://geology.data.nt.gov.au/resource/feature/ntgs/miningactivity/2368/747"));
        Assert.assertTrue(convertedText.contains("MiningActivity Id"));
        Assert.assertTrue(convertedText.contains("EarthResourceML - MineralOccurrence"));
        Assert.assertTrue(convertedText.contains("EarthResourceML - Mine"));
        Assert.assertTrue(convertedText.contains("EarthResourceML - MiningActivity"));
        Assert.assertTrue(convertedText.contains("EarthResourceML - Commodity"));
        Assert.assertTrue(convertedText.contains("EarthResourceML - MiningFeatureOccurrence"));
        Assert.assertTrue(convertedText.contains("EarthResourceML - Mine"));


    }

    /**
     * Unit test for testing the basic features of the transformer with the kml.xsl XSLT
     * @throws Exception
     */
    @Test
    public void testFeatureMembersEncoding() throws Exception {
        final String testXml = org.auscope.portal.Util.loadXML("src/test/resources/mineGetFeatureResponse.xml");
        final InputStream inputStream = new FileInputStream("src/main/webapp/WEB-INF/xsl/WfsToHtml.xsl");
        final Properties properties = new Properties();

        properties.setProperty("serviceURL", "fake-service-url");

        String convertedText = transformer.convert(testXml, inputStream, properties);

        //Check we have data
        Assert.assertNotNull(convertedText);
        Assert.assertTrue(convertedText.length() > 0);

        Assert.assertTrue(convertedText.contains("WOOLDRIDGE CREEK WORKINGS"));
        Assert.assertTrue(convertedText.contains("HALL MAGNESITE MINE"));
        Assert.assertTrue(convertedText.contains("http://services-test.auscope.org/resource/feature/pirsa/mine/95"));
        Assert.assertTrue(convertedText.contains("http://services-test.auscope.org/resource/feature/pirsa/mine/217"));

    }
}

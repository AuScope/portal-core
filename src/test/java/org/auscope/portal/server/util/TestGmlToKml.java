package org.auscope.portal.server.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.jmock.Mockery;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.Source;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.StringReader;

import org.auscope.portal.server.util.Util;

public class TestGmlToKml {
	private GmlToKml gmlToKml = new GmlToKml();
	
	private Util util = new Util();
	
	private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
	
	private PortalPropertyPlaceholderConfigurer propertyConfigurer = context.mock(PortalPropertyPlaceholderConfigurer.class);
	
	
	@Before
    public void setup() throws Exception {
        final String serviceUrl = "somejunk";

        context.checking(new Expectations() {{
            oneOf(propertyConfigurer).resolvePlaceholder(with(any(String.class)));will(returnValue(serviceUrl));
        }});
	}
	
	@Test
	public void testGenericFeatureParser() throws Exception {
		String testXml = org.auscope.portal.Util.loadXML("src/test/resources/GetUndefinedFeatureSet.xml");
		InputStream inputStream = new FileInputStream("src/main/webapp/WEB-INF/xsl/kml.xsl");
		
		
		String convertedText = gmlToKml.convert(testXml, inputStream, "");
		
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
}

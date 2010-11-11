package org.auscope.portal.gsml;

import java.io.StringReader;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.auscope.portal.gsml.YilgarnNamespaceContext;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;



public class YilgarnLocSpecimenRecords {
	private static final Log logger = LogFactory.getLog(YilgarnLocSpecimenRecords.class);
	String serviceName;
	String dateAndTime;
	String observedMineralName;
	String observedMineralDescription;
	String obsProcessContact;
	String obsProcessMethod;
	String observedProperty;
	String quantityName;
	String quantityValue;
	String uom;
	
	
	public YilgarnLocSpecimenRecords(Node node, XPath xPath ) throws XPathExpressionException{
        Node tempNode = null;    
        xPath.setNamespaceContext(new YilgarnNamespaceContext());
        
        
        tempNode = (Node)xPath.evaluate("om:Observation/@gml:id", node, XPathConstants.NODE);
        serviceName = tempNode != null ? tempNode.getTextContent() : "";
        
        String timeDateExpression = "om:Observation/om:samplingTime/gml:TimeInstant/gml:timePosition";
        tempNode = (Node)xPath.evaluate(timeDateExpression, node, XPathConstants.NODE);
        dateAndTime = tempNode != null ? tempNode.getTextContent() : "";
        
        String ObservationProcessExpression = "om:Observation/om:procedure/omx:ObservationProcess/@gml:id";
        tempNode = (Node)xPath.evaluate(ObservationProcessExpression, node, XPathConstants.NODE);
        observedMineralName = tempNode != null ? tempNode.getTextContent() : "";
        
        String obsProcessDesExpression = "om:Observation/om:procedure/omx:ObservationProcess/gml:description";
        tempNode = (Node)xPath.evaluate(obsProcessDesExpression, node, XPathConstants.NODE);
        observedMineralDescription = tempNode != null ? tempNode.getTextContent() : "";
        
        String obsProcessContExpression = "om:Observation/om:procedure/omx:ObservationProcess/sml:contact/@xlink:title";
        tempNode = (Node)xPath.evaluate(obsProcessContExpression, node, XPathConstants.NODE);
        obsProcessContact = tempNode != null ? tempNode.getTextContent() : "";
        
        String obsProcessMethodExpression = "om:Observation/om:procedure/omx:ObservationProcess/omx:method";
        tempNode = (Node)xPath.evaluate(obsProcessMethodExpression, node, XPathConstants.NODE);
        obsProcessMethod = tempNode != null ? tempNode.getTextContent() : "";
        
        String observedPropertyExpression = "om:Observation/om:observedProperty/@xlink:href";
        tempNode = (Node)xPath.evaluate(observedPropertyExpression, node, XPathConstants.NODE);
        observedProperty = tempNode != null ? tempNode.getTextContent() : "";
        
        String quantityNameExpression = "om:Observation/om:result/swe:Quantity/gml:name";
        tempNode = (Node)xPath.evaluate(quantityNameExpression, node, XPathConstants.NODE);
        quantityName = tempNode != null ? tempNode.getTextContent() : "";
        
        String quantityValueExpression = "om:Observation/om:result/swe:Quantity/swe:value";
        tempNode = (Node)xPath.evaluate(quantityValueExpression, node, XPathConstants.NODE);
        quantityValue = tempNode != null ? tempNode.getTextContent() : "";
        
        String uomExpression = "om:Observation/om:result/swe:Quantity/swe:uom/@xlink:href";
        tempNode = (Node)xPath.evaluate(uomExpression, node, XPathConstants.NODE);
        uom = tempNode != null ? tempNode.getTextContent() : "";
	}	
	
	public String getServiceName()
	{
		return serviceName;
	}
	public String getDateAndTime()
	{
		return dateAndTime;
	}
	public String getObservedMineralName()
	{
		return observedMineralName;
	}
	public String getObservedMineralDescription()
	{
		return observedMineralDescription;
	}
	public String getObsProcessContact()
	{
		return obsProcessContact;
	}
	public String getObsProcessMethod()
	{
		return obsProcessMethod;
	}
	public String getObservedProperty()
	{
		return observedProperty;
	}
	public String getQuantityName()
	{
		return quantityName;
	}
	public String getQuantityValue()
	{
		return quantityValue;
	}
	public String getUom()
	{
		return uom;
	}
	
	public static YilgarnLocSpecimenRecords[] parseRecords(String gmlResponse)throws Exception{
		YilgarnLocSpecimenRecords[] records = null;
	
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    	factory.setNamespaceAware(true);
	        DocumentBuilder builder = factory.newDocumentBuilder();            
	        InputSource inputSource = new InputSource(new StringReader(gmlResponse));
	        Document doc = builder.parse(inputSource); 
	        
	        XPath xPath = XPathFactory.newInstance().newXPath();
	        xPath.setNamespaceContext(new YilgarnNamespaceContext());
	        NodeList exprResult = (NodeList)xPath.evaluate("/wfs:FeatureCollection/gml:featureMembers/sa:LocatedSpecimen/sa:relatedObservation", doc, XPathConstants.NODESET);            
	        
	        records = new YilgarnLocSpecimenRecords[exprResult.getLength()];
	        for (int i=0; i < exprResult.getLength(); i++) { 
	        	
	        	records[i] = new YilgarnLocSpecimenRecords(exprResult.item(i), xPath);	        	
	        }
			
			return records;
	}
	
}

package org.auscope.portal.core.services.responses.wcs;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Minimal response object for a WCS GetCapabilities request.
 * 
 * @author woo392
 *
 */
public class GetCapabilitiesRecord_1_0_0 {
	
	private final Log log = LogFactory.getLog(getClass());
	
	// WCS Capabilities Map<capability_type, url> where capability_type = GetCapabilities, GetCoverage, DescribeCoverage etc.
	Map<String, String> capabilities;
	
	// CoverageOfferingBriefs
	CoverageOfferingBrief[] coverageOfferingBriefs;
	
	
	private static final String EXTRACT_CAPABILITY_REQUESTS_EXPRESSION = "/WCS_Capabilities/Capability/Request";
	// This expression is relative to the previous expression
	private static final String EXTRACT_CAPABILITY_REQUEST_ONLINE_RESOURCES_EXPRESSION = "DCPType/HTTP/Get/OnlineResource/@href";
	private static final String EXTRACT_COVERAGE_OFFERING_BRIEFS_EXPRESSION = "/WCS_Capabilities/ContentMetadata/CoverageOfferingBrief";
	

	public GetCapabilitiesRecord_1_0_0(InputStream inXml) throws SAXException, IOException, ParserConfigurationException {
		try {
			Document doc = DOMUtil.buildDomFromStream(inXml, false);
			this.capabilities = getCapabilityRequests(doc);
			this.coverageOfferingBriefs = getCoverageBriefs(doc);
		} catch(SAXException e) {
			log.error("Parsing error: " + e.getMessage());
            throw e;
		} catch(IOException e) {
			log.error("IO error: " + e.getMessage());
            throw e;
		} catch (ParserConfigurationException e) {
			log.error("Parser configuration error: " + e.getMessage());
            throw e;
		}
	}
	
	/**
	 * Parse WCS GetCapabilities XML document for capability requests.
	 * 
	 * @param doc the WCS GetCapabilities XML document
	 * @return a Map of the capability requests <String(request), String(URL)>
	 */
	private Map<String, String> getCapabilityRequests(Document doc) {
		Map<String, String> capabilityRequests = new HashMap<String, String>();
        try {
        	XPathFactory xpFactory = XPathFactory.newInstance();
        	XPath xp = xpFactory.newXPath();
        	NodeList requestNodes = (NodeList)xp.evaluate(EXTRACT_CAPABILITY_REQUESTS_EXPRESSION, doc, XPathConstants.NODE);
        	for (int i = 0; i < requestNodes.getLength(); i++) {
        		if(!requestNodes.item(i).getNodeName().toLowerCase().equals("#text")) {
		        	String requestUrl = xp.evaluate(EXTRACT_CAPABILITY_REQUEST_ONLINE_RESOURCES_EXPRESSION, requestNodes.item(i));
		        	capabilityRequests.put(requestNodes.item(i).getNodeName(), requestUrl);
        		}
        	}
        } catch (XPathExpressionException e) {
            log.error("GetCapabilities get capability requests xml parsing error: " + e.getMessage());
        }
        return capabilityRequests;
    }
	
	/**
	 * 
	 * @param doc
	 * @return
	 */
	private CoverageOfferingBrief[] getCoverageBriefs(Document doc) {
		ArrayList<CoverageOfferingBrief> coverageOfferingsList = new ArrayList<CoverageOfferingBrief>();
		try {
			XPathFactory xpFactory = XPathFactory.newInstance();
        	XPath xp = xpFactory.newXPath();
        	NodeList requestNodes = (NodeList)xp.evaluate(EXTRACT_COVERAGE_OFFERING_BRIEFS_EXPRESSION, doc, XPathConstants.NODESET);
        	for (int i = 0; i < requestNodes.getLength(); i++) {
        		CoverageOfferingBrief cob = new CoverageOfferingBrief(requestNodes.item(i));
        		coverageOfferingsList.add(cob);
        	}
		} catch(XPathExpressionException e) {
			log.error("GetCapabilities get coverage offering briefs xml parsing error: " + e.getMessage());
		} catch(ParseException pe) {
			log.error("GetCapabilities get coverage offering parsing error: " + pe.getMessage());
		}
		CoverageOfferingBrief[] coverageOfferings = new CoverageOfferingBrief[coverageOfferingsList.size()];
		coverageOfferings = coverageOfferingsList.toArray(coverageOfferings);
		return coverageOfferings;
	}
	
	public Map<String, String> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(Map<String, String> capabilities) {
		this.capabilities = capabilities;
	}

	public CoverageOfferingBrief[] getCoverageOfferingBriefs() {
		return coverageOfferingBriefs;
	}

	public void setCoverageOfferingBriefs(CoverageOfferingBrief[] coverageOfferingBriefs) {
		this.coverageOfferingBriefs = coverageOfferingBriefs;
	}

}

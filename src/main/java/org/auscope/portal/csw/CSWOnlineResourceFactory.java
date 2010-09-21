package org.auscope.portal.csw;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

public abstract class CSWOnlineResourceFactory {
    /**
     * Parses a Node into its appropriate CSWOnlineResource representation.
     * @param node Must be a <gmd:CI_OnlineResource> node
     * @param xPath Must be configured with CSWNamespaceContext
     * @return
     * @throws XPathExpressionException 
     * @throws Exception
     */
    public static CSWOnlineResource parseFromNode(Node node, XPath xPath) throws XPathExpressionException {
        Node temp = null;
        Node urlNode = null;
        URL url = null;
        String name = "";
        String description = "";
        String protocol = "";
        
    	try {
            urlNode = (Node) xPath.evaluate("gmd:CI_OnlineResource/gmd:linkage/gmd:URL", node, XPathConstants.NODE);
            if(urlNode != null) {
            	url = new URL(urlNode.getTextContent());
            }
    	} catch (MalformedURLException ex) {
    		//TODO: URLs may now be malformed but we don't want to stop processing because of it
    		// as we are now allowing for multiple URLs. Ignore for now.
    		//	throw new IllegalArgumentException(String.format("malformed url '%1$s'",temp.getTextContent()), ex);
    	}        	
        
        temp = (Node) xPath.evaluate("gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString", node, XPathConstants.NODE);
        if (temp != null)
            protocol = temp.getTextContent();
        
        temp = (Node) xPath.evaluate("gmd:CI_OnlineResource/gmd:name/gco:CharacterString", node, XPathConstants.NODE);
        if (temp != null)
            name = temp.getTextContent();
        
        temp = (Node) xPath.evaluate("gmd:CI_OnlineResource/gmd:description/gco:CharacterString", node, XPathConstants.NODE);
        if (temp != null)
            description = temp.getTextContent();
        
        return new CSWOnlineResourceImpl(url, protocol, name, description);
    }
}
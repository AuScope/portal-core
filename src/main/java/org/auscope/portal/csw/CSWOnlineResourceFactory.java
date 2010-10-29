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

	private static final String protocolXpath = "gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString";
	private static final String nameXpath = "gmd:CI_OnlineResource/gmd:name/gco:CharacterString";
	private static final String descriptionXpath = "gmd:CI_OnlineResource/gmd:description/gco:CharacterString";
	private static final String urlXpath = "gmd:CI_OnlineResource/gmd:linkage/gmd:URL";


    public static CSWOnlineResource parseFromNode(Node node, XPath xPath) throws XPathExpressionException {
        String urlString = null;
        String name = "";
        String description = "";
        String protocol = "";
        URL url = null;

    	try {
    		urlString = (String) xPath.evaluate(urlXpath, node, XPathConstants.STRING);
            if(urlString != null) {
            	url = new URL(urlString);
            }
    	} catch (MalformedURLException ex) {
    		//TODO: URLs may now be malformed but we don't want to stop processing because of it
    		// as we are now allowing for multiple URLs. Ignore for now.
    		//	throw new IllegalArgumentException(String.format("malformed url '%1$s'",temp.getTextContent()), ex);
    	}

    	protocol = (String) xPath.evaluate(protocolXpath, node, XPathConstants.STRING);
    	name = (String) xPath.evaluate(nameXpath, node, XPathConstants.STRING);
        description = (String) xPath.evaluate(descriptionXpath, node, XPathConstants.STRING);

        return new CSWOnlineResourceImpl(url, protocol, name, description);
    }
}
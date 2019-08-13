package org.auscope.portal.core.services.responses.csw;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.namespaces.CSWNamespaceContext;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Node;

/**
 * A factory for creating CSWOnlineResource objects.
 */
public abstract class CSWOnlineResourceFactory {
    
	protected static final Log logger = LogFactory.getLog(CSWOnlineResourceFactory.class);
        
    /**
     * Parses a Node into its appropriate CSWOnlineResource representation.
     *
     * @param node
     *            Must be a <gmd:CI_OnlineResource> node
     * @param threddsLayerName 
     * 			  the name of dataset layer indexed by Thredds server.
     * @return the abstract csw online resource
     * @throws XPathExpressionException
     *             the x path expression exception
     */
    public static AbstractCSWOnlineResource parseFromNode(Node node, String threddsLayerName) throws XPathExpressionException {
        String urlString = null;
        String name = "";
        String description = "";
        String protocol = "";
        String applicationProfile = "";
        URL url = null;

        CSWNamespaceContext nc = new CSWNamespaceContext();
        XPathExpression protocolXpath = DOMUtil.compileXPathExpr(
                "gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString", nc);
        XPathExpression nameXpath = DOMUtil.compileXPathExpr(
                "gmd:CI_OnlineResource/gmd:name/gco:CharacterString|gmd:CI_OnlineResource/gmd:name/gmx:MimeFileType",
                nc);
        XPathExpression descriptionXpath = DOMUtil.compileXPathExpr(
                "gmd:CI_OnlineResource/gmd:description/gco:CharacterString", nc);
        XPathExpression urlXpath = DOMUtil.compileXPathExpr("gmd:CI_OnlineResource/gmd:linkage/gmd:URL", nc);
        XPathExpression applicationProfileXpath = DOMUtil.compileXPathExpr(
                "gmd:CI_OnlineResource/gmd:applicationProfile/gco:CharacterString", nc);

        try {
            urlString = (String) urlXpath.evaluate(node, XPathConstants.STRING);
            if (urlString != null) {
                url = new URL(urlString);
            }
        } catch (MalformedURLException ex) {
            //TODO: URLs may now be malformed but we don't want to stop processing because of it
            // as we are now allowing for multiple URLs. Ignore for now.
            // throw new IllegalArgumentException(String.format("malformed url '%1$s'",temp.getTextContent()), ex);
        }

        protocol = (String) protocolXpath.evaluate(node, XPathConstants.STRING);
        if (threddsLayerName != null && threddsLayerName.length() > 0) {
        	name = threddsLayerName;
        }
        else {
            name = (String) nameXpath.evaluate(node, XPathConstants.STRING);
        }
        description = (String) descriptionXpath.evaluate(node, XPathConstants.STRING);
        applicationProfile = (String) applicationProfileXpath.evaluate(node, XPathConstants.STRING);
        return new CSWOnlineResourceImpl(url, protocol, name, description, applicationProfile);
    }
}

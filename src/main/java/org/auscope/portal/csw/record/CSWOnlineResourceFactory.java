package org.auscope.portal.csw.record;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.auscope.portal.csw.CSWNamespaceContext;
import org.auscope.portal.server.util.DOMUtil;
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
    public static CSWOnlineResource parseFromNode(Node node) throws XPathExpressionException {
        String urlString = null;
        String name = "";
        String description = "";
        String protocol = "";
        String applicationProfile = "";
        URL url = null;

        CSWNamespaceContext nc = new CSWNamespaceContext();
        XPathExpression protocolXpath = DOMUtil.compileXPathExpr("gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString", nc);
        XPathExpression nameXpath = DOMUtil.compileXPathExpr("gmd:CI_OnlineResource/gmd:name/gco:CharacterString", nc);
        XPathExpression descriptionXpath = DOMUtil.compileXPathExpr("gmd:CI_OnlineResource/gmd:description/gco:CharacterString", nc);
        XPathExpression urlXpath = DOMUtil.compileXPathExpr("gmd:CI_OnlineResource/gmd:linkage/gmd:URL", nc);
        XPathExpression applicationProfileXpath = DOMUtil.compileXPathExpr("gmd:CI_OnlineResource/gmd:applicationProfile/gco:CharacterString", nc);

        try {
            urlString = (String) urlXpath.evaluate(node, XPathConstants.STRING);
            if(urlString != null) {
                url = new URL(urlString);
            }
        } catch (MalformedURLException ex) {
            //TODO: URLs may now be malformed but we don't want to stop processing because of it
            // as we are now allowing for multiple URLs. Ignore for now.
            //	throw new IllegalArgumentException(String.format("malformed url '%1$s'",temp.getTextContent()), ex);
        }

        protocol = (String) protocolXpath.evaluate(node, XPathConstants.STRING);
        name = (String) nameXpath.evaluate(node, XPathConstants.STRING);
        description = (String) descriptionXpath.evaluate(node, XPathConstants.STRING);
        applicationProfile = (String) applicationProfileXpath.evaluate(node, XPathConstants.STRING);

        return new CSWOnlineResourceImpl(url, protocol, name, description, applicationProfile);
    }
}

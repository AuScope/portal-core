package org.auscope.portal.core.services.responses.csw;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.auscope.portal.core.services.namespaces.CSWNamespaceContext;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Node;

/**
 * Contains factory methods for the CSWContact class.
 *
 * @author Josh Vote
 * @version $Id$
 */
public class CSWContactFactory {

    /** The Constant xPath expression for XPATHTELEPHONE. */
    private static final String XPATHTELEPHONE = "gmd:phone/gmd:CI_Telephone/gmd:voice/gco:CharacterString";

    /** The Constant xPath expression for XPATHFACSIMILE. */
    private static final String XPATHFACSIMILE = "gmd:phone/gmd:CI_Telephone/gmd:facsimile/gco:CharacterString";

    /** The Constant xPath expression for XPATHADDRESSDELIVERYPOINT. */
    private static final String XPATHADDRESSDELIVERYPOINT = "gmd:address/gmd:CI_Address/gmd:deliveryPoint/gco:CharacterString";

    /** The Constant xPath expression for XPATHADDRESSCITY. */
    private static final String XPATHADDRESSCITY = "gmd:address/gmd:CI_Address/gmd:city/gco:CharacterString";

    /** The Constant xPath expression for XPATHADDRESSADMINISTRATIVEAREA. */
    private static final String XPATHADDRESSADMINISTRATIVEAREA = "gmd:address/gmd:CI_Address/gmd:administrativeArea/gco:CharacterString";

    /** The Constant xPath expression for XPATHADDRESSPOSTALCODE. */
    private static final String XPATHADDRESSPOSTALCODE = "gmd:address/gmd:CI_Address/gmd:postalCode/gco:CharacterString";

    /** The Constant xPath expression for XPATHADDRESSCOUNTRY. */
    private static final String XPATHADDRESSCOUNTRY = "gmd:address/gmd:CI_Address/gmd:country/gco:CharacterString";

    /** The Constant xPath expression for XPATHADDRESSEMAIL. */
    private static final String XPATHADDRESSEMAIL = "gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString";

    /** The Constant xPath expression for XPATHONLINERESOURCE. */
    private static final String XPATHONLINERESOURCE = "gmd:onlineResource";

    /**
     * Given a node representing a gmd:CI_Contact, generate a CSWContact instance and return it.
     *
     * @param node
     *            Must be a gmd_CI_Contact node
     * @return the cSW contact
     * @throws XPathExpressionException
     *             the x path expression exception
     */
    public static CSWContact generateContactFromCIContactNode(Node node) throws XPathExpressionException {
        CSWContact contact = new CSWContact();
        CSWNamespaceContext nc = new CSWNamespaceContext();

        //Parse from each of our fields, we are OK with missing values
        Node field = (Node) DOMUtil.compileXPathExpr(XPATHTELEPHONE, nc).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            contact.setTelephone(field.getTextContent());
        }

        field = (Node) DOMUtil.compileXPathExpr(XPATHFACSIMILE, nc).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            contact.setFacsimile(field.getTextContent());
        }

        field = (Node) DOMUtil.compileXPathExpr(XPATHADDRESSDELIVERYPOINT, nc).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            contact.setAddressDeliveryPoint(field.getTextContent());
        }

        field = (Node) DOMUtil.compileXPathExpr(XPATHADDRESSCITY, nc).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            contact.setAddressCity(field.getTextContent());
        }

        field = (Node) DOMUtil.compileXPathExpr(XPATHADDRESSADMINISTRATIVEAREA, nc).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            contact.setAddressAdministrativeArea(field.getTextContent());
        }

        field = (Node) DOMUtil.compileXPathExpr(XPATHADDRESSPOSTALCODE, nc).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            contact.setAddressPostalCode(field.getTextContent());
        }

        field = (Node) DOMUtil.compileXPathExpr(XPATHADDRESSCOUNTRY, nc).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            contact.setAddressCountry(field.getTextContent());
        }

        field = (Node) DOMUtil.compileXPathExpr(XPATHADDRESSEMAIL, nc).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            contact.setAddressEmail(field.getTextContent());
        }

        field = (Node) DOMUtil.compileXPathExpr(XPATHONLINERESOURCE, nc).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            AbstractCSWOnlineResource resource = CSWOnlineResourceFactory.parseFromNode(field, null);
            contact.setOnlineResource(resource);
        }

        return contact;
    }
}

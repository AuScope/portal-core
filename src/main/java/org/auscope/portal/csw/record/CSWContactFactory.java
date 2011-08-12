package org.auscope.portal.csw.record;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.auscope.portal.csw.CSWNamespaceContext;
import org.auscope.portal.server.util.DOMUtil;
import org.w3c.dom.Node;

/**
 * Contains factory methods for the CSWContact class
 * @author Josh Vote
 *
 */
public class CSWContactFactory {

    private static final String xpathTelephone = "gmd:phone/gmd:CI_Telephone/gmd:voice/gco:CharacterString";
    private static final String xpathFacsimile = "gmd:phone/gmd:CI_Telephone/gmd:facsimile/gco:CharacterString";

    private static final String xpathAddressDeliveryPoint = "gmd:address/gmd:CI_Address/gmd:deliveryPoint/gco:CharacterString";
    private static final String xpathAddressCity = "gmd:address/gmd:CI_Address/gmd:city/gco:CharacterString";
    private static final String xpathAddressAdministrativeArea = "gmd:address/gmd:CI_Address/gmd:administrativeArea/gco:CharacterString";
    private static final String xpathAddressPostalCode = "gmd:address/gmd:CI_Address/gmd:postalCode/gco:CharacterString";
    private static final String xpathAddressCountry = "gmd:address/gmd:CI_Address/gmd:country/gco:CharacterString";
    private static final String xpathAddressEmail = "gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString";

    private static final String xpathOnlineResource = "gmd:onlineResource";

    /**
     * Given a node representing a gmd:CI_Contact, generate a CSWContact instance and return it
     * @param node Must be a gmd_CI_Contact node
     * @throws XPathExpressionException
     */
    public static CSWContact generateContactFromCIContactNode(Node node) throws XPathExpressionException {
        CSWContact contact = new CSWContact();
        CSWNamespaceContext nc = new CSWNamespaceContext();

        //Parse from each of our fields, we are OK with missing values
        Node field = (Node) DOMUtil.compileXPathExpr(xpathTelephone, nc).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            contact.setTelephone(field.getTextContent());
        }

        field = (Node) DOMUtil.compileXPathExpr(xpathFacsimile, nc).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            contact.setFacsimile(field.getTextContent());
        }

        field = (Node) DOMUtil.compileXPathExpr(xpathAddressDeliveryPoint, nc).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            contact.setAddressDeliveryPoint(field.getTextContent());
        }

        field = (Node) DOMUtil.compileXPathExpr(xpathAddressCity, nc).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            contact.setAddressCity(field.getTextContent());
        }

        field = (Node) DOMUtil.compileXPathExpr(xpathAddressAdministrativeArea, nc).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            contact.setAddressAdministrativeArea(field.getTextContent());
        }

        field = (Node) DOMUtil.compileXPathExpr(xpathAddressPostalCode, nc).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            contact.setAddressPostalCode(field.getTextContent());
        }

        field = (Node) DOMUtil.compileXPathExpr(xpathAddressCountry, nc).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            contact.setAddressCountry(field.getTextContent());
        }

        field = (Node) DOMUtil.compileXPathExpr(xpathAddressEmail, nc).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            contact.setAddressEmail(field.getTextContent());
        }

        field = (Node) DOMUtil.compileXPathExpr(xpathOnlineResource, nc).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            CSWOnlineResource resource = CSWOnlineResourceFactory.parseFromNode(field);
            contact.setOnlineResource(resource);
        }

        return contact;
    }
}

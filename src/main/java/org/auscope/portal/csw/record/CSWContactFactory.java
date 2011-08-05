package org.auscope.portal.csw.record;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.auscope.portal.csw.CSWXPathUtil;
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

        //Parse from each of our fields, we are OK with missing values
        Node field = (Node) CSWXPathUtil.attemptCompileXpathExpr(xpathTelephone).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            contact.setTelephone(field.getTextContent());
        }

        field = (Node) CSWXPathUtil.attemptCompileXpathExpr(xpathFacsimile).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            contact.setFacsimile(field.getTextContent());
        }

        field = (Node) CSWXPathUtil.attemptCompileXpathExpr(xpathAddressDeliveryPoint).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            contact.setAddressDeliveryPoint(field.getTextContent());
        }

        field = (Node) CSWXPathUtil.attemptCompileXpathExpr(xpathAddressCity).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            contact.setAddressCity(field.getTextContent());
        }

        field = (Node) CSWXPathUtil.attemptCompileXpathExpr(xpathAddressAdministrativeArea).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            contact.setAddressAdministrativeArea(field.getTextContent());
        }

        field = (Node) CSWXPathUtil.attemptCompileXpathExpr(xpathAddressPostalCode).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            contact.setAddressPostalCode(field.getTextContent());
        }

        field = (Node) CSWXPathUtil.attemptCompileXpathExpr(xpathAddressCountry).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            contact.setAddressCountry(field.getTextContent());
        }

        field = (Node) CSWXPathUtil.attemptCompileXpathExpr(xpathAddressEmail).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            contact.setAddressEmail(field.getTextContent());
        }

        field = (Node) CSWXPathUtil.attemptCompileXpathExpr(xpathOnlineResource).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            CSWOnlineResource resource = CSWOnlineResourceFactory.parseFromNode(field);
            contact.setOnlineResource(resource);
        }

        return contact;
    }
}

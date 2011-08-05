package org.auscope.portal.csw.record;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.auscope.portal.csw.CSWXPathUtil;
import org.w3c.dom.Node;

/**
 * A factory class with methods for instantiating CSWResponsibleParty objects
 * @author Josh Vote
 *
 */
public class CSWResponsiblePartyFactory {

    private static final String xPathIndividualName = "gmd:individualName/gco:CharacterString";
    private static final String xPathOrganisationName = "gmd:organisationName/gco:CharacterString";
    private static final String xPathPositionName = "gmd:positionName/gco:CharacterString";
    private static final String xPathContactInfo = "gmd:contactInfo/gmd:CI_Contact";

    /**
     * Attempts to parse a gmd:CI_Responsible party node into a CSWResponsibleParty element
     * @param node Must be a gmd:CI_Responsible element
     * @return
     * @throws XPathExpressionException
     */
    public static CSWResponsibleParty generateResponsiblePartyFromNode(Node node) throws XPathExpressionException {
        CSWResponsibleParty rp = new CSWResponsibleParty();

        //Parse from each of our fields, we are OK with missing values
        Node field = (Node) CSWXPathUtil.attemptCompileXpathExpr(xPathIndividualName).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            rp.setIndividualName(field.getTextContent());
        }

        field = (Node) CSWXPathUtil.attemptCompileXpathExpr(xPathOrganisationName).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            rp.setOrganisationName(field.getTextContent());
        }

        field = (Node) CSWXPathUtil.attemptCompileXpathExpr(xPathPositionName).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            rp.setPositionName(field.getTextContent());
        }

        field = (Node) CSWXPathUtil.attemptCompileXpathExpr(xPathContactInfo).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            CSWContact contact = CSWContactFactory.generateContactFromCIContactNode(field);
            rp.setContactInfo(contact);
        }

        return rp;
    }
}

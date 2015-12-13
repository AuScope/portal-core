package org.auscope.portal.core.services.responses.csw;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.auscope.portal.core.services.namespaces.CSWNamespaceContext;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Node;

/**
 * A factory class with methods for instantiating CSWResponsibleParty objects
 * 
 * @author Josh Vote
 *
 */
public class CSWResponsiblePartyFactory {

    private static final String XPATHINDIVIDUALNAME = "gmd:individualName/gco:CharacterString";
    private static final String XPATHORGANISATIONNAME = "gmd:organisationName/gco:CharacterString";
    private static final String xPathPositionName = "gmd:positionName/gco:CharacterString";
    private static final String xPathContactInfo = "gmd:contactInfo/gmd:CI_Contact";

    /**
     * Attempts to parse a gmd:CI_Responsible party node into a CSWResponsibleParty element.
     * 
     * @param node
     *            Must be a gmd:CI_Responsible element
     * @throws XPathExpressionException
     *             invalid xml.
     * @return CSWResponsibleParty
     */
    public static CSWResponsibleParty generateResponsiblePartyFromNode(Node node) throws XPathExpressionException {
        CSWResponsibleParty rp = new CSWResponsibleParty();
        CSWNamespaceContext nc = new CSWNamespaceContext();

        //Parse from each of our fields, we are OK with missing values
        Node field = (Node) DOMUtil.compileXPathExpr(XPATHINDIVIDUALNAME, nc).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            rp.setIndividualName(field.getTextContent());
        }

        field = (Node) DOMUtil.compileXPathExpr(XPATHORGANISATIONNAME, nc).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            rp.setOrganisationName(field.getTextContent());
        }

        field = (Node) DOMUtil.compileXPathExpr(xPathPositionName, nc).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            rp.setPositionName(field.getTextContent());
        }

        field = (Node) DOMUtil.compileXPathExpr(xPathContactInfo, nc).evaluate(node, XPathConstants.NODE);
        if (field != null) {
            CSWContact contact = CSWContactFactory.generateContactFromCIContactNode(field);
            rp.setContactInfo(contact);
        }

        return rp;
    }
}

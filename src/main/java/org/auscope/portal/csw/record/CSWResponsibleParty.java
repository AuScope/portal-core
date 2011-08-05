package org.auscope.portal.csw.record;

/**
 * Represents a simplified version of a gmd:CI_ResponsibleParty element from a CSW record response
 * @author Josh Vote
 *
 */
public class CSWResponsibleParty {
    private String individualName;
    private String organisationName;
    private String positionName;
    private CSWContact contactInfo;

    public String getIndividualName() {
        return individualName;
    }
    public void setIndividualName(String individualName) {
        this.individualName = individualName;
    }
    public String getOrganisationName() {
        return organisationName;
    }
    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }
    public String getPositionName() {
        return positionName;
    }
    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }
    public CSWContact getContactInfo() {
        return contactInfo;
    }
    public void setContactInfo(CSWContact contactInfo) {
        this.contactInfo = contactInfo;
    }
    @Override
    public String toString() {
        return "CSWResponsibleParty [individualName=" + individualName
                + ", organisationName=" + organisationName + ", positionName="
                + positionName + ", contactInfo=" + contactInfo + "]";
    }



}

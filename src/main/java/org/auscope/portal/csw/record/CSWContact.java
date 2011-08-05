package org.auscope.portal.csw.record;
/**
 * Represents a simplified view of a gmd:CI_Contact XML element which is used
 * for holding contact information about a particular individual or organisation.
 * @author Josh Vote
 *
 */
public class CSWContact {
    private String telephone;
    private String facsimile;

    private String addressDeliveryPoint;
    private String addressCity;
    private String addressAdministrativeArea;
    private String addressPostalCode;
    private String addressCountry;
    private String addressEmail;

    private CSWOnlineResource onlineResource;


    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getFacsimile() {
        return facsimile;
    }

    public void setFacsimile(String facsimile) {
        this.facsimile = facsimile;
    }

    public String getAddressDeliveryPoint() {
        return addressDeliveryPoint;
    }

    public void setAddressDeliveryPoint(String addressDeliveryPoint) {
        this.addressDeliveryPoint = addressDeliveryPoint;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    public String getAddressAdministrativeArea() {
        return addressAdministrativeArea;
    }

    public void setAddressAdministrativeArea(String addressAdministrativeArea) {
        this.addressAdministrativeArea = addressAdministrativeArea;
    }

    public String getAddressPostalCode() {
        return addressPostalCode;
    }

    public void setAddressPostalCode(String addressPostalCode) {
        this.addressPostalCode = addressPostalCode;
    }

    public String getAddressCountry() {
        return addressCountry;
    }

    public void setAddressCountry(String addressCountry) {
        this.addressCountry = addressCountry;
    }

    public String getAddressEmail() {
        return addressEmail;
    }

    public void setAddressEmail(String addressEmail) {
        this.addressEmail = addressEmail;
    }

    public CSWOnlineResource getOnlineResource() {
        return onlineResource;
    }

    public void setOnlineResource(CSWOnlineResource onlineResource) {
        this.onlineResource = onlineResource;
    }

    @Override
    public String toString() {
        return "CSWContact [telephone=" + telephone + ", facsimile="
                + facsimile + ", addressDeliveryPoint=" + addressDeliveryPoint
                + ", addressCity=" + addressCity
                + ", addressAdministrativeArea=" + addressAdministrativeArea
                + ", addressPostalCode=" + addressPostalCode
                + ", addressCountry=" + addressCountry + ", addressEmail="
                + addressEmail + ", onlineResource=" + onlineResource + "]";
    }


}

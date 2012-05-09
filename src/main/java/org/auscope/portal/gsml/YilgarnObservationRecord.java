package org.auscope.portal.gsml;

/**
 * Represents a view of an om:Observation feature type as used in the Yilgarn Laterite geochemistry sa:LocatedSpecimen feature
 */
public class YilgarnObservationRecord {

    /** The service name. */
    private String serviceName;

    /** The date. */
    private String date;

    /** The observed mineral name. */
    private String observedMineralName;

    /** The preparation details. */
    private String preparationDetails;

    /** The lab details. */
    private String labDetails;

    /** The analytical method. */
    private String analyticalMethod;

    /** The observed property. */
    private String observedProperty;

    /** The analyte name. */
    private String analyteName;

    /** The analyte value. */
    private String analyteValue;

    /** The uom. */
    private String uom;



    /**
     * Generates a new Located Specimen Record
     * @param serviceName service name
     * @param date date
     * @param observedMineralName observed mineral name
     * @param preparationDetails preparation details
     * @param labDetails lab details
     * @param analyticalMethod analytical method
     * @param observedProperty observed property
     * @param analyteName analyte name
     * @param analyteValue analyte value
     * @param uom unit of measure
     */
    public YilgarnObservationRecord(String serviceName, String date,
            String observedMineralName, String preparationDetails,
            String labDetails, String analyticalMethod,
            String observedProperty, String analyteName, String analyteValue,
            String uom) {
        this.serviceName = serviceName;
        this.date = date;
        this.observedMineralName = observedMineralName;
        this.preparationDetails = preparationDetails;
        this.labDetails = labDetails;
        this.analyticalMethod = analyticalMethod;
        this.observedProperty = observedProperty;
        this.analyteName = analyteName;
        this.analyteValue = analyteValue;
        this.uom = uom;
    }

    /**
     * Gets the service name.
     *
     * @return the service name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * Gets the observed mineral name.
     *
     * @return the observed mineral name
     */
    public String getObservedMineralName() {
        return observedMineralName;
    }

    /**
     * Gets the preparation details.
     *
     * @return the preparation details
     */
    public String getPreparationDetails() {
        return preparationDetails;
    }

    /**
     * Gets the lab details.
     *
     * @return the lab details
     */
    public String getLabDetails() {
        return labDetails;
    }

    /**
     * Gets the analytical method.
     *
     * @return the analytical method
     */
    public String getAnalyticalMethod() {
        return analyticalMethod;
    }

    /**
     * Gets the observed property.
     *
     * @return the observed property
     */
    public String getObservedProperty() {
        return observedProperty;
    }

    /**
     * Gets the analyte name.
     *
     * @return the analyte name
     */
    public String getAnalyteName() {
        return analyteName;
    }

    /**
     * Gets the analyte value.
     *
     * @return the analyte value
     */
    public String getAnalyteValue() {
        return analyteValue;
    }

    /**
     * Gets the uom.
     *
     * @return the uom
     */
    public String getUom() {
        return uom;
    }

    /**
     * Prints out the internal fields of this record
     */
    @Override
    public String toString() {
        return "YilgarnLocSpecimenRecords [serviceName=" + serviceName
                + ", date=" + date + ", observedMineralName="
                + observedMineralName + ", preparationDetails="
                + preparationDetails + ", labDetails=" + labDetails
                + ", analyticalMethod=" + analyticalMethod
                + ", observedProperty=" + observedProperty + ", analyteName="
                + analyteName + ", analyteValue=" + analyteValue + ", uom="
                + uom + "]";
    }


}

package org.auscope.portal.gsml;

/**
 * A simplified/denormalised view of a Yilgarn laterite geochemistry sa:LocatedSpecimen feature
 * @author Josh Vote
 *
 */
public class YilgarnLocatedSpecimenRecord {
    /** The list of related observations associated with this located specimen*/
    private YilgarnObservationRecord[] relatedObservations;
    /** The material class of this located specimen*/
    private String materialClass;

    /**
     * Creates a new instance of this class
     * @param relatedObservations The list of related observations associated with this located specimen
     * @param materialClass The material class of this located specimen
     */
    public YilgarnLocatedSpecimenRecord(
            YilgarnObservationRecord[] relatedObservations, String materialClass) {
        this.relatedObservations = relatedObservations;
        this.materialClass = materialClass;
    }

    /**
     * Gets the list of related observations associated with this located specimen
     * @return
     */
    public YilgarnObservationRecord[] getRelatedObservations() {
        return relatedObservations;
    }

    /**
     * Gets the material class of this located specimen
     * @return
     */
    public String getMaterialClass() {
        return materialClass;
    }



}

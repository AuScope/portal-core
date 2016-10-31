package org.auscope.portal.core.services.responses.vocab;

/**
 * A highly simplified representation of a SKOS 'Concept'.
 *
 * @author Josh Vote
 */
public class Concept {

    /** The urn. */
    private String urn;

    /** The href. */
    private boolean href;

    /** The label. */
    private String label;

    /** The preferred label. */
    private String preferredLabel;

    /** The definition. */
    private String definition;

    /** The broader. */
    private Concept[] broader;

    /** The narrower. */
    private Concept[] narrower;

    /** The related. */
    private Concept[] related;

    /**
     * Creates a new Concept, empty for all but it's URN.
     *
     * @param urn
     *            the urn
     */
    public Concept(String urn) {
        this(urn, false);
    }

    /**
     * Creates a new Concept, empty for all but it's URN.
     *
     * @param urn
     *            The unique ID for this concept
     * @param href
     *            if set this Concept is only a 'pointer' to concept
     */
    public Concept(String urn, boolean href) {
        this.urn = urn;
        this.label = "";
        this.definition = "";
        this.href = href;
        this.preferredLabel = "";
        this.broader = new Concept[0];
        this.narrower = new Concept[0];
        this.related = new Concept[0];
    }

    /**
     * Gets the rdf:label attached to this concept (if any).
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the rdf:label attached to this concept (if any).
     *
     * @param label
     *            the new label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Gets the skos:prefLabel attached to this concept (if any).
     *
     * @return the preferred label
     */
    public String getPreferredLabel() {
        return preferredLabel;
    }

    /**
     * Sets the skos:prefLabel attached to this concept (if any).
     *
     * @param preferredLabel
     *            the new preferred label
     */
    public void setPreferredLabel(String preferredLabel) {
        this.preferredLabel = preferredLabel;
    }

    /**
     * Gets every concept which is "broader" than this concept.
     *
     * @return the broader
     */
    public Concept[] getBroader() {
        return broader;
    }

    /**
     * Sets every concept which is "broader" than this concept.
     *
     * @param broader
     *            the new broader
     */
    public void setBroader(Concept[] broader) {
        this.broader = broader;
    }

    /**
     * Gets every concept which is "narrower" than this concept.
     *
     * @return the narrower
     */
    public Concept[] getNarrower() {
        return narrower;
    }

    /**
     * Sets every concept which is "broader" than this concept.
     *
     * @param narrower
     *            the new narrower
     */
    public void setNarrower(Concept[] narrower) {
        this.narrower = narrower;
    }

    /**
     * Gets the unique URN for this individual.
     *
     * @return the urn
     */
    public String getUrn() {
        return urn;
    }

    /**
     * Gets every concept which is "related" to this concept.
     *
     * @return the related
     */
    public Concept[] getRelated() {
        return related;
    }

    /**
     * Sets every concept which is "related" to this concept.
     *
     * @param related
     *            the new related
     */
    public void setRelated(Concept[] related) {
        this.related = related;
    }

    /**
     * Gets whether this instance is a fully populated concept or just an empty reference to the URN of the actual concept.
     *
     * @return true, if is href
     */
    public boolean isHref() {
        return href;
    }

    /**
     * Gets the definition of this concept.
     *
     * @return the definition
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * Sets the definition of this concept.
     *
     * @param definition
     *            the new definition
     */
    public void setDefinition(String definition) {
        this.definition = definition;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Concept) {
            return this.equals((Concept) obj);
        } else if (obj instanceof String) {
            return this.equals((String) obj);
        } else {
            return super.equals(obj);
        }
    }

    /**
     * Compares 2 named individuals based on their URN.
     *
     * @param concept
     *            the concept
     * @return true, if successful
     */
    public boolean equals(Concept concept) {
        if (concept == null) {
            return false;
        }
        return this.equals(concept.urn);
    }

    /**
     * Compares a string with this individuals urn.
     *
     * @param myUrn
     *            the urn
     * @return true, if successful
     */
    public boolean equals(String myUrn) {
        if (this.urn != null && myUrn != null) {
            return this.urn.equals(myUrn);
        } else {
            return this.urn == myUrn;
        }
    }

    @Override
    public int hashCode() {
        if (this.urn == null) {
            return 0;
        }
        return this.urn.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Concept [urn=" + urn + ", href=" + href + ", label=" + label
                + ", preferredLabel=" + preferredLabel + ", definition="
                + definition + "]";
    }
}

package org.auscope.portal.server.domain.vocab;


/**
 * Highly simplified representation of a rdf:Description element
 * @author Josh Vote
 *
 */
public class Description {
    private String urn;
    private boolean isHref;
    private Description[] narrower;
    private Description[] broader;
    private Description[] related;
    private Description[] topConcepts;

    /**
     * Creates a new description which will eventually be populated
     * @param urn
     */
    public Description(String urn) {
        this(urn, false);
    }

    /**
     * Creates a new Description which can optionally be used as placeholder for a URN
     * @param urn
     */
    public Description(String urn, boolean isHref) {
        this.urn = urn;
        this.isHref = isHref;
        this.narrower = new Description[0];
        this.broader = new Description[0];
        this.related = new Description[0];
        this.topConcepts = new Description[0];
    }

    /**
     * Gets every description that is defined as being narrower
     * @return
     */
    public Description[] getNarrower() {
        return narrower;
    }
    /**
     * Sets every description that is defined as being narrower
     * @param narrower
     */
    public void setNarrower(Description[] narrower) {
        this.narrower = narrower;
    }
    /**
     * Gets every description that is defined as being broader
     * @return
     */
    public Description[] getBroader() {
        return broader;
    }
    /**
     * Sets every description that is defined as being broader
     * @param broader
     */
    public void setBroader(Description[] broader) {
        this.broader = broader;
    }
    /**
     * Gets every description that is defined as being related
     * @return
     */
    public Description[] getRelated() {
        return related;
    }
    /**
     * Sets every description that is defined as being related
     * @param related
     */
    public void setRelated(Description[] related) {
        this.related = related;
    }
    /**
     * Gets every description that is defined as being a top concept of this description
     * @return
     */
    public Description[] getTopConcepts() {
        return topConcepts;
    }
    /**
     * Sets every description that is defined as being a top concept of this description
     * @param topConcepts
     */
    public void setTopConcepts(Description[] topConcepts) {
        this.topConcepts = topConcepts;
    }
    /**
     * Gets the URN of this description
     * @return
     */
    public String getUrn() {
        return urn;
    }

    /**
     * If set this Description is purely a placeholder reference (by urn) to another
     * Description element
     * @return
     */
    public boolean isHref() {
        return isHref;
    }

    @Override
    public String toString() {
        return "Description [urn=" + urn + ", isHref=" + isHref + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Description) {
            return this.equals((Description) obj);
        } else if (obj instanceof String) {
            return this.equals((String) obj);
        } else {
            return super.equals(obj);
        }
    }

    /**
     * Compares 2 Description's based on their URN
     * @return
     */
    public boolean equals(Description description) {
        if (description == null) {
            return false;
        }
        return this.equals(description.urn);
    }

    /**
     * Compares a string with this individuals urn
     * @param urn
     * @return
     */
    public boolean equals(String urn) {
        if (this.urn != null && urn != null) {
            return this.urn.equals(urn);
        } else {
            return this.urn == urn;
        }
    }



}

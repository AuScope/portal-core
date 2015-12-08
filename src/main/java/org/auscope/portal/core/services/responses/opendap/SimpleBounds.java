package org.auscope.portal.core.services.responses.opendap;

import java.io.Serializable;

/**
 * Represents a simple numerical bounds.
 *
 * @author vot002
 * @version $Id$
 */
public class SimpleBounds implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The from. */
    private double from;

    /** The to. */
    private double to;

    /**
     * The minimum value of this bounds.
     * 
     * @return the minimum value
     */
    public double getFrom() {
        return from;
    }

    /**
     * The maximum value of this bounds.
     *
     * @return the maximum value
     */
    public double getTo() {
        return to;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SimpleBounds [from=" + from + ", to=" + to + "]";
    }

    /**
     * Instantiates a new simple bounds.
     *
     * @param from
     *            the from
     * @param to
     *            the to
     */
    public SimpleBounds(double from, double to) {
        super();
        this.from = from;
        this.to = to;
    }

}

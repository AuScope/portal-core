package org.auscope.portal.server.domain.opendap;

import java.io.Serializable;

/**
 * Represents a simple numerical bounds
 * @author vot002
 *
 */
public class SimpleBounds implements Serializable {

	private static final long serialVersionUID = 1L;
	private double from;
    private double to;

    /**
     * The minimum value of this bounds
     * @return
     */
    public double getFrom() {
        return from;
    }
    /**
     * The maximum value of this bounds
     * @return
     */
    public double getTo() {
        return to;
    }

    @Override
    public String toString() {
        return "SimpleBounds [from=" + from + ", to=" + to + "]";
    }


    public SimpleBounds(double from, double to) {
        super();
        this.from = from;
        this.to = to;
    }


}

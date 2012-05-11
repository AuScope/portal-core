package org.auscope.portal.server.domain.wcs;

/**
 * Represents a Web Coverage Service resolution in both X and Y directions
 * @author Josh Vote
 *
 */
public class Resolution {
    /** resolution in x direction*/
    private double x;
    /** resolution in y direction*/
    private double y;


    /**
     * Creates a new Resolution
     * @param x resolution in x direction
     * @param y resolution in y direction
     */
    public Resolution(double x, double y) {
        this.x = x;
        this.y = y;
    }
    /**
     * Gets resolution in x direction
     * @return
     */
    public double getX() {
        return x;
    }
    /**
     * Sets resolution in x direction
     */
    public void setX(double x) {
        this.x = x;
    }
    /**
     * Gets resolution in y direction
     */
    public double getY() {
        return y;
    }
    /**
     * Sets resolution in y direction
     */
    public void setY(double y) {
        this.y = y;
    }


}

package org.auscope.portal.core.services.responses.wcs;

/**
 * Represents a Web Coverage Service resolution in both X and Y directions
 * 
 * @author Josh Vote
 *
 */
public class Resolution {
    /** resolution in x direction */
    private double x;
    /** resolution in y direction */
    private double y;

    /**
     * Creates a new Resolution
     * 
     * @param x
     *            resolution in x direction
     * @param y
     *            resolution in y direction
     */
    public Resolution(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets resolution in x direction
     * 
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

    /**
     * Prints the contents of this object
     */
    @Override
    public String toString() {
        return "Resolution [x=" + x + ", y=" + y + "]";
    }

    /**
     * Tests equality based on the x/y values of this resolution (can only test against other Resolution objects).
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Resolution) {
            Resolution r = (Resolution) o;

            return this.x == r.x && this.y == r.y;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Creates a hashcode based on X/Y values
     */
    @Override
    public int hashCode() {
        return new Double(x).hashCode() ^ new Double(y).hashCode();
    }

}

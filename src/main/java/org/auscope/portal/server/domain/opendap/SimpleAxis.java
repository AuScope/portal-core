package org.auscope.portal.server.domain.opendap;

/**
 * Represents a simplified 1 dimensional NUMERICAL axis
 * @author vot002
 *
 */
public class SimpleAxis extends ViewVariable {

	private static final long serialVersionUID = 1L;

	public static final String TYPE_STRING = "axis";

    private SimpleBounds dimensionBounds;
    private SimpleBounds valueBounds;

    public SimpleAxis(String name, String dataType, String units,
            SimpleBounds dimensionBounds, SimpleBounds valueBounds) {
        super();
        this.type = TYPE_STRING;
        this.name = name;
        this.dimensionBounds = dimensionBounds;
        this.dataType = dataType;
        this.units = units;
        this.valueBounds = valueBounds;
    }

    /**
     * Gets the bounds of this dimension in datapoints
     * @return
     */
    public SimpleBounds getDimensionBounds() {
        return dimensionBounds;
    }

    /**
     * Sets the bounds of this dimension in datapoints
     * @param dimensionLength
     */
    public void setDimensionBounds(SimpleBounds dimensionBounds) {
        this.dimensionBounds = dimensionBounds;
    }

    /**
     * Gets the upper and lower bounds of the VALUES contained in this axis
     * @return
     */
    public SimpleBounds getValueBounds() {
        return valueBounds;
    }

    /**
     * Sets the upper and lower bounds of the VALUES contained in this axis
     * @return
     */
    public void setValueBounds(SimpleBounds valueBounds) {
        this.valueBounds = valueBounds;
    }


}

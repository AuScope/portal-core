package org.auscope.portal.server.domain.opendap;

import java.util.Arrays;

/**
 * Represents a simplified grid variable
 * @author vot002
 *
 */
public class SimpleGrid extends ViewVariable {

    public static final String TYPE_STRING = "grid";
    
    private ViewVariable[] axes;
    
    /**
     * Gets the list of axes that make up this gridded variable
     * @return
     */
    public ViewVariable[] getAxes() {
        return axes;
    }

    /**
     * Sets the list of axes that make up this gridded variable
     * @param axes
     */
    public void setAxes(ViewVariable[] axes) {
        this.axes = axes;
    }

    public SimpleGrid(String name, String dataType, String units, 
            ViewVariable[] axes) {
        super();
        this.name = name;
        this.dataType = dataType;
        this.units = units;
        this.type = TYPE_STRING;
        this.axes = axes;
    }

    public String toString() {
        return "SimpleGrid [axes=" + Arrays.toString(axes) + ", type=" + type
                + ", dataType=" + dataType + ", name=" + name + ", units="
                + units + "]";
    }
}

package org.auscope.portal.core.services.responses.opendap;

import java.util.Arrays;

/**
 * Represents a simplified grid variable
 * 
 * @author vot002
 *
 */
public class SimpleGrid extends AbstractViewVariable {

    private static final long serialVersionUID = 1L;

    public static final String TYPE_STRING = "grid";

    private AbstractViewVariable[] axes;

    /**
     * Gets the list of axes that make up this gridded variable
     * 
     * @return
     */
    public AbstractViewVariable[] getAxes() {
        return axes;
    }

    /**
     * Sets the list of axes that make up this gridded variable
     * 
     * @param axes
     */
    public void setAxes(AbstractViewVariable[] axes) {
        this.axes = axes;
    }

    public SimpleGrid(String name, String dataType, String units,
            AbstractViewVariable[] axes) {
        super();
        this.name = name;
        this.dataType = dataType;
        this.units = units;
        this.type = TYPE_STRING;
        this.axes = axes;
    }

    @Override
    public String toString() {
        return "SimpleGrid [axes=" + Arrays.toString(axes) + ", type=" + type
                + ", dataType=" + dataType + ", name=" + name + ", units="
                + units + "]";
    }
}

package org.auscope.portal.server.domain.opendap;

import java.io.Serializable;

/**
 * Represents a simplification of the ucar.nc2.Variable that can be passed between view and controller.
 * @author vot002
 *
 */
public abstract class ViewVariable implements Serializable {
    
    /**
     * This should be overridden in any sub classes.
     */
    protected String type = "undefined";
    
    /**
     * The unique (within parent group) name of this variable
     */
    protected String name;

    /**
     * The string based representation of the type of this view variable
     */
    protected String dataType;
    /**
     * The units that this variable is expressed in (purely descriptive)
     */
    protected String units;
    
    /**
     * The name of the parent group (or empty string representing the root group) that encapsulates this variable
     */
    protected String parentGroupName;
    
    /**
     * Gets the name of the parent group (or empty string representing the root group) that encapsulates this variable
     * @return
     */
    public String getParentGroupName() {
        return parentGroupName;
    }
    
    /**
     * Sets the name of the parent group (or empty string representing the root group) that encapsulates this variable
     * @param parentGroupName
     */
    public void setParentGroupName(String parentGroupName) {
        this.parentGroupName = parentGroupName;
    }
    
    /**
     * Returns a string identifier of the type of variable this instance represents
     * @return
     */
    public String getType() {
        return type;
    }
    
    /**
     * The unique (within parent group) name of this variable
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * The unique (within parent group) name of this variable
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The string based representation of the type of this view variable
     * @return the dataType
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * The string based representation of the type of this view variable
     * @param dataType the dataType to set
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * The units that this variable is expressed in (purely descriptive)
     * @return the units
     */
    public String getUnits() {
        return units;
    }

    /**
     * The units that this variable is expressed in (purely descriptive)
     * @param units the units to set
     */
    public void setUnits(String units) {
        this.units = units;
    }
}

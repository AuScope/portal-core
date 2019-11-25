package org.auscope.portal.core.services.csw;

/**
 * Highly simplified version of a DQ_GriddedDataPositionalAccuracy element
 * @author Josh Vote
 *
 */
public class GriddedDataPositionalAccuracy {
    private String nameOfMeasure;
    private String unitOfMeasure;
    private String value;
    
    
    public GriddedDataPositionalAccuracy() {
        super();
    }
    
    public String getNameOfMeasure() {
        return nameOfMeasure;
    }
    
    public void setNameOfMeasure(String nameOfMeasure) {
        this.nameOfMeasure = nameOfMeasure;
    }
    
    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }
    
    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
}

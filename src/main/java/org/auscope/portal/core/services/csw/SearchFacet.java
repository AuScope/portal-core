package org.auscope.portal.core.services.csw;

/**
 * Represents a typed search that can be applied to a CSW service.
 * @author Josh Vote (CSIRO)
 *
 * @param <T>
 */
public class SearchFacet<T> {

    public enum Comparison {
        GreaterThan,
        LessThan,
        Equal
    }

    private T value;
    private String field;
    private Comparison comparison;

    public SearchFacet(T value, String field, Comparison comparison) {
        super();
        this.value = value;
        this.field = field;
        this.comparison = comparison;
    }

    public T getValue() {
        return value;
    }
    public void setValue(T value) {
        this.value = value;
    }
    public String getField() {
        return field;
    }
    public void setField(String field) {
        this.field = field;
    }
    public Comparison getComparison() {
        return comparison;
    }
    public void setComparison(Comparison comparison) {
        this.comparison = comparison;
    }
}

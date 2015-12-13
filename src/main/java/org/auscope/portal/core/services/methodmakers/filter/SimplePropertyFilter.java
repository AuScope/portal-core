package org.auscope.portal.core.services.methodmakers.filter;

/**
 * Simple class for generating a filter based on a single property/value pair
 * 
 * @author Josh Vote
 */
public class SimplePropertyFilter extends AbstractFilter {

    /** The XPath defining the property to filter against */
    private String property;
    /** The value to be used in the filter */
    private String value;

    /**
     * Creates a new instance
     * 
     * @param property
     *            The XPath defining the property to filter against
     * @param value
     *            The value to be used in the filter
     */
    public SimplePropertyFilter(String property, String value) {
        this.property = property;
        this.value = value;
    }

    /**
     * Overridden method
     * 
     * @see AbstractFilter
     */
    @Override
    public String getFilterStringAllRecords() {
        return this.generateFilter(this.generatePropertyIsEqualToFragment(property, value));
    }

    /**
     * Overridden method
     * 
     * @see AbstractFilter
     */
    @Override
    public String getFilterStringBoundingBox(FilterBoundingBox bbox) {
        return this.generateFilter(
                this.generateAndComparisonFragment(
                        this.generatePropertyIsEqualToFragment(property, value),
                        this.generateBboxFragment(bbox, null)));

    }

}

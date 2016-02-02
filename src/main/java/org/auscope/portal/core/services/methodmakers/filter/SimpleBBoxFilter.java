package org.auscope.portal.core.services.methodmakers.filter;

/**
 * Represents a simple OGC:Filter that can only make queries based upon bounding box on the 'default' geometry field.
 */
public class SimpleBBoxFilter extends AbstractFilter {
    @Override
    public String getFilterStringAllRecords() {
        return "";
    }

    @Override
    public String getFilterStringBoundingBox(FilterBoundingBox bbox) {
        if (bbox == null)
            return getFilterStringAllRecords();

        return this.generateFilter(this.generateBboxFragment(bbox, null));
    }
}

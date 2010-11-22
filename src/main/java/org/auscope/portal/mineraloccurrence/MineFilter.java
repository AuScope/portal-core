package org.auscope.portal.mineraloccurrence;

import org.auscope.portal.server.domain.filter.AbstractFilter;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;

/**
 * Class that represents ogc:Filter markup for er:mine queries
 *
 * @author Mat Wyatt
 * @version $Id$
 */
public class MineFilter extends AbstractFilter {
    private String filterFragment;

    /**
     * Given a mine name, this object will build a filter to a wild card search
     * for mine names
     *
     * @param mineName
     *            the main name
     */
    public MineFilter(String mineName) {
        // Check the NON-Feature Chained name - faster!
        if (mineName != null && mineName.length() > 0) {
        	// Geoserver bug - cant search on duplicate attributes -
        	// once resolved delete fragment and change to this: SISS-912
        	// this.filterFragment = this.generatePropertyIsLikeFragment("er:specification/er:Mine/gml:name", mineName);
            this.filterFragment = this.generatePropertyIsLikeFragment("er:specification/er:Mine/er:mineName/er:MineName/er:mineName", mineName);
        }
        // Ensure a MFO query always returns a type mine!
        else {
            this.filterFragment = this.generatePropertyIsLikeFragment("er:specification/er:Mine/gml:name", "*");
        }
    }

    public String getFilterStringAllRecords() {
        return this.generateFilter(this.filterFragment);
    }

    public String getFilterStringBoundingBox(FilterBoundingBox bbox) {
        return this.generateFilter(
                this.generateAndComparisonFragment(
                        this.generateBboxFragment(bbox, "er:location"),
                        this.filterFragment));
    }

}

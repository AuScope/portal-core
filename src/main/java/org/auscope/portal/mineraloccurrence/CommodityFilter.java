package org.auscope.portal.mineraloccurrence;

import org.auscope.portal.server.domain.filter.AbstractFilter;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;

/**
 * User: Michael Stegherr
 * Date: 23/03/2009
 * Time: 1:59:02 PM
 * @version $Id$
 */
public class CommodityFilter extends AbstractFilter {

    private String filterFragment;

    public CommodityFilter(String commodityName) {
        if (commodityName.length() > 0) 
            this.filterFragment = this.generatePropertyIsEqualToFragment("er:commodityName", commodityName);
        else
            this.filterFragment = "";
    }

    
    public String getFilterStringAllRecords() {
        return this.generateFilter(filterFragment);
    }

    public String getFilterStringBoundingBox(FilterBoundingBox bbox) {
        return this.generateFilter(
                this.generateAndComparisonFragment(
                        this.generateBboxFragment(bbox, "er:occurrence/er:MiningFeatureOccurrence/er:location"), 
                        this.filterFragment));
    }
    
}

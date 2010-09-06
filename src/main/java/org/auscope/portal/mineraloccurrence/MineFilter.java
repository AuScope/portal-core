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
        if (mineName == null || mineName.isEmpty())
            this.filterFragment = "";
        else
            this.filterFragment = this.generatePropertyIsLikeFragment("er:mineName/er:MineName/er:mineName", mineName);
    }

    public String getFilterStringAllRecords() {
        return this.generateFilter(this.filterFragment);
    }

    public String getFilterStringBoundingBox(FilterBoundingBox bbox) {
        return this.generateFilter(
                this.generateAndComparisonFragment(
                        this.generateBboxFragment(bbox, "er:occurrence/er:MiningFeatureOccurrence/er:location"), 
                        this.filterFragment));
    }

}

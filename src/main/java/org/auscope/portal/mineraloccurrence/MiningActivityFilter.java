package org.auscope.portal.mineraloccurrence;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.domain.filter.AbstractFilter;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;

/**
 * @author Mathew Wyatt
 * 
 * @version $Id$
 */
public class MiningActivityFilter extends AbstractFilter {
    private List<Mine> associatedMines;
    private String startDate;
    private String endDate;
    private String oreProcessed;
    private String producedMaterial;
    private String cutOffGrade;
    private String production;
    
    // -------------------------------------------------------------- Constants
    
    /** Log object for this class. */
    protected final Log log = LogFactory.getLog(getClass());
    
    
    // ----------------------------------------------------------- Constructors
    
    public MiningActivityFilter(List<Mine> associatedMines,
                                String startDate,
                                String endDate,
                                String oreProcessed,
                                String producedMaterial,
                                String cutOffGrade,
                                String production) {
        this.associatedMines = associatedMines;
        this.startDate = startDate;
        this.endDate = endDate;
        this.oreProcessed = oreProcessed;
        this.producedMaterial = producedMaterial;
        this.cutOffGrade = cutOffGrade;
        this.production = production;
    }

    // --------------------------------------------------------- Public Methods
    
    @Override
    public String getFilterStringAllRecords() {
        return this.generateFilter(this.generateFilterFragment());
    }

    @Override
    public String getFilterStringBoundingBox(FilterBoundingBox bbox) {
        
        return this.generateFilter(
                this.generateAndComparisonFragment(
                        this.generateBboxFragment(bbox, "er:occurrence/er:MiningFeatureOccurrence/er:location"), 
                        this.generateFilterFragment()));
    }

    
    // -------------------------------------------------------- Private Methods
    private String generateFilterFragment() {
        List<String> mineFragments = new ArrayList<String>();
        List<String> parameterFragments = new ArrayList<String>();
        
        int z = 0;
        for (Mine mine : this.associatedMines) {

            log.debug((++z) + " : " + mine.getMineNameURI());

            int relActCount = mine.getRelatedActivities().size();
            log.debug("___Number of mine related activities: " + relActCount);

            int y = 0;
            for (String s : mine.getRelatedActivities()) {
                log.debug("___" + (++y) + " : " + s);
                
                mineFragments.add(this.generatePropertyIsEqualToFragment("gml:name", s));
            }
        }
 
        if(this.startDate.length() > 0)
            parameterFragments.add(this.generatePropertyIsGreaterThanOrEqualTo("er:activityDuration/gml:TimePeriod/gml:begin/gml:TimeInstant/gml:timePosition", this.startDate));
        
        if(this.endDate.length() > 0)
            parameterFragments.add(this.generatePropertyIsLessThanOrEqualTo("er:activityDuration/gml:TimePeriod/gml:end/gml:TimeInstant/gml:timePosition", this.endDate));
        
        if(this.oreProcessed.length() > 0)
            parameterFragments.add(this.generatePropertyIsGreaterThan("er:oreProcessed/gsml:CGI_NumericValue/gsml:principalValue", this.oreProcessed));
        
        if(this.producedMaterial.length() > 0)
            parameterFragments.add(this.generatePropertyIsEqualToFragment("er:producedMaterial/er:Product/er:productName/gsml:CGI_TermValue/gsml:value", this.producedMaterial));
        
        if(this.cutOffGrade.length() > 0)
            parameterFragments.add(this.generatePropertyIsGreaterThan("er:producedMaterial/er:Product/er:grade/gsml:CGI_NumericValue/gsml:principalValue", this.cutOffGrade));
        
        if(this.production.length() > 0)
            parameterFragments.add(this.generatePropertyIsGreaterThan("er:producedMaterial/er:Product/er:production/gsml:CGI_NumericValue/gsml:principalValue", this.production));
        
        return this.generateAndComparisonFragment(
                this.generateOrComparisonFragment(mineFragments.toArray(new String[mineFragments.size()])),
                this.generateAndComparisonFragment(parameterFragments.toArray(new String[parameterFragments.size()])));
    }

    
}

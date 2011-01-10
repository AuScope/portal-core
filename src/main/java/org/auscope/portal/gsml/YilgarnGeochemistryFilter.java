package org.auscope.portal.gsml;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.domain.filter.AbstractFilter;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;

/**
 * @author 
 * 
 * @version $Id: YilgarnGeochemistryFilter.java 1233 2010-10-20   $
 */

public class YilgarnGeochemistryFilter extends AbstractFilter{
	private String rockLithology;
    private String weatherLithology;
    private String geologicName;
    
// -------------------------------------------------------------- Constants
    
    /** Log object for this class. */
    protected final Log log = LogFactory.getLog(getClass());
    
    
    // ----------------------------------------------------------- Constructors
    
    public YilgarnGeochemistryFilter(String geologicName, String rockLithology,
                                String weatherLithology) {
    	this.geologicName = geologicName;
        this.rockLithology = rockLithology;
        this.weatherLithology = weatherLithology;
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
                        this.generateBboxFragment(bbox, "gsml:occurrence/gsml:MappedFeature/gsml:shape"), 
                        this.generateFilterFragment()));
    }

    
    // -------------------------------------------------------- Private Methods
    private String generateFilterFragment() {
        List<String> parameterFragments = new ArrayList<String>();
        if(this.geologicName.length() > 0)
        	parameterFragments.add(this.generatePropertyIsLikeFragment("gml:name", this.geologicName));
        	
        if(this.rockLithology.length() > 0)
            parameterFragments.add(this.generatePropertyIsLikeFragment("gsml:composition/gsml:CompositionPart/gsml:material/gsml:RockMaterial/gsml:lithology/@xlink:href", this.rockLithology));
        
        if(this.weatherLithology.length() > 0)
            parameterFragments.add(this.generatePropertyIsLikeFragment("gsml:weatheringCharacter/gsml:WeatheringDescription/gsml:weatheringProduct/gsml:RockMaterial/gsml:lithology/@xlink:href", this.weatherLithology));
        
        
        
        return this.generateAndComparisonFragment(
                this.generateAndComparisonFragment(parameterFragments.toArray(new String[parameterFragments.size()])));
    }


}

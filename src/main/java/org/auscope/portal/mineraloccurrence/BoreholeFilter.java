package org.auscope.portal.mineraloccurrence;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.domain.filter.AbstractFilter;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;

/**
 * @author Tannu Gupta
 * 
 * @version $Id: YilgarnGeochemistryFilter.java 1233 2010-10-20   $
 */

public class BoreholeFilter extends AbstractFilter{	

	
		private String boreholeName;
	    private String custodian;
	    private String dateOfDrilling;
	    private List<String> restrictToIDList;
	    
	// -------------------------------------------------------------- Constants
	    
	    /** Log object for this class. */
	    protected final Log log = LogFactory.getLog(getClass());
	    
	    
	    // ----------------------------------------------------------- Constructors
	    
	    public BoreholeFilter(String boreholeName, String custodian,
	                          String dateOfDrilling, List<String> restrictToIDList) {
	    	this.boreholeName = boreholeName;
	        this.custodian = custodian;
	        this.dateOfDrilling = dateOfDrilling;
	        this.restrictToIDList = restrictToIDList;
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
	                        this.generateBboxFragment(bbox, "gsml:collarLocation/gsml:BoreholeCollar/gsml:location"), 
	                        this.generateFilterFragment()));
	    }

	    
	    // -------------------------------------------------------- Private Methods
	    private String generateFilterFragment() {
	        List<String> parameterFragments = new ArrayList<String>();
	        if(this.boreholeName.length() > 0)
	        	parameterFragments.add(this.generatePropertyIsLikeFragment("gml:name", this.boreholeName));
	        	
	        if(this.custodian.length() > 0)
	            parameterFragments.add(this.generatePropertyIsLikeFragment("gsml:indexData/gsml:BoreholeDetails/gsml:coreCustodian/@xlink:title", this.custodian));
	        
	        if(this.dateOfDrilling.length() > 0)
	            parameterFragments.add(this.generatePropertyIsLikeFragment("gsml:indexData/gsml:BoreholeDetails/gsml:dateOfDrilling", this.dateOfDrilling));
	        
	        if (this.restrictToIDList != null && this.restrictToIDList.size() > 0) {
	            List<String> idFragments = new ArrayList<String>();
	            for (String id : restrictToIDList) {
	                if (id != null && id.length() > 0) {
	                    //TODO - Uncomment this when http://jira.codehaus.org/browse/GEOT-3522 is fixed
	                    //idFragments.add(this.generatePropertyIsEqualToFragment("gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']", id));
	                    idFragments.add(this.generatePropertyIsEqualToFragment("gml:name[1]", id));
	                }
	            }
	            parameterFragments.add(this.generateOrComparisonFragment(idFragments.toArray(new String[idFragments.size()])));
	        }
	        
	        return this.generateAndComparisonFragment(
	                this.generateAndComparisonFragment(parameterFragments.toArray(new String[parameterFragments.size()])));
	    }


	


}

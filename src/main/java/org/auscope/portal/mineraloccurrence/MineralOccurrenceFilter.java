package org.auscope.portal.mineraloccurrence;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.domain.filter.AbstractFilter;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;

/**
 * Class that represents ogc:Filter markup for er:MineralOccurrence queries
 *
 * @author Jarek Sanders
 * @version $Id$
 */
@SuppressWarnings("deprecation")
public class MineralOccurrenceFilter extends AbstractFilter {

    // TODO: Include ENDOWMENT when GeoServers accept this element
    //       (...you may just just put it b/w RESERVE and RESOURCE)


    public enum MeasureTypes { ENDOWMENT, RESERVE, RESOURCE, ANY, NONE }

    /** Log object for this class. */
    protected final Log log = LogFactory.getLog(getClass());

    private String commodityName;
    private MeasureTypes measureType;
    private String minOreAmount;
    private String minOreAmountUOM;
    private String minCommodityAmount;
    private String minCommodityAmountUOM;
    private int paramsCount;
    private String filterStr;

    /**
     * C'tor
     */
    public MineralOccurrenceFilter( String commodityName,
                                    String measureType,
                                    String minOreAmount,
                                    String minOreAmountUOM,
                                    String minCommodityAmount,
                                    String minCommodityAmountUOM )
    {
        this.commodityName         = commodityName == null ? "" : commodityName;
        this.minOreAmount          = minOreAmount;
        this.minOreAmountUOM       = minOreAmountUOM;
        this.minCommodityAmount    = minCommodityAmount;
        this.minCommodityAmountUOM = minCommodityAmountUOM;

        this.measureType           = getMeasureType(measureType);
        this.paramsCount           = getParameterCount();
        this.filterStr             = makeFilter();
    }

    @Override
    public String getFilterStringAllRecords() {
        return this.generateFilter(filterStr);
    }

    @Override
    public String getFilterStringBoundingBox(FilterBoundingBox bbox) {


        return this.generateFilter(
                this.generateAndComparisonFragment(
                        this.generateBboxFragment(bbox, "gsml:shape"),
                        this.filterStr));
    }

    /**
     * Constructs WFS MineralOccurence filter query string based
     * on user parameters.
     * @return Filter query string for sending as a POST request
     */
    private String makeFilter() {

        // Case 1. Get All Query
        if ( (this.measureType == MeasureTypes.NONE) && commodityName.isEmpty()) {
            return "";
        }

        // Case 2. Commodities Only Query
        else if ( (this.measureType == MeasureTypes.NONE) && (!commodityName.isEmpty()) ) {
            return this.generatePropertyIsEqualToFragment("gsml:specification/er:MineralOccurrence/er:commodityDescription/er:Commodity/er:commodityName", commodityName);
        }

        // Case 3. Amount Only Query
        else if ( (this.measureType != MeasureTypes.NONE) && commodityName.isEmpty() ) {

            // Single measure
            if (this.measureType != MeasureTypes.ANY) {
                if (paramsCount == 0) {
                    return this.generatePropertyIsLikeFragment("gsml:specification/er:MineralOccurrence/er:oreAmount/"+ getMeasureTypeTag(this.measureType) +"/er:measureDetails/er:CommodityMeasure/er:commodityOfInterest/er:Commodity/er:commodityName", "*");
                } else if(paramsCount > 0) {
                    return this.generateParametersFragment(getMeasureTypeTag(this.measureType));
                } else {
                    return "";
                }
            }
            // Multiple measures - ANY
            else if (this.measureType == MeasureTypes.ANY) {
                List<String> fragments = new ArrayList<String>();

                if (paramsCount == 0) {
                    for (MeasureTypes measure : EnumSet.range(MeasureTypes.RESERVE, MeasureTypes.RESOURCE)) {
                        fragments.add(this.generatePropertyIsLikeFragment("gsml:specification/er:MineralOccurrence/er:oreAmount/"+ getMeasureTypeTag(measure) +"/er:measureDetails/er:CommodityMeasure/er:commodityOfInterest/er:Commodity/er:commodityName", "*"));
                    }
                } else if(paramsCount > 0) {
                    for (MeasureTypes measure : EnumSet.range(MeasureTypes.RESERVE, MeasureTypes.RESOURCE)) {
                        fragments.add(this.generateParametersFragment(getMeasureTypeTag(measure)));
                    }
                }

                return this.generateOrComparisonFragment(fragments.toArray(new String[fragments.size()]));
            }
        }

        // Case 4. Commodity + Amount Query
        else if ( (this.measureType != MeasureTypes.NONE) && !commodityName.isEmpty() ) {

            // Single Measure
            if (this.measureType != MeasureTypes.ANY) {
                if (this.paramsCount == 0) {
                    return this.generatePropertyIsEqualToFragment("gsml:specification/er:MineralOccurrence/er:oreAmount/"+ getMeasureTypeTag(this.measureType) +"/er:measureDetails/er:CommodityMeasure/er:commodityOfInterest/er:Commodity/er:commodityName", commodityName);
                } else {
                    return this.generateCommodityAndParametersFragment(commodityName,getMeasureTypeTag(this.measureType));
                }
            }
            // Multiple Measures - ANY
            else if (this.measureType == MeasureTypes.ANY) {
                List<String> fragments = new ArrayList<String>();
                if (this.paramsCount == 0) {
                    for (MeasureTypes measure : EnumSet.range(MeasureTypes.RESERVE, MeasureTypes.RESOURCE)) {
                        fragments.add(this.generatePropertyIsEqualToFragment("gsml:specification/er:MineralOccurrence/er:oreAmount/"+getMeasureTypeTag(measure)+"/er:measureDetails/er:CommodityMeasure/er:commodityOfInterest/er:Commodity/er:commodityName", commodityName));
                    }
                } else if (this.paramsCount > 0) {
                    for (MeasureTypes measure : EnumSet.range(MeasureTypes.RESERVE, MeasureTypes.RESOURCE)) {
                        fragments.add(this.generateCommodityAndParametersFragment(commodityName, getMeasureTypeTag(measure)));
                    }
                }

                return this.generateOrComparisonFragment(fragments.toArray(new String[fragments.size()]));
            }
        }

        return "";
    }


    /*
     * Appends search commodity and amount parameters entered by user
     */
    private String generateCommodityAndParametersFragment(String commodityName,  String measure) {
        List<String> fragments = new ArrayList<String>();

        fragments.add(this.generatePropertyIsEqualToFragment("gsml:specification/er:MineralOccurrence/er:oreAmount/"+measure+"/er:measureDetails/er:CommodityMeasure/er:commodityOfInterest/er:Commodity/er:commodityName", commodityName));

        if (!this.minOreAmount.isEmpty()) {
            fragments.add(this.generatePropertyIsGreaterThanOrEqualTo("gsml:specification/er:MineralOccurrence/er:oreAmount/"+measure+"/er:ore/gsml:CGI_NumericValue/gsml:principalValue", this.minOreAmount));
        }
        if (!this.minOreAmountUOM.isEmpty()) {
            fragments.add(this.generatePropertyIsEqualToFragment("gsml:specification/er:MineralOccurrence/er:oreAmount/"+measure+"/er:ore/gsml:CGI_NumericValue/gsml:principalValue/@uom", this.minOreAmountUOM));
        }
        if (!this.minCommodityAmount.isEmpty()) {
            fragments.add(this.generatePropertyIsGreaterThanOrEqualTo("gsml:specification/er:MineralOccurrence/er:oreAmount/"+measure+"/er:measureDetails/er:CommodityMeasure/er:commodityAmount/gsml:CGI_NumericValue/gsml:principalValue", this.minCommodityAmount));
        }
        if (!this.minCommodityAmountUOM.isEmpty()) {
            fragments.add(this.generatePropertyIsEqualToFragment("gsml:specification/er:MineralOccurrence/er:oreAmount/"+measure+"/er:measureDetails/er:CommodityMeasure/er:commodityAmount/gsml:CGI_NumericValue/gsml:principalValue/@uom", this.minCommodityAmountUOM));
        }

        return this.generateAndComparisonFragment(fragments.toArray(new String[fragments.size()]));
    }


    /*
     * Generates a large <ogc:And> fragment consisiting of all the parameters specified
     */
    private String generateParametersFragment(String measure) {
        List<String> fragments = new ArrayList<String>();

        if (!this.minOreAmount.isEmpty()) {
            fragments.add(this.generatePropertyIsGreaterThanOrEqualTo("gsml:specification/er:MineralOccurrence/er:oreAmount/"+measure+"/er:ore/gsml:CGI_NumericValue/gsml:principalValue", this.minOreAmount));
        }
        if (!this.minOreAmountUOM.isEmpty()) {
            fragments.add(this.generatePropertyIsEqualToFragment("gsml:specification/er:MineralOccurrence/er:oreAmount/"+measure+"/er:ore/gsml:CGI_NumericValue/gsml:principalValue/@uom", this.minOreAmountUOM));
        }
        if (!this.minCommodityAmount.isEmpty()) {
            fragments.add(this.generatePropertyIsEqualToFragment("gsml:specification/er:MineralOccurrence/er:oreAmount/"+measure+"/er:measureDetails/er:CommodityMeasure/er:commodityAmount/gsml:CGI_NumericValue/gsml:principalValue", this.minCommodityAmount));
        }
        if (!this.minCommodityAmountUOM.isEmpty()) {
            fragments.add(this.generatePropertyIsEqualToFragment("gsml:specification/er:MineralOccurrence/er:oreAmount/"+measure+"/er:measureDetails/er:CommodityMeasure/er:commodityAmount/gsml:CGI_NumericValue/gsml:principalValue/@uom", this.minCommodityAmountUOM));
        }

        return this.generateAndComparisonFragment(fragments.toArray(new String[fragments.size()]));
    }


    /*
     * Converts measure type string displayed in combobox into respective
     * MeasureTypes enumerated value
     */
    private MeasureTypes getMeasureType(String measureType) {

    	if (measureType == null) {
    		return MeasureTypes.NONE;
    	}
    	
        if (measureType.equals("Resource")) {
            return MeasureTypes.RESOURCE;
        } else if (measureType.equals("Reserve")) {
            return MeasureTypes.RESERVE;
        } else if (measureType.equals("Endowment")) {
            return MeasureTypes.ENDOWMENT;
        } else if (measureType.equals("Any")) {
            return MeasureTypes.ANY;
        } else {
            return MeasureTypes.NONE;
        }
    }


    /*
     * Returns number of Amount Measure parameters submitted by user
     */
    private int getParameterCount() {
        int count = 0;

        if ((this.minOreAmount != null) && (!this.minOreAmount.isEmpty()))
            count++;
        if ((this.minOreAmountUOM != null) && (!this.minOreAmountUOM.isEmpty()))
            count++;
        if ((this.minCommodityAmount != null) && (!this.minCommodityAmount.isEmpty()))
            count++;
        if ((this.minCommodityAmountUOM != null) && (!this.minCommodityAmountUOM.isEmpty()))
            count++;

        log.debug("Returning count: " + count);
        return count;
    }


    /**
     * Returns the measure type tag for building up the filter property name
     * @param type the measure type
     * @return measure type tag as a String
     */
    public String getMeasureTypeTag(MeasureTypes type) {
        switch (type) {
            case ENDOWMENT : return "er:Endowment";
            case RESOURCE  : return "er:Resource";
            case RESERVE   : return "er:Reserve";
            default        : return ""; // Shouldn't go here
        }
    }


}

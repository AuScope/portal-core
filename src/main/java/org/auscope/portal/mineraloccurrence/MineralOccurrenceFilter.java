package org.auscope.portal.mineraloccurrence;

import java.util.Collection;
import java.util.EnumSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class that represents ogc:Filter markup for er:MineralOccurrence queries
 * 
 * @author Jarek Sanders
 * @version $Id$
 */
public class MineralOccurrenceFilter implements IFilter {
 
    // TODO: Include ENDOWMENT when GeoServers accept this element 
    //       (...you may just just put it b/w RESERVE and RESOURCE) 
    public enum MeasureTypes { ENDOWMENT, RESERVE, RESOURCE, ANY, NONE }
    
    /** Log object for this class. */
    protected final Log log = LogFactory.getLog(getClass());
    
    private Collection<Commodity> commodityOccurrences;
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
    public MineralOccurrenceFilter( Collection<Commodity> commodities,
                                    String measureType,
                                    String minOreAmount,
                                    String minOreAmountUOM,
                                    String minCommodityAmount,
                                    String minCommodityAmountUOM ) 
    {    
        this.commodityOccurrences  = commodities;
        this.minOreAmount          = minOreAmount;
        this.minOreAmountUOM       = minOreAmountUOM;
        this.minCommodityAmount    = minCommodityAmount;
        this.minCommodityAmountUOM = minCommodityAmountUOM;
        
        this.measureType           = getMeasureType(measureType);
        this.paramsCount           = getParameterCount();
        this.filterStr             = makeFilter();
    }

    /**
     * Returns WFS MineralOccurence filter query string 
     * @return Filter query string for sending as a POST request
     */
    public String getFilterString() {
        return this.filterStr;
    }
    
    /**
     * Constructs WFS MineralOccurence filter query string based 
     * on user parameters.
     * @return Filter query string for sending as a POST request
     */
    private String makeFilter() {        
        
        int commoditiesCount = commodityOccurrences.size();        
        log.debug("Number of commodities: " + commoditiesCount);

        StringBuilder queryString = new StringBuilder();
        
        // Case 1. Get All Query
        if ( (this.measureType == MeasureTypes.NONE) && commodityOccurrences.isEmpty()) {
            queryString.setLength(0);
        }
        
        // Case 2. Commodities Only Query
        else if ( (this.measureType == MeasureTypes.NONE) && (commoditiesCount > 0 ) ) {
            for( Commodity commodity : commodityOccurrences ) {
                addPropertyIsLike( queryString,
                                   "er:commodityDescription/@xlink:href",
                                   commodity.getName() );
            }                    

            if (commoditiesCount > 1)
                addOperatorOR(queryString);

            addExpressionFILTER(queryString);
        }

        // Case 3. Amount Only Query
        else if ( (this.measureType != MeasureTypes.NONE) && commodityOccurrences.isEmpty() ) {
            
            // Single measure
            if (this.measureType != MeasureTypes.ANY) {
                if (paramsCount == 0) {
                    addPropertyIsLike( queryString,
                                       "er:oreAmount/"+ getMeasureTypeTag(this.measureType) +"/er:measureDetails/er:CommodityMeasure/er:commodityOfInterest/@xlink:href",
                                       "*" );
                } else if(paramsCount > 0) {
                    addParameters( queryString, getMeasureTypeTag(this.measureType) );
                }
                addExpressionFILTER(queryString);
            } 
            // Multiple measures - ANY 
            else if (this.measureType == MeasureTypes.ANY) {
                if (paramsCount == 0) {  
                    for (MeasureTypes measure : EnumSet.range(MeasureTypes.RESERVE, MeasureTypes.RESOURCE)) {
                        addPropertyIsLike( queryString,
                                           "er:oreAmount/"+ getMeasureTypeTag(measure) +"/er:measureDetails/er:CommodityMeasure/er:commodityOfInterest/@xlink:href",
                                           "*" );
                    }
                } else if(paramsCount > 0) {  
                    for (MeasureTypes measure : EnumSet.range(MeasureTypes.RESERVE, MeasureTypes.RESOURCE)) { 
                        addParameters(queryString, getMeasureTypeTag(measure));
                    }
                }
                addOperatorOR(queryString);
                addExpressionFILTER(queryString);                
            }
        }
        
        // Case 4. Commodity + Amount Query
        else if ( (this.measureType != MeasureTypes.NONE) && !commodityOccurrences.isEmpty() ) {
            // Single Measure
            if (this.measureType != MeasureTypes.ANY) {
                if (this.paramsCount == 0) {
                    for( Commodity commodity : commodityOccurrences ) {
                        addPropertyIsEqualTo( queryString,
                                              "er:oreAmount/"+ getMeasureTypeTag(this.measureType) +"/er:measureDetails/er:CommodityMeasure/er:commodityOfInterest/@xlink:href",
                                              commodity.getName() );
                    }
                } else if (this.paramsCount > 0) {
                    for( Commodity commodity : commodityOccurrences ) {
                        addCommodityAndParameters( queryString,
                                                   commodity,
                                                   getMeasureTypeTag(this.measureType));
                    }
                }
                if (commoditiesCount > 1)
                    addOperatorOR(queryString);
                
                addExpressionFILTER(queryString);                                
            }
            // Multiple Measures - ANY 
            else if (this.measureType == MeasureTypes.ANY) {
                if (this.paramsCount == 0) {
                    for( Commodity commodity : commodityOccurrences ) {
                        for (MeasureTypes measure : EnumSet.range(MeasureTypes.RESERVE, MeasureTypes.RESOURCE)) {
                            addPropertyIsEqualTo( queryString,
                                                  "er:oreAmount/"+getMeasureTypeTag(measure)+"/er:measureDetails/er:CommodityMeasure/er:commodityOfInterest/@xlink:href",            
                                                  commodity.getName() );
                        }
                    }
                } else if (this.paramsCount > 0) {
                    for (MeasureTypes measure : EnumSet.range(MeasureTypes.RESERVE, MeasureTypes.RESOURCE)) {
                        for( Commodity commodity : commodityOccurrences ) {
                            addCommodityAndParameters( queryString,
                                                       commodity,
                                                       getMeasureTypeTag(measure));
                        }
                    }
                }
                addOperatorOR(queryString);
                addExpressionFILTER(queryString);
            }            
        }                
        
        return queryString.toString();
    }
    
    
    /*
     * Appends ogc:PropertyIsEqualTo comparison statement 
     */
    private void addPropertyIsEqualTo(StringBuilder sb, String property, String value) {
        sb.append("        <ogc:PropertyIsEqualTo>\n");
        sb.append("          <ogc:PropertyName>"+ property +"</ogc:PropertyName>\n");            
        sb.append("          <ogc:Literal>" + value + "</ogc:Literal>\n");
        sb.append("        </ogc:PropertyIsEqualTo>\n");                       
    }
    
    
    /*
     * Appends ogc:PropertyIsLike comparison statement 
     */
    private void addPropertyIsLike(StringBuilder sb, String property, String value) {        
        sb.append("        <ogc:PropertyIsLike wildCard=\"*\" singleChar=\"#\" escapeChar=\"!\">\n");
        sb.append("          <ogc:PropertyName>"+ property +"</ogc:PropertyName>\n");            
        sb.append("          <ogc:Literal>"+ value +"</ogc:Literal>\n");
        sb.append("        </ogc:PropertyIsLike>\n");        
    }
    
    
    /*
     * Appends search commodity and amount parameters entered by user 
     */
    private void addCommodityAndParameters(StringBuilder iBuffer, Commodity commodity,  String measure) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("        <ogc:PropertyIsEqualTo>\n");
        sb.append("          <ogc:PropertyName>er:oreAmount/"+measure+"/er:measureDetails/er:CommodityMeasure/er:commodityOfInterest/@xlink:href</ogc:PropertyName>\n");            
        sb.append("          <ogc:Literal>" + commodity.getName() + "</ogc:Literal>\n");
        sb.append("        </ogc:PropertyIsEqualTo>\n");
        
        if (!this.minOreAmount.isEmpty()) {
            sb.append("        <ogc:PropertyIsGreaterThanOrEqualTo>\n");
            sb.append("          <ogc:PropertyName>er:oreAmount/"+measure+"/er:ore/gsml:CGI_NumericValue/gsml:principalValue</ogc:PropertyName>\n");
            sb.append("          <ogc:Literal>"+this.minOreAmount+"</ogc:Literal>\n");
            sb.append("        </ogc:PropertyIsGreaterThanOrEqualTo>\n");                
        }
        if (!this.minOreAmountUOM.isEmpty()) {
            sb.append("        <ogc:PropertyIsEqualTo>\n");
            sb.append("          <ogc:PropertyName>er:oreAmount/"+measure+"/er:ore/gsml:CGI_NumericValue/gsml:principalValue/@uom</ogc:PropertyName>\n");
            sb.append("          <ogc:Literal>"+this.minOreAmountUOM+"</ogc:Literal>\n");
            sb.append("        </ogc:PropertyIsEqualTo>\n");                
        }
        if (!this.minCommodityAmount.isEmpty()) {
            sb.append("        <ogc:PropertyIsGreaterThanOrEqualTo>\n");
            sb.append("          <ogc:PropertyName>er:oreAmount/"+measure+"/er:measureDetails/er:CommodityMeasure/er:commodityAmount/gsml:CGI_NumericValue/gsml:principalValue</ogc:PropertyName>\n");
            sb.append("          <ogc:Literal>"+this.minCommodityAmount+"</ogc:Literal>\n");
            sb.append("        </ogc:PropertyIsGreaterThanOrEqualTo>\n");                
        }
        if (!this.minCommodityAmountUOM.isEmpty()) {
            sb.append("        <ogc:PropertyIsEqualTo>\n");
            sb.append("          <ogc:PropertyName>er:oreAmount/"+measure+"/er:measureDetails/er:CommodityMeasure/er:commodityAmount/gsml:CGI_NumericValue/gsml:principalValue/@uom</ogc:PropertyName>\n");
            sb.append("          <ogc:Literal>"+this.minCommodityAmountUOM+"</ogc:Literal>\n");
            sb.append("        </ogc:PropertyIsEqualTo>\n");
        }        
        addOperatorAND(sb);
        iBuffer.append(sb);
    }
    
    
    /*
     * Appends amount search parameters entered by user
     */
    private void addParameters(StringBuilder iBuffer, String measure) {
        StringBuilder sb = new StringBuilder(); 
        if (!this.minOreAmount.isEmpty()) {
            sb.append("        <ogc:PropertyIsGreaterThanOrEqualTo>\n");
            sb.append("          <ogc:PropertyName>er:oreAmount/"+measure+"/er:ore/gsml:CGI_NumericValue/gsml:principalValue</ogc:PropertyName>\n");
            sb.append("          <ogc:Literal>"+this.minOreAmount+"</ogc:Literal>\n");
            sb.append("        </ogc:PropertyIsGreaterThanOrEqualTo>\n");                
        }
        if (!this.minOreAmountUOM.isEmpty()) {
            sb.append("        <ogc:PropertyIsEqualTo>\n");
            sb.append("          <ogc:PropertyName>er:oreAmount/"+measure+"/er:ore/gsml:CGI_NumericValue/gsml:principalValue/@uom</ogc:PropertyName>\n");
            sb.append("          <ogc:Literal>"+this.minOreAmountUOM+"</ogc:Literal>\n");
            sb.append("        </ogc:PropertyIsEqualTo>\n");                
        }
        if (!this.minCommodityAmount.isEmpty()) {
            sb.append("        <ogc:PropertyIsGreaterThanOrEqualTo>\n");
            sb.append("          <ogc:PropertyName>er:oreAmount/"+measure+"/er:measureDetails/er:CommodityMeasure/er:commodityAmount/gsml:CGI_NumericValue/gsml:principalValue</ogc:PropertyName>\n");
            sb.append("          <ogc:Literal>"+this.minCommodityAmount+"</ogc:Literal>\n");
            sb.append("        </ogc:PropertyIsGreaterThanOrEqualTo>\n");                
        }
        if (!this.minCommodityAmountUOM.isEmpty()) {
            sb.append("        <ogc:PropertyIsEqualTo>\n");
            sb.append("          <ogc:PropertyName>er:oreAmount/"+measure+"/er:measureDetails/er:CommodityMeasure/er:commodityAmount/gsml:CGI_NumericValue/gsml:principalValue/@uom</ogc:PropertyName>\n");
            sb.append("          <ogc:Literal>"+this.minCommodityAmountUOM+"</ogc:Literal>\n");
            sb.append("        </ogc:PropertyIsEqualTo>\n");
        }        
        if (this.paramsCount > 1)
            addOperatorAND(sb);
        
        iBuffer.append(sb);
    }
    
    
    /*
     * Embrace given string with <ogc:Filter> </ogc:Filter> tags
     */ 
    private void addExpressionFILTER(StringBuilder sb) {
        sb.insert(0,"    <ogc:Filter>\n");
        sb.append("    </ogc:Filter>\n");
    }    
    
    
    /*
     * Embrace given string with <ogc:Or> </ogc:Or> logical operator tags
     */
    private void addOperatorAND(StringBuilder sb) {
        sb.insert(0,"      <ogc:And>\n");
        sb.append("      </ogc:And>\n");
    }    

    
    /*
     * Embrace given string with <ogc:Or> </ogc:Or> logical operator tags
     */
    private void addOperatorOR(StringBuilder sb) {
        sb.insert(0,"      <ogc:Or>\n");
        sb.append("      </ogc:Or>\n");
    }

    
    /*
     * Converts measure type string displayed in combobox into respective 
     * MeasureTypes enumerated value  
     */ 
    private MeasureTypes getMeasureType(String measureType) {
        
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

package org.auscope.portal.mineraloccurrence;

import java.util.Collection;

/**
 * User: Michael Stegherr
 * Date: 26/03/2009
 * Time: 5:18:28 PM
 */
public class MineralOccurrenceFilter implements IFilter {
    // TODO: endowment to be commented in again, when data model (mineraloccurrence ml) includes this
    public enum MeasureType { /*ENDOWMENT,*/ RESOURCE, RESERVE, ANY }
    
    private Collection<Commodity> commodities;
    private MeasureType measureType;
    private String minOreAmount;
    private String minOreAmountUOM;
    private String minCommodityAmount;
    private String minCommodityAmountUOM;
    private String cutOffGrade;
    private String cutOffGradeUOM;

    public MineralOccurrenceFilter(Collection<Commodity> commodities,
                                   String measureType,
                                   String minOreAmount,
                                   String minOreAmountUOM,
                                   String minCommodityAmount,
                                   String minCommodityAmountUOM,
                                   String cutOffGrade,
                                   String cutOffGradeUOM) {

        this.commodities = commodities;
        this.minOreAmount          = minOreAmount;
        this.minOreAmountUOM       = minOreAmountUOM;
        this.minCommodityAmount    = minCommodityAmount;
        this.minCommodityAmountUOM = minCommodityAmountUOM;
        this.cutOffGrade           = cutOffGrade;
        this.cutOffGradeUOM        = cutOffGradeUOM;
        
        // parse strings from combobox into enum values
        // TODO: endowment to be commented in again, when data model (mineraloccurrence ml) includes this
/*        if(measureType.compareTo("Endowment") == 0)
            this.measureType = MeasureType.ENDOWMENT;
        else */if(measureType.compareTo("Resource") == 0)
            this.measureType = MeasureType.RESOURCE;
        else if(measureType.compareTo("Reserve") == 0)
            this.measureType = MeasureType.RESERVE;
        else // anything else will query for every measure type
            this.measureType = MeasureType.ANY;
    }

    /**
     * Build the query string based on given properties
     * @return String for sending as a POST request
     */
    public String getFilterString() {                  //TODO: this sucks! use geotools api to build queries...
        StringBuffer queryString = new StringBuffer();

        queryString.append("<ogc:Filter xmlns:er=\"urn:cgi:xmlns:GGIC:EarthResource:1.1\"\n" +
                "        xmlns:gsml=\"urn:cgi:xmlns:CGI:GeoSciML:2.0\"\n" +
                "        xmlns:ogc=\"http://www.opengis.net/ogc\"\n" +
                "        xmlns:gml=\"http://www.opengis.net/gml\"\n" +
                "        xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
                "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");

        if(checkMany())
            queryString.append("<ogc:And>\n");
        
        if(this.commodities != null)
        {
            // if commodities, filter that
            if( commodities.size()!=0 )
            {
                if( commodities.size()>1 )
                    queryString.append("            <ogc:Or>\n");
                
                for( Commodity commodity : commodities ) {
                    queryString.append("                <ogc:PropertyIsEqualTo>\n" +
                                       "                    <ogc:PropertyName>gml:name</ogc:PropertyName>\n" +
                                       "                    <ogc:Literal>"+commodity.getSource()+"</ogc:Literal>\n" +
                                       "                </ogc:PropertyIsEqualTo>\n");
                }
                
                if( commodities.size()>1 )
                    queryString.append("            </ogc:Or>\n");
            }
        }
        
        if(!this.minOreAmount.equals(""))
        {
            if(this.measureType == MeasureType.ANY)
            {
                queryString.append("                 <ogc:Or>\n");
                
                for(MeasureType t : MeasureType.values())
                    if(t!=MeasureType.ANY)
                        queryString.append(createOreAmountQuery(t));
                
                queryString.append("                 </ogc:Or>\n");
            }
            else
                queryString.append(createOreAmountQuery(this.measureType));
        }

        if(!this.minCommodityAmount.equals(""))
        {
            if(this.measureType == MeasureType.ANY)
            {
                queryString.append("                 <ogc:Or>\n");
                
                for(MeasureType t : MeasureType.values())
                    if(t!=MeasureType.ANY)
                        queryString.append(createCommodityAmountQuery(t));
                
                queryString.append("                 </ogc:Or>\n");
            }
            else
                queryString.append(createCommodityAmountQuery(this.measureType));            
        }

        if(!this.cutOffGrade.equals(""))
        {
            if(this.measureType == MeasureType.ANY)
            {
                queryString.append("                 <ogc:Or>\n");
                
                for(MeasureType t : MeasureType.values())
                    if(t!=MeasureType.ANY)
                        queryString.append(createCutOffGradeQuery(t));
                
                queryString.append("                 </ogc:Or>\n");
            }
            else
                queryString.append(createCutOffGradeQuery(this.measureType));            
        }

        if(checkMany())
            queryString.append("</ogc:And>\n");

        queryString.append("        </ogc:Filter>\n");

        return queryString.toString();

    }

    /**
     * Returns a PropertyIsGreaterThan query for ore amount
     * @param type the desired measure type
     * @return the string for using in a filter query
     */
    private String createOreAmountQuery(MeasureType type)
    {
        return "            <ogc:And>\n" +
               "                <ogc:PropertyIsEqualTo>\n" +
               "                    <ogc:PropertyName>er:oreAmount/" + getMeasureTypeTag(type) +
               "/er:ore/gsml:CGI_NumericValue/gsml:principalValue/@uom</ogc:PropertyName>\n" +
               "                    <ogc:Literal>"+this.minOreAmountUOM+"</ogc:Literal>\n" +
               "                </ogc:PropertyIsEqualTo>\n" +
               "                <ogc:PropertyIsGreaterThan>\n" +
               "                    <ogc:PropertyName>er:oreAmount/" + getMeasureTypeTag(type) +
               "/er:ore/gsml:CGI_NumericValue/gsml:principalValue</ogc:PropertyName>\n" +
               "                    <ogc:Literal>"+this.minOreAmount+"</ogc:Literal>\n" +
               "                </ogc:PropertyIsGreaterThan>\n" +
               "            </ogc:And>\n";
    }
    
    /**
     * Returns a PropertyIsGreaterThan query for commodity amount
     * @param type the desired measure type
     * @return the string for using in a filter query
     */
    private String createCommodityAmountQuery(MeasureType type)
    {
        return "                <ogc:And>" +
               "                   <ogc:PropertyIsEqualTo>\n" +
               "                      <ogc:PropertyName>er:oreAmount/" + getMeasureTypeTag(type) +
               "/er:measureDetails/er:CommodityMeasure/er:commodityAmount/gsml:CGI_NumericValue/gsml:principalValue/@uom</ogc:PropertyName>\n" +
               "                      <ogc:Literal>"+this.minCommodityAmountUOM+"</ogc:Literal>\n" +
               "                   </ogc:PropertyIsEqualTo>" +
               "                   <ogc:PropertyIsGreaterThan>\n" +
               "                      <ogc:PropertyName>er:oreAmount/" + getMeasureTypeTag(type) +            
               "/er:measureDetails/er:CommodityMeasure/er:commodityAmount/gsml:CGI_NumericValue/gsml:principalValue</ogc:PropertyName>\n" +
               "                      <ogc:Literal>"+this.minCommodityAmount+"</ogc:Literal>\n" +
               "                   </ogc:PropertyIsGreaterThan>" +
               "                </ogc:And>";
    }
    
    /**
     * Returns a PropertyIsGreaterThan query for cut off grade
     * @param type the desired measure type
     * @return the string for using in a filter query
     */
    private String createCutOffGradeQuery(MeasureType type)
    {
        return "                <ogc:And>" +
               "                   <ogc:PropertyIsEqualTo>\n" +
               "                      <ogc:PropertyName>er:oreAmount/" + getMeasureTypeTag(type) +
               "/er:measureDetails/er:CommodityMeasure/er:cutOffGrade/gsml:CGI_NumericValue/gsml:principalValue/@uom</ogc:PropertyName>\n" +
               "                      <ogc:Literal>"+this.cutOffGradeUOM+"</ogc:Literal>\n" +
               "                   </ogc:PropertyIsEqualTo>" +
               "                   <ogc:PropertyIsGreaterThan>\n" +
               "                      <ogc:PropertyName>er:oreAmount/" + getMeasureTypeTag(type) +            
               "/er:measureDetails/er:CommodityMeasure/er:cutOffGrade/gsml:CGI_NumericValue/gsml:principalValue</ogc:PropertyName>\n" +
               "                      <ogc:Literal>"+this.cutOffGrade+"</ogc:Literal>\n" +
               "                   </ogc:PropertyIsGreaterThan>" +
               "                </ogc:And>";
    }

    /**
     * Returns the measure type tag for building up the filter property name
     * @param type the measure type
     * @return measure type tag as a String
     */
    public String getMeasureTypeTag(MeasureType type) {
        switch (type) {
        // TODO: endowment to be commented in again, when data model (mineraloccurrence ml) includes this
//            case ENDOWMENT:
//                return "er:Endowment";
                
            case RESOURCE:
                return "er:Resource";
                
            case RESERVE:
                return "er:Reserve";
    
            default:
                // TODO shouldn't go there, error handling?
                return "";
        }
    }

    /**
     * Checks if more than one query parameters have a value.
     * @return true, if more than one parameter is found
     */
    private boolean checkMany() {
        int howManyHaveaValue = 0;

        if(this.commodities != null) {
            if(!this.commodities.isEmpty())
                howManyHaveaValue++;
        }
        if(!this.minOreAmount.equals(""))
            howManyHaveaValue++;
        if(!this.minCommodityAmount.equals(""))
            howManyHaveaValue++;
        if(!this.cutOffGrade.equals(""))
            howManyHaveaValue++;

        if(howManyHaveaValue >= 2)
            return true;

        return false;
    }

}

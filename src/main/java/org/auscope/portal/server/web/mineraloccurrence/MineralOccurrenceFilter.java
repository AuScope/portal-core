package org.auscope.portal.server.web.mineraloccurrence;

import java.util.Collection;

/**
 * User: Michael Stegherr
 * Date: 26/03/2009
 * Time: 5:18:28 PM
 */
public class MineralOccurrenceFilter implements IFilter {
    // endowment commented out for now, because it isn't implemented yet
    public enum MeasureType { /*ENDOWMENT,*/ RESOURCE, RESERVE, ANY }
    
    private Collection<String> names;
    private MeasureType measureType;
    private String minOreAmount;
    private String minCommodityAmount;
    private String cutOffGrade;
    private String minOreAmountUOM;

    public MineralOccurrenceFilter(Collection<String> names,
                                   String measureType,
                                   String minOreAmount,
                                   String minOreAmountUOM,
                                   String minCommodityAmount,
                                   String cutOffGrade) {
        this.names              = names;
        this.minOreAmount       = minOreAmount;
        this.minCommodityAmount = minCommodityAmount;
        this.cutOffGrade        = cutOffGrade;
        this.minOreAmountUOM    = minOreAmountUOM;
        
        // parse strings from combobox into enum values
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

        queryString.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<wfs:GetFeature version=\"1.1.0\" xmlns:mo=\"urn:cgi:xmlns:GGIC:MineralOccurrence:1.0\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:gsml=\"urn:cgi:xmlns:CGI:GeoSciML:2.0\"\n" +
                "        xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "        xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\" maxFeatures=\"200\">\n" +
                "    <wfs:Query typeName=\"mo:MineralOccurrence\">\n" +
                "        <ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">\n");

        if(checkMany())
            queryString.append("<ogc:And>\n");
        
        if(this.names != null)
        {
            String[] namesArray = this.names.toArray(new String[names.size()]);

            // if names, filter that
            if( namesArray.length!=0 )
            {
                if( namesArray.length>1 )
                    queryString.append("            <ogc:Or>\n");
                
                for( int i=0; i<namesArray.length; i++ ) {
                    queryString.append("                <ogc:PropertyIsEqualTo>\n" +
                                       "                    <ogc:PropertyName>gml:name</ogc:PropertyName>\n" +
                                       "                    <ogc:Literal>"+namesArray[i]+"</ogc:Literal>\n" +
                                       "                </ogc:PropertyIsEqualTo>\n");
                }
                
                if( namesArray.length>1 )
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

        queryString.append("</ogc:Filter>\n" +
                "    </wfs:Query>\n" +
                "</wfs:GetFeature>");

        return queryString.toString();

    }

    /**
     * Returns a PropertyIsGreaterThan query for ore amount
     * @param type the desired measure type
     * @return the string for using in a filter query
     */
    private String createOreAmountQuery(MeasureType type)
    {
        return "                <ogc:PropertyIsGreaterThan>\n" +
               "                   <ogc:PropertyName>mo:oreAmount/" + getMeasureTypeTag(type) +
               "/mo:ore/gsml:CGI_NumericValue/gsml:principalValue</ogc:PropertyName>\n" +
               "                   <ogc:Literal>"+this.minOreAmount+"</ogc:Literal>\n" +
               "           </ogc:PropertyIsGreaterThan>";
    }
    
    /**
     * Returns a PropertyIsGreaterThan query for commodity amount
     * @param type the desired measure type
     * @return the string for using in a filter query
     */
    private String createCommodityAmountQuery(MeasureType type)
    {
        return "                <ogc:PropertyIsGreaterThan>\n" +
               "                   <ogc:PropertyName>mo:oreAmount/" + getMeasureTypeTag(type) +            
               "/mo:measureDetails/mo:CommodityMeasure/mo:commodityAmount/gsml:CGI_NumericValue/gsml:principalValue</ogc:PropertyName>\n" +
               "                   <ogc:Literal>"+this.minCommodityAmount+"</ogc:Literal>\n" +
               "           </ogc:PropertyIsGreaterThan>";
    }
    
    /**
     * Returns a PropertyIsGreaterThan query for cut off grade
     * @param type the desired measure type
     * @return the string for using in a filter query
     */
    private String createCutOffGradeQuery(MeasureType type)
    {
        return "                <ogc:PropertyIsGreaterThan>\n" +
               "                   <ogc:PropertyName>mo:oreAmount/" + getMeasureTypeTag(type) +            
               "/mo:measureDetails/mo:CommodityMeasure/mo:cutOffGrade/gsml:CGI_NumericValue/gsml:principalValue</ogc:PropertyName>\n" +
               "                   <ogc:Literal>"+this.cutOffGrade+"</ogc:Literal>\n" +
               "           </ogc:PropertyIsGreaterThan>";
    }

    /**
     * Returns the measure type tag for building up the filter property name
     * @param type the measure type
     * @return measure type tag as a String
     */
    public String getMeasureTypeTag(MeasureType type) {
        switch (type) {
//            case ENDOWMENT:
//                return "mo:Endowment";
                
            case RESOURCE:
                return "mo:Resource";
                
            case RESERVE:
                return "mo:Reserve";
    
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

        if(this.names != null) {
            if(!this.names.isEmpty())
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
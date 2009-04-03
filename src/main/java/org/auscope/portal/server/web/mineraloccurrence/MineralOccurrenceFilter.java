package org.auscope.portal.server.web.mineraloccurrence;

import java.util.Collection;

/**
 * User: Michael Stegherr
 * Date: 26/03/2009
 * Time: 5:18:28 PM
 */
public class MineralOccurrenceFilter implements IFilter {
    public enum MeasureType { ENDOWMENT, RESOURCE, RESERVE }
    
    private Collection<String> names;
    private MeasureType measureType;
    private String minOreAmount;
    private String minCommodityAmount;
    private String cutOffGrade;

    public MineralOccurrenceFilter(Collection<String> names,
                                   String measureType,
                                   String minOreAmount,
                                   String minCommodityAmount,
                                   String cutOffGrade) {
        this.names              = names;
        this.minOreAmount       = minOreAmount;
        this.minCommodityAmount = minCommodityAmount;
        this.cutOffGrade        = cutOffGrade;
        
        if(measureType.compareTo("Endowment") == 0)
            this.measureType = MeasureType.ENDOWMENT;
        else if(measureType.compareTo("Resource") == 0)
            this.measureType = MeasureType.RESOURCE;
        else if(measureType.compareTo("Reserve") == 0)
            this.measureType = MeasureType.RESERVE;
        else
        {
            // TODO query any?
        }
    }

    /**
     * Build the query string based on given properties
     * @return
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
                    queryString.append("            </ogc:Or>");
            }
        }
        
        // TODO implement querying ANY measure type! (use <ogc:Or>)
        if(!this.minOreAmount.equals(""))
        {
            queryString.append("                <ogc:PropertyIsGreaterThan>\n" +
                               "                   <ogc:PropertyName>mo:oreAmount/" + getMeasureTypeTag() +
                               "/mo:ore/gsml:CGI_NumericValue/gsml:principalValue</ogc:PropertyName>\n" +
                               "                   <ogc:Literal>"+this.minOreAmount+"</ogc:Literal>\n" +
                               "           </ogc:PropertyIsGreaterThan>");
        }

        // TODO implement querying ANY measure type! (use <ogc:Or>)
        if(!this.minCommodityAmount.equals(""))
        {
            queryString.append("                <ogc:PropertyIsGreaterThan>\n" +
                               "                   <ogc:PropertyName>mo:oreAmount/" + getMeasureTypeTag() +            
                               "/mo:measureDetails/mo:CommodityMeasure/mo:commodityAmount/gsml:CGI_NumericValue/gsml:principalValue</ogc:PropertyName>\n" +
                               "                   <ogc:Literal>"+this.minCommodityAmount+"</ogc:Literal>\n" +
                               "           </ogc:PropertyIsGreaterThan>");
        }

        // TODO implement querying ANY measure type! (use <ogc:Or>)
        if(!this.cutOffGrade.equals(""))
        {
            queryString.append("                <ogc:PropertyIsGreaterThan>\n" +
                               "                   <ogc:PropertyName>mo:oreAmount/" + getMeasureTypeTag() +            
                               "/mo:measureDetails/mo:CommodityMeasure/mo:cutOffGrade/gsml:CGI_NumericValue/gsml:principalValue</ogc:PropertyName>\n" +
                               "                   <ogc:Literal>"+this.cutOffGrade+"</ogc:Literal>\n" +
                               "           </ogc:PropertyIsGreaterThan>");
        }

        if(checkMany())
            queryString.append("</ogc:And>\n");

        queryString.append("</ogc:Filter>\n" +
                "    </wfs:Query>\n" +
                "</wfs:GetFeature>");

        return queryString.toString();

    }

    /**
     * Return the measure type tag for building up the filter property name
     * @return measure type tag as String
     */
    private String getMeasureTypeTag() {
        switch (this.measureType) {
            case ENDOWMENT:
                return "mo:Endowment";
                
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
     * Do more than one query parameter have a value
     * @return
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
package org.auscope.portal.mineraloccurrence;

/**
 * User: Michael Stegherr
 * Date: 23/03/2009
 * Time: 1:59:02 PM
 * @version $Id$
 */
public class CommodityFilter implements IFilter {

    private String commodityName;

    public CommodityFilter(String commodityName) {
        this.commodityName  = commodityName;
    }
    
    /**
     * Build the query string based on given properties
     * @return String for sending in a POST request
     */
    public String getFilterString() {
        StringBuffer queryString = new StringBuffer();

        queryString.append("    <ogc:Filter>\n");

        if(checkMany())
            queryString.append("    <ogc:And>\n");

        if(!this.commodityName.equals(""))
            queryString.append("      <ogc:PropertyIsEqualTo>\n" +
                               "        <ogc:PropertyName>er:commodityName</ogc:PropertyName>\n" +
                               "        <ogc:Literal>"+this.commodityName+"</ogc:Literal>\n" +
                               "      </ogc:PropertyIsEqualTo>\n");

        if(checkMany())
            queryString.append("    </ogc:And>\n");

        queryString.append("    </ogc:Filter>\n");

        return queryString.toString();
    }
    

    /**
     * Checks if more than one query parameter have a value.
     * @return true, if more than one parameter is found
     */
    private boolean checkMany() {
        int howManyHaveaValue = 0;

        if(!this.commodityName.equals(""))
            howManyHaveaValue++;

        if(howManyHaveaValue >= 2)
            return true;

        return false;
    }
}

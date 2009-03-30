package org.auscope.portal.server.web.mineraloccurrence;

/**
 * User: Michael Stegherr
 * Date: 26/03/2009
 * Time: 5:18:28 PM
 */
public class MineralOccurrenceFilter implements IFilter {
    private String commodityAmount;
    private String commodityName;
    private String cutOffGrade;

    public MineralOccurrenceFilter(String commodityAmount,
                                   String commodityName,
                                   String cutOffGrade) {
        this.commodityAmount = commodityAmount;
        this.commodityName   = commodityName;
        this.cutOffGrade     = cutOffGrade;
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
                "        xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\">\n" +
                "    <wfs:Query typeName=\"mo:MineralOccurrence\">\n" +
                "        <ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">\n");

        if(checkMany())
            queryString.append("<ogc:And>");

        if(!this.commodityAmount.equals(""))
            queryString.append("<ogc:PropertyIsGreaterThan>\n" +
                    "                    <ogc:PropertyName>mo:oreAmount/mo:Resource/mo:measureDetails/mo:CommodityMeasure/mo:commodityAmount/gsml:CGI_NumericValue/gsml:principalValue</ogc:PropertyName>\n" +
                    "                    <ogc:Literal>"+this.commodityAmount+"</ogc:Literal>\n" +
                    "                </ogc:PropertyIsGreaterThan>");

        if(!this.commodityName.equals(""))      //TODO: correct query
            queryString.append("<ogc:PropertyIsEqualTo>\n" +
                    "                    <ogc:PropertyName>mo:producedMaterial/mo:Product/mo:productName/gsml:CGI_TermValue/gsml:value</ogc:PropertyName>\n" +
                    "                    <ogc:Literal>"+this.commodityName+"</ogc:Literal>\n" +
                    "                </ogc:PropertyIsEqualTo>");

        if(!this.cutOffGrade.equals(""))      //TODO: correct query
            queryString.append("<ogc:PropertyIsGreaterThan>\n" +
                    "                    <ogc:PropertyName>mo:producedMaterial/mo:Product/mo:grade/gsml:CGI_NumericValue/gsml:principalValue</ogc:PropertyName>\n" +
                    "                    <ogc:Literal>"+this.cutOffGrade+"</ogc:Literal>\n" +
                    "                </ogc:PropertyIsGreaterThan>");

        if(checkMany())
            queryString.append("</ogc:And>");

        queryString.append("</ogc:Filter>\n" +
                "    </wfs:Query>\n" +
                "</wfs:GetFeature>");

        return queryString.toString();

    }

    /**
     * Do more than one query parameter have a value
     * @return
     */
    private boolean checkMany() {
        int howManyHaveaValue = 0;

        if(!this.commodityAmount.equals(""))
            howManyHaveaValue++;
        if(!this.commodityName.equals(""))
            howManyHaveaValue++;
        if(!this.cutOffGrade.equals(""))
            howManyHaveaValue++;

        if(howManyHaveaValue >= 2)
            return true;

        return false;
    }

}
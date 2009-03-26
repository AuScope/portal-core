package org.auscope.portal.server.web.mineraloccurrence;

/**
 * User: Mathew Wyatt
 * Date: 24/03/2009
 * Time: 9:54:28 AM
 */
public class MiningActivityFilter implements IFilter {
    private String[] associatedMines;
    private String startDate;
    private String endDate;
    private String oreProcessed;
    private String producedMaterial;
    private String cutOffGrade;
    private String production;

    public MiningActivityFilter(String[] associatedMines,
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
                "    <wfs:Query typeName=\"mo:MiningActivity\">\n" +
                "        <ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">\n");

        if(checkMany())
            queryString.append("<ogc:And>");

        if(this.associatedMines.length ==1 ) {//if there is one mine, then only put it in with no ogc:Or's
            queryString.append("<ogc:PropertyIsEqualTo>\n" +
                    "                    <ogc:PropertyName>mo:associatedMine/@xlink:href</ogc:PropertyName>\n" +
                    "                    <ogc:Literal>" + this.associatedMines[0] + "</ogc:Literal>\n" +
                    "                </ogc:PropertyIsEqualTo>");
        } else if(this.associatedMines.length > 1) {
            queryString.append("<ogc:Or>");
            for(String mineUri : this.associatedMines)
                queryString.append("<ogc:PropertyIsEqualTo>\n" +
                        "                    <ogc:PropertyName>mo:associatedMine/@xlink:href</ogc:PropertyName>\n" +
                        "                    <ogc:Literal>" + mineUri + "</ogc:Literal>\n" +
                        "                </ogc:PropertyIsEqualTo>");
            queryString.append("</ogc:Or>");
        }

        if(!this.startDate.equals(""))
            queryString.append("<ogc:PropertyIsGreaterThan>\n" +
                    "                    <ogc:PropertyName>mo:activityDuration/gml:TimePeriod/gml:begin/gml:TimeInstant/gml:timePosition</ogc:PropertyName>\n" +
                    "                    <ogc:Literal>"+ this.startDate +"</ogc:Literal>\n" +
                    "                </ogc:PropertyIsGreaterThan>");

        if(!this.endDate.equals(""))
            queryString.append("<ogc:PropertyIsLessThan>\n" +
                    "                    <ogc:PropertyName>mo:activityDuration/gml:TimePeriod/gml:end/gml:TimeInstant/gml:timePosition</ogc:PropertyName>\n" +
                    "                    <ogc:Literal>"+this.endDate+"</ogc:Literal>\n" +
                    "                </ogc:PropertyIsLessThan>");

        if(!this.oreProcessed.equals(""))
            queryString.append("<ogc:PropertyIsGreaterThan>\n" +
                    "                    <ogc:PropertyName>mo:oreProcessed/gsml:CGI_NumericValue/gsml:principalValue</ogc:PropertyName>\n" +
                    "                    <ogc:Literal>"+this.oreProcessed+"</ogc:Literal>\n" +
                    "                </ogc:PropertyIsGreaterThan>");

        if(!this.producedMaterial.equals(""))
            queryString.append("<ogc:PropertyIsEqualTo>\n" +
                    "                    <ogc:PropertyName>mo:producedMaterial/mo:Product/mo:productName/gsml:CGI_TermValue/gsml:value</ogc:PropertyName>\n" +
                    "                    <ogc:Literal>"+this.producedMaterial+"</ogc:Literal>\n" +
                    "                </ogc:PropertyIsEqualTo>");

        if(!this.cutOffGrade.equals(""))
            queryString.append("<ogc:PropertyIsGreaterThan>\n" +
                    "                    <ogc:PropertyName>mo:producedMaterial/mo:Product/mo:grade/gsml:CGI_NumericValue/gsml:principalValue</ogc:PropertyName>\n" +
                    "                    <ogc:Literal>"+this.cutOffGrade+"</ogc:Literal>\n" +
                    "                </ogc:PropertyIsGreaterThan>");

        if(!this.production.equals(""))
            queryString.append("<ogc:PropertyIsGreaterThan>\n" +
                    "                    <ogc:PropertyName>mo:producedMaterial/mo:Product/mo:production/gsml:CGI_NumericValue/gsml:principalValue</ogc:PropertyName>\n" +
                    "                    <ogc:Literal>"+this.production+"</ogc:Literal>\n" +
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

        if(this.associatedMines.length >= 1)
            howManyHaveaValue++;
        if(!this.startDate.equals(""))
            howManyHaveaValue++;
        if(!this.endDate.equals(""))
            howManyHaveaValue++;
        if(!this.oreProcessed.equals(""))
            howManyHaveaValue++;
        if(!this.producedMaterial.equals(""))
            howManyHaveaValue++;
        if(!this.cutOffGrade.equals(""))
            howManyHaveaValue++;
        if(!this.production.equals(""))
            howManyHaveaValue++;

        if(howManyHaveaValue >= 2)
            return true;

        return false;
    }

}

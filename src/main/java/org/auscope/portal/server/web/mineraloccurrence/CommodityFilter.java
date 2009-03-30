package org.auscope.portal.server.web.mineraloccurrence;

/**
 * User: Michael Stegherr
 * Date: 23/03/2009
 * Time: 1:59:02 PM
 */
public class CommodityFilter implements IFilter {
    private String commodityName;

    public CommodityFilter(String commodityName) {
        this.commodityName = commodityName;
    }

    public String getFilterString() {
        //if there is no name specified then just get all of them
        if(commodityName.equals("")) {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<wfs:GetFeature version=\"1.1.0\" xmlns:mo=\"urn:cgi:xmlns:GGIC:MineralOccurrence:1.0\" xmlns:wfs=\"http://www.opengis.net/wfs\"\n" +
                    "        xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "        xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\" maxFeatures=\"200\">\n" +
                    "    <wfs:Query typeName=\"mo:Commodity\"/>\n" +
                    "</wfs:GetFeature>";
        }
        //if we have a name, then build a query to find the commodities with that name
        else {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<wfs:GetFeature version=\"1.1.0\" xmlns:mo=\"urn:cgi:xmlns:GGIC:MineralOccurrence:1.0\" xmlns:wfs=\"http://www.opengis.net/wfs\"\n" +
                    "        xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "        xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\">\n" +
                    "    <wfs:Query typeName=\"mo:Commodity\">\n" +
                    "        <ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">\n" +
                    "                <ogc:PropertyIsEqualTo>\n" +
                    "                    <ogc:PropertyName>mo:commodityName</ogc:PropertyName>\n" +
                    "                    <ogc:Literal>" + commodityName + "</ogc:Literal>\n" +
                    "                </ogc:PropertyIsEqualTo>\n" +
                    "        </ogc:Filter>\n" +
                    "    </wfs:Query>\n" +
                    "</wfs:GetFeature>";
        }

    }
    
}

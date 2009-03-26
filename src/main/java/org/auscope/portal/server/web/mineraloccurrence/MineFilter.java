package org.auscope.portal.server.web.mineraloccurrence;

/**
 * User: Mathew Wyatt
 * Date: 23/03/2009
 * Time: 1:59:02 PM
 */
public class MineFilter implements IFilter {
    private String mineName;
    private String kvps;

    public MineFilter(String mineName) {
        this.mineName = mineName;
        //this.kvps = "service=WFS&version=1.1.0&request=GetFeature&typeName=mo:Mine&namespace=xmlns(mo=urn:cgi:xmlns:GGIC:MineralOccurrence:1.0)";
    }

    public String getFilterString() {
        //if there is no name specified then just get all of them
        if(mineName.equals("")) {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<wfs:GetFeature version=\"1.1.0\" xmlns:mo=\"urn:cgi:xmlns:GGIC:MineralOccurrence:1.0\" xmlns:wfs=\"http://www.opengis.net/wfs\"\n" +
                    "        xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "        xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\" maxFeatures=\"200\">\n" +
                    "    <wfs:Query typeName=\"mo:Mine\"/>\n" +
                    "</wfs:GetFeature>";
        }
        //if we have a name, then build a query to find the mines with that name
        else {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<wfs:GetFeature version=\"1.1.0\" xmlns:mo=\"urn:cgi:xmlns:GGIC:MineralOccurrence:1.0\" xmlns:wfs=\"http://www.opengis.net/wfs\"\n" +
                    "        xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "        xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\">\n" +
                    "    <wfs:Query typeName=\"mo:Mine\">\n" +
                    "        <ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">\n" +
                    "                <ogc:PropertyIsEqualTo>\n" +
                    "                    <ogc:PropertyName>mo:mineName/mo:MineName/mo:mineName</ogc:PropertyName>\n" +
                    "                    <ogc:Literal>" + mineName + "</ogc:Literal>\n" +
                    "                </ogc:PropertyIsEqualTo>\n" +
                    "        </ogc:Filter>\n" +
                    "    </wfs:Query>\n" +
                    "</wfs:GetFeature>";
        }

    }
    
}

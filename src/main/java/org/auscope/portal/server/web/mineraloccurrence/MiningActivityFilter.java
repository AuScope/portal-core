package org.auscope.portal.server.web.mineraloccurrence;

/**
 * User: Mathew Wyatt
 * Date: 24/03/2009
 * Time: 9:54:28 AM
 */
public class MiningActivityFilter implements IFilter {
    private String associatedMine;

    public MiningActivityFilter(String associatedMine) {
        this.associatedMine = associatedMine;
    }

    public String getFilterString() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<wfs:GetFeature version=\"1.1.0\" xmlns:mo=\"urn:cgi:xmlns:GGIC:MineralOccurrence:1.0\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:gsml=\"urn:cgi:xmlns:CGI:GeoSciML:2.0\"\n" +
                "        xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "        xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\">\n" +
                "    <wfs:Query typeName=\"mo:MiningActivity\">\n" +
                "        <ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">\n" +
                "                <ogc:PropertyIsEqualTo>\n" +
                "                    <ogc:PropertyName>mo:associatedMine/@xlink:href</ogc:PropertyName>\n" +
                "                    <ogc:Literal>" + this.associatedMine + "</ogc:Literal>\n" +
                "                </ogc:PropertyIsEqualTo>\n" +
                "        </ogc:Filter>\n" +
                "    </wfs:Query>\n" +
                "</wfs:GetFeature>";
    }

}

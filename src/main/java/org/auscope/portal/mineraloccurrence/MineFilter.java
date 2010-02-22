package org.auscope.portal.mineraloccurrence;

/**
 * Class that represents ogc:Filter markup for er:mine queries
 * 
 * @author Mat Wyatt
 * @version $Id$
 */
public class MineFilter implements IFilter {
    private String mineName;

    /**
     * Given a mine name, this object will build a filter 
     * to a wild card search for mine names
     *
     * @param mineName the main name
     */
    public MineFilter(String mineName) {
        this.mineName = mineName;
    }


    public String getFilterString() {
        return  "        <ogc:Filter xmlns:er=\"urn:cgi:xmlns:GGIC:EarthResource:1.1\" \n" +
                "                    xmlns:ogc=\"http://www.opengis.net/ogc\" \n" +
                "                    xmlns:gml=\"http://www.opengis.net/gml\" \n" +
                "                    xmlns:xlink=\"http://www.w3.org/1999/xlink\" \n" +
                "                    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "            <ogc:PropertyIsLike wildCard=\"*\" singleChar=\"#\" escapeChar=\"!\">\n" +
                "                <ogc:PropertyName>er:mineName/er:MineName/er:mineName</ogc:PropertyName>\n" +
                "                <ogc:Literal>" + mineName + "</ogc:Literal>\n" +
                "            </ogc:PropertyIsLike>\n" +
                "        </ogc:Filter>";

    }
    
}

package org.auscope.portal.mineraloccurrence;

/**
 * Class that represents ogc:Filter markup for er:mine queries
 * 
 * @author Mat Wyatt
 * @version $Id$
 */
public class MineFilter implements IFilter {
    private String filterStr;

    /**
     * Given a mine name, this object will build a filter 
     * to a wild card search for mine names
     *
     * @param mineName the main name
     */
    public MineFilter(String mineName) {        
        StringBuilder sb = new StringBuilder();        
        sb.append("    <ogc:Filter>\n");
        sb.append("      <ogc:PropertyIsLike wildCard=\"*\" singleChar=\"#\" escapeChar=\"!\">\n");
        sb.append("        <ogc:PropertyName>er:mineName/er:MineName/er:mineName</ogc:PropertyName>\n");
        sb.append("        <ogc:Literal>" + mineName + "</ogc:Literal>\n");        
        sb.append("      </ogc:PropertyIsLike>\n");        
        sb.append("    </ogc:Filter>\n");                
        this.filterStr = sb.toString();
    }


    public String getFilterString() {        
        return this.filterStr;
    }
    
}

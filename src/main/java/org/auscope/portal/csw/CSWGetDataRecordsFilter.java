package org.auscope.portal.csw;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.domain.filter.AbstractFilter;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;

/**
 * Represents a OGC:Filter that will fetch all WFS, WMS and WCS records from a CSW
 * @author VOT002
 *
 */
public class CSWGetDataRecordsFilter extends AbstractFilter {
	protected final Log log = LogFactory.getLog(getClass());

    private String generateFilterFragment() {
        return this.generateOrComparisonFragment(new String[] {
                this.generatePropertyIsLikeFragment("AnyText", "*wms*"),
                this.generatePropertyIsLikeFragment("AnyText", "*wfs*"),
                this.generatePropertyIsLikeFragment("AnyText", "*wcs*"),
        });
    }
    
    /**
     * Returns an ogc:filter fragment that will fetch all WFS, WMS and WCS records from a CSW
     */
    @Override
    public String getFilterStringAllRecords() {
        
        //This is a bit of a hack - unfortunately the NamespaceContext class is unsuitable here
        //as it contains no methods to iterate the contianted list of Namespaces
        HashMap<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("xmlns:ogc", "http://www.opengis.net/ogc");        
        return this.generateFilter(this.generateFilterFragment(), namespaces);
    }

    /**
     * Not implemented
     */
    @Override
    public String getFilterStringBoundingBox(FilterBoundingBox bbox) {
        
    	return null;
    }
    
}

package org.auscope.portal.core.services.csw;

import java.util.Arrays;

import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.view.ViewCSWRecordFactory;
import org.springframework.ui.ModelMap;

/**
 * Similar to ViewCSWRecordFactory but with the expectation of a GriddedCSWRecord instead
 * of a CSWRecord
 * @author Josh Vote
 *
 */
public class ViewGriddedCSWRecordFactory extends ViewCSWRecordFactory {
    
    @Override
    public ModelMap toView(CSWRecord record) {
        ModelMap map = super.toView(record);
        
        ModelMap extensions = new ModelMap();
        
        extensions.put("griddedInfo", Arrays.asList(((GriddedCSWRecord) record).getGriddedInfo()));
        extensions.put("dateStamp", ((GriddedCSWRecord) record).getDateStamp());
        
        map.put("extensions", extensions);
        
        return map;
    }
}

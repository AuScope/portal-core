package org.auscope.portal.server.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.auscope.portal.mineraloccurrence.BoreholeFilter;
import org.auscope.portal.server.domain.ogc.FilterTestUtilities;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for BoreholeFilter
 * @author Josh Vote
 *
 */
public class TestBoreholeFilter {
  
    /**
     * Tests that null params will NOT generate exceptions and are equivelant to empty params
     */
    @Test
    public void testNullOrEmptyParams() {
        
        BoreholeFilter filter = new BoreholeFilter(null, null, null, null);
        String nullFilterString = filter.getFilterStringAllRecords();
        
        filter = new BoreholeFilter("", "", "", new ArrayList<String>());
        String emptyFilterString = filter.getFilterStringAllRecords();
        
        Assert.assertEquals(emptyFilterString, nullFilterString);
    }
    
    /**
     * Tests that the ID restriction list uses CASE SENSITIVE comparisons
     * @throws Exception 
     */
    @Test
    public void testCaseSensitiveIDs() throws Exception {
        List<String> restrictedIDs = Arrays.asList("CaseSensitiveId1", "CaseSensitiveId2");
        BoreholeFilter filter = new BoreholeFilter("", "", "", restrictedIDs);
        
        String filterString = filter.getFilterStringAllRecords();
        
        Document doc = FilterTestUtilities.parsefilterStringXML(filterString);
        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/@matchCase", new String[] {"true"}, 2);
    }
    
    /**
     * Tests that the non ID string comparisons will ALWAYS use matchCase=false
     * @throws Exception 
     */
    @Test
    public void testCaseInsensitiveStrings() throws Exception {
        BoreholeFilter filter = new BoreholeFilter("boreholeName", "boreholeCustodian", null, null);
        String filterString = filter.getFilterStringAllRecords();
        
        Document doc = FilterTestUtilities.parsefilterStringXML(filterString);
        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLike/@matchCase", new String[] {"false"}, 2);
    }
}

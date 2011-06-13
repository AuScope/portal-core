package org.auscope.portal.server.domain.filter;

import org.apache.commons.lang.NotImplementedException;
import org.auscope.portal.server.domain.ogc.FilterTestUtilities;
import org.junit.Assert;
import org.junit.Test;

public class TestAbstractFilter extends AbstractFilter {

    @Override
    public String getFilterStringAllRecords() {
        throw new NotImplementedException();
    }

    @Override
    public String getFilterStringBoundingBox(FilterBoundingBox bbox) {
        throw new NotImplementedException();
    }
    
    /**
     * Tests for AUS-2072 ensuring that matchCase is FALSE by default
     */
    @Test
    public void testMatchCaseDefault() {
        Assert.assertTrue(this.generatePropertyIsEqualToFragment("foo", "bar").contains("matchCase=\"false\""));
        Assert.assertTrue(this.generatePropertyIsNotEqualTo("foo", "bar").contains("matchCase=\"false\""));
        Assert.assertTrue(this.generatePropertyIsLessThan("foo", "bar").contains("matchCase=\"false\""));
        Assert.assertTrue(this.generatePropertyIsGreaterThan("foo", "bar").contains("matchCase=\"false\""));
        Assert.assertTrue(this.generatePropertyIsLessThanOrEqualTo("foo", "bar").contains("matchCase=\"false\""));
        Assert.assertTrue(this.generatePropertyIsGreaterThanOrEqualTo("foo", "bar").contains("matchCase=\"false\""));
        Assert.assertTrue(this.generatePropertyIsLikeFragment("foo", "bar").contains("matchCase=\"false\""));
    }
    
    /**
     * Tries to parse the XML that the filters generate - exceptions will be thrown if not well formed
     * @throws Exception
     */
    @Test
    public void testWellFormedXML() throws Exception {
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsEqualToFragment("foo", "bar"));
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsNotEqualTo("foo", "bar"));
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsLessThan("foo", "bar"));
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsGreaterThan("foo", "bar"));
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsLessThanOrEqualTo("foo", "bar"));
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsGreaterThanOrEqualTo("foo", "bar"));
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsLikeFragment("foo", "bar"));
        
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsEqualToFragment("foo", "bar", true, MatchActionType.Any));
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsNotEqualTo("foo", "bar", true, MatchActionType.All));
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsLessThan("foo", "bar", true, MatchActionType.One));
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsGreaterThan("foo", "bar", true, MatchActionType.Any));
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsLessThanOrEqualTo("foo", "bar", false, MatchActionType.All));
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsGreaterThanOrEqualTo("foo", "bar", false, MatchActionType.One));
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsLikeFragment("foo", "bar",'a','b','c', true, MatchActionType.Any));
        
        FilterTestUtilities.parsefilterStringXML(this.generateAndComparisonFragment(
                this.generatePropertyIsEqualToFragment("foo", "bar"),
                this.generatePropertyIsEqualToFragment("foo", "bar"),
                this.generatePropertyIsEqualToFragment("foo", "bar"),
                this.generatePropertyIsEqualToFragment("foo", "bar")));
        FilterTestUtilities.parsefilterStringXML(this.generateOrComparisonFragment(
                this.generatePropertyIsEqualToFragment("foo", "bar"),
                this.generatePropertyIsEqualToFragment("foo", "bar"),
                this.generatePropertyIsEqualToFragment("foo", "bar"),
                this.generatePropertyIsEqualToFragment("foo", "bar")));
        FilterTestUtilities.parsefilterStringXML(this.generateNotComparisonFragment(
                this.generatePropertyIsEqualToFragment("foo", "bar")));
    }
}

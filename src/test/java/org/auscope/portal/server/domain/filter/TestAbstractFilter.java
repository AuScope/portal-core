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
        Assert.assertTrue(this.generatePropertyIsEqualToFragment("myPropertyName", "myLiteral").contains("matchCase=\"false\""));
        Assert.assertTrue(this.generatePropertyIsNotEqualTo("myPropertyName", "myLiteral").contains("matchCase=\"false\""));
        Assert.assertTrue(this.generatePropertyIsLessThan("myPropertyName", "myLiteral").contains("matchCase=\"false\""));
        Assert.assertTrue(this.generatePropertyIsGreaterThan("myPropertyName", "myLiteral").contains("matchCase=\"false\""));
        Assert.assertTrue(this.generatePropertyIsLessThanOrEqualTo("myPropertyName", "myLiteral").contains("matchCase=\"false\""));
        Assert.assertTrue(this.generatePropertyIsGreaterThanOrEqualTo("myPropertyName", "myLiteral").contains("matchCase=\"false\""));
        Assert.assertTrue(this.generatePropertyIsLikeFragment("myPropertyName", "myLiteral").contains("matchCase=\"false\""));
    }
    
    /**
     * Tries to parse the XML that the filters generate - exceptions will be thrown if not well formed
     * @throws Exception
     */
    @Test
    public void testWellFormedXML() throws Exception {
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsEqualToFragment("myPropertyName", "myLiteral"));
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsNotEqualTo("myPropertyName", "myLiteral"));
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsLessThan("myPropertyName", "myLiteral"));
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsGreaterThan("myPropertyName", "myLiteral"));
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsLessThanOrEqualTo("myPropertyName", "myLiteral"));
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsGreaterThanOrEqualTo("myPropertyName", "myLiteral"));
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsLikeFragment("myPropertyName", "myLiteral"));
        
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsEqualToFragment("myPropertyName", "myLiteral", true, MatchActionType.Any));
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsNotEqualTo("myPropertyName", "myLiteral", true, MatchActionType.All));
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsLessThan("myPropertyName", "myLiteral", true, MatchActionType.One));
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsGreaterThan("myPropertyName", "myLiteral", true, MatchActionType.Any));
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsLessThanOrEqualTo("myPropertyName", "myLiteral", false, MatchActionType.All));
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsGreaterThanOrEqualTo("myPropertyName", "myLiteral", false, MatchActionType.One));
        FilterTestUtilities.parsefilterStringXML(this.generatePropertyIsLikeFragment("myPropertyName", "myLiteral",'a','b','c', true, MatchActionType.Any));
        
        FilterTestUtilities.parsefilterStringXML(this.generateAndComparisonFragment(
                this.generatePropertyIsEqualToFragment("myPropertyName", "myLiteral"),
                this.generatePropertyIsEqualToFragment("myPropertyName", "myLiteral"),
                this.generatePropertyIsEqualToFragment("myPropertyName", "myLiteral"),
                this.generatePropertyIsEqualToFragment("myPropertyName", "myLiteral")));
        FilterTestUtilities.parsefilterStringXML(this.generateOrComparisonFragment(
                this.generatePropertyIsEqualToFragment("myPropertyName", "myLiteral"),
                this.generatePropertyIsEqualToFragment("myPropertyName", "myLiteral"),
                this.generatePropertyIsEqualToFragment("myPropertyName", "myLiteral"),
                this.generatePropertyIsEqualToFragment("myPropertyName", "myLiteral")));
        FilterTestUtilities.parsefilterStringXML(this.generateNotComparisonFragment(
                this.generatePropertyIsEqualToFragment("myPropertyName", "myLiteral")));
    }
}

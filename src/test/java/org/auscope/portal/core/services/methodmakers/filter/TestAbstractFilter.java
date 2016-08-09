package org.auscope.portal.core.services.methodmakers.filter;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.NotImplementedException;
import org.auscope.portal.core.test.AbstractFilterTestUtilities;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

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
        Assert.assertTrue(this.generatePropertyIsEqualToFragment("myPropertyName", "myLiteral").contains(
                "matchCase=\"false\""));
        Assert.assertTrue(this.generatePropertyIsNotEqualTo("myPropertyName", "myLiteral").contains(
                "matchCase=\"false\""));
        Assert.assertTrue(this.generatePropertyIsLessThan("myPropertyName", "myLiteral")
                .contains("matchCase=\"false\""));
        Assert.assertTrue(this.generatePropertyIsGreaterThan("myPropertyName", "myLiteral").contains(
                "matchCase=\"false\""));
        Assert.assertTrue(this.generatePropertyIsLessThanOrEqualTo("myPropertyName", "myLiteral").contains(
                "matchCase=\"false\""));
        Assert.assertTrue(this.generatePropertyIsGreaterThanOrEqualTo("myPropertyName", "myLiteral").contains(
                "matchCase=\"false\""));
        Assert.assertTrue(this.generatePropertyIsLikeFragment("myPropertyName", "myLiteral").contains(
                "matchCase=\"false\""));
        Assert.assertTrue(this.generateDatePropertyIsLessThan("myPropertyName",false,
                this.generateFunctionDateParse("1997-05-12")).contains(
                "matchCase=\"false\""));
        Assert.assertTrue(this.generateDatePropertyIsGreaterThan("myPropertyName",false,
                this.generateFunctionDateParse("1997-05-12")).contains(
                "matchCase=\"false\""));
    }

    /**
     * Tries to parse the XML that the filters generate - exceptions will be thrown if not well formed
     * @throws SAXException 
     * @throws IOException 
     * @throws ParserConfigurationException 
     */
    @Test
    public void testWellFormedXML() throws ParserConfigurationException, IOException, SAXException {
        AbstractFilterTestUtilities.parsefilterStringXML(this.generatePropertyIsEqualToFragment("myPropertyName",
                "myLiteral"));
        AbstractFilterTestUtilities.parsefilterStringXML(this.generatePropertyIsNotEqualTo("myPropertyName",
                "myLiteral"));
        AbstractFilterTestUtilities
                .parsefilterStringXML(this.generatePropertyIsLessThan("myPropertyName", "myLiteral"));
        AbstractFilterTestUtilities.parsefilterStringXML(this.generatePropertyIsGreaterThan("myPropertyName",
                "myLiteral"));
        AbstractFilterTestUtilities.parsefilterStringXML(this.generatePropertyIsLessThanOrEqualTo("myPropertyName",
                "myLiteral"));
        AbstractFilterTestUtilities.parsefilterStringXML(this.generatePropertyIsGreaterThanOrEqualTo("myPropertyName",
                "myLiteral"));
        AbstractFilterTestUtilities.parsefilterStringXML(this.generatePropertyIsLikeFragment("myPropertyName",
                "myLiteral"));

        AbstractFilterTestUtilities.parsefilterStringXML(this.generatePropertyIsEqualToFragment("myPropertyName",
                "myLiteral", true, MatchActionType.Any));
        AbstractFilterTestUtilities.parsefilterStringXML(this.generatePropertyIsNotEqualTo("myPropertyName",
                "myLiteral", true, MatchActionType.All));
        AbstractFilterTestUtilities.parsefilterStringXML(this.generatePropertyIsLessThan("myPropertyName", "myLiteral",
                true, MatchActionType.One));
        AbstractFilterTestUtilities.parsefilterStringXML(this.generatePropertyIsGreaterThan("myPropertyName",
                "myLiteral", true, MatchActionType.Any));
        AbstractFilterTestUtilities.parsefilterStringXML(this.generatePropertyIsLessThanOrEqualTo("myPropertyName",
                "myLiteral", false, MatchActionType.All));
        AbstractFilterTestUtilities.parsefilterStringXML(this.generatePropertyIsGreaterThanOrEqualTo("myPropertyName",
                "myLiteral", false, MatchActionType.One));
        AbstractFilterTestUtilities.parsefilterStringXML(this.generatePropertyIsLikeFragment("myPropertyName",
                "myLiteral", 'a', 'b', 'c', true, MatchActionType.Any));
        AbstractFilterTestUtilities.parsefilterStringXML(this.generateDatePropertyIsGreaterThan("myPropertyName",false,
                this.generateFunctionDateParse("1997-05-12")));
        AbstractFilterTestUtilities.parsefilterStringXML(this.generateDatePropertyIsLessThan("myPropertyName",false,
                this.generateFunctionDateParse("1997-05-12")));


        AbstractFilterTestUtilities.parsefilterStringXML(this.generateAndComparisonFragment(
                this.generatePropertyIsEqualToFragment("myPropertyName", "myLiteral"),
                this.generatePropertyIsEqualToFragment("myPropertyName", "myLiteral"),
                this.generatePropertyIsEqualToFragment("myPropertyName", "myLiteral"),
                this.generatePropertyIsEqualToFragment("myPropertyName", "myLiteral")));
        AbstractFilterTestUtilities.parsefilterStringXML(this.generateOrComparisonFragment(
                this.generatePropertyIsEqualToFragment("myPropertyName", "myLiteral"),
                this.generatePropertyIsEqualToFragment("myPropertyName", "myLiteral"),
                this.generatePropertyIsEqualToFragment("myPropertyName", "myLiteral"),
                this.generatePropertyIsEqualToFragment("myPropertyName", "myLiteral")));
        AbstractFilterTestUtilities.parsefilterStringXML(this.generateNotComparisonFragment(
                this.generatePropertyIsEqualToFragment("myPropertyName", "myLiteral")));
    }

    @Test
    public void testEscapingChars() {
        final String literal = "&<>\"";
        final String escapedLiteral = "&amp;&lt;&gt;&quot;";
        final String propertyName = "propertyName";

        String fragment = this.generatePropertyIsEqualToFragment(propertyName, literal);
        Assert.assertFalse(fragment.contains(literal));
        Assert.assertTrue(fragment.contains(escapedLiteral));

        fragment = this.generatePropertyIsGreaterThan(propertyName, literal);
        Assert.assertFalse(fragment.contains(literal));
        Assert.assertTrue(fragment.contains(escapedLiteral));

        fragment = this.generatePropertyIsLessThan(propertyName, literal);
        Assert.assertFalse(fragment.contains(literal));
        Assert.assertTrue(fragment.contains(escapedLiteral));

        fragment = this.generatePropertyIsLikeFragment(propertyName, literal);
        Assert.assertFalse(fragment.contains(literal));
        Assert.assertTrue(fragment.contains(escapedLiteral));

        fragment = this.generatePropertyIsNotEqualTo(propertyName, literal);
        Assert.assertFalse(fragment.contains(literal));
        Assert.assertTrue(fragment.contains(escapedLiteral));

        fragment = this.generateDatePropertyIsGreaterThan(propertyName, false,
                this.generateFunctionDateParse(escapedLiteral));
        Assert.assertFalse(fragment.contains(literal));
        Assert.assertTrue(fragment.contains(escapedLiteral));

        fragment = this.generateDatePropertyIsLessThan(propertyName, false,
                this.generateFunctionDateParse(escapedLiteral));
        Assert.assertFalse(fragment.contains(literal));
        Assert.assertTrue(fragment.contains(escapedLiteral));
    }
}

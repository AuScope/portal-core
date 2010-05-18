package org.auscope.portal.mineraloccurrence;

import java.io.IOException;
import java.util.Collection;
import java.util.Arrays;

import junit.framework.Assert;

import org.auscope.portal.Util;
import org.junit.Test;
import org.jmock.Mockery;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;


/**
 * User: Michael Stegherr
 * Date: 30/03/2009
 * Time: 3:27:26 PM
 */
public class TestMineralOccurrenceFilter {
    private Mockery context = new Mockery(){{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    @Test
    public void testWithTwoNames() throws IOException {
        final Commodity commodity1 = context.mock(Commodity.class);
        final Commodity commodity2 = context.mock(Commodity.class, "commodity2");
        final Collection<Commodity> commodities = Arrays.asList(commodity1, commodity2);
        
        context.checking(new Expectations() {{
            oneOf (commodity1).getSource(); will(returnValue("urn:cgi:feature:GSV:MineralOccurrence:361169"));
            oneOf (commodity2).getSource(); will(returnValue("urn:cgi:feature:GSV:MineralOccurrence:361179"));
        }});
        
        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter(commodities, "", "", "", "", "");
        
        Assert.assertEquals(Util.loadXML(
            "src/test/resources/GetMineralOccurrencesWithTwoSpecifiedNames.xml").replaceAll("\n", "").replaceAll("\\s+", ""),
            mineralOccurrenceFilter.getFilterString().replaceAll("\n", "").replaceAll("\\s+", ""));
    }
    
    @Test
    public void testWithNameAndMinimumOreAmount() throws IOException {
        final Commodity commodity1 = context.mock(Commodity.class);
        final Collection<Commodity> commodities = Arrays.asList(commodity1);

        context.checking(new Expectations() {{
            oneOf (commodity1).getSource(); will(returnValue("urn:cgi:feature:GSV:MineralOccurrence:361179"));
        }});
        
        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter(commodities, "Any", "1234567", "urn:ogc:def:uom:UCUM:t", "", "");

        Assert.assertEquals(Util.loadXML(
            "src/test/resources/GetMineralOccurrencesWithSpecifiedNameAndMinimumOreAmount.xml").replaceAll("\n", "").replaceAll("\\s+", ""),
            mineralOccurrenceFilter.getFilterString().replaceAll("\n", "").replaceAll("\\s+", ""));
    }
    
    @Test
    public void testReserveMinimumOreAmount() throws IOException {
        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter(null, "Reserve", "2000000", "urn:ogc:def:uom:UCUM:t", "", "");
        
        Assert.assertEquals(Util.loadXML(
            "src/test/resources/GetMineralOccurrencesWithSpecifiedReserveMinimumOreAmount.xml").replaceAll("\n", "").replaceAll("\\W", ""),
            mineralOccurrenceFilter.getFilterString().replaceAll("\n", "").replaceAll("\\W", ""));
    }

    @Test
    public void testAnyMinimumOreAmount() throws IOException {
        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter(null, "Any", "1000000", "urn:ogc:def:uom:UCUM:t", "", "");
        
        Assert.assertEquals(Util.loadXML(
            "src/test/resources/GetMineralOccurrencesWithSpecifiedMinimumOreAmount.xml").replaceAll("\n", "").replaceAll("\\W", ""),
            mineralOccurrenceFilter.getFilterString().replaceAll("\n", "").replaceAll("\\W", ""));
    }

    @Test
    public void testResourceMinimumCommodityAmount() throws IOException {
        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter(null, "Resource", "", "", "6000000", "urn:ogc:def:uom:UCUM:t");
        
        Assert.assertEquals(Util.loadXML(
            "src/test/resources/GetMineralOccurrencesWithSpecifiedResourceMinimumCommodityAmount.xml").replaceAll("\n", "").replaceAll("\\W", ""),
            mineralOccurrenceFilter.getFilterString().replaceAll("\n", "").replaceAll("\\W", ""));
    }


    @Test
    public void testAnyMinimumCommodityAmount() throws IOException {
        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter(null, "ANY-Stuff", "", "", "7000000", "urn:ogc:def:uom:UCUM:t");
        
        Assert.assertEquals(Util.loadXML(
            "src/test/resources/GetMineralOccurrencesWithSpecifiedMinimumCommodityAmount.xml").replaceAll("\n", "").replaceAll("\\W", ""),
            mineralOccurrenceFilter.getFilterString().replaceAll("\n", "").replaceAll("\\W", ""));
    }

    
    //TODO: to be reimplemented when the data model (mineraloccurrence ml) complies to this feature

/*
    @Test
    public void testEndowmentCutOffGrade() throws IOException {
        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter(null, "Endowment", "", "", "", "1500", "", "");

        Assert.assertEquals(Util.loadXML(
            "src/test/resources/GetMineralOccurrencesWithSpecifiedEndowmentCutOffGrade.xml").replaceAll("\n", "").replaceAll("\\W", ""),
            mineralOccurrenceFilter.getFilterString().replaceAll("\n", "").replaceAll("\\W", ""));
    }*/
}

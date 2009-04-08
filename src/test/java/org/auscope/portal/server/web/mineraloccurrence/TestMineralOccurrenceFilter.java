package org.auscope.portal.server.web.mineraloccurrence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;

import org.auscope.portal.Util;
import org.junit.Test;


/**
 * User: Michael Stegherr
 * Date: 30/03/2009
 * Time: 3:27:26 PM
 */
public class TestMineralOccurrenceFilter {

    @Test
    public void testWithTwoNames() throws IOException {
        Collection<String> names = new ArrayList<String>();
        names.add("urn:cgi:feature:GSV:MineralOccurrence:361169");
        names.add("urn:cgi:feature:GSV:MineralOccurrence:361179");
        
        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter(names, "", "", "", "");
        
        Assert.assertEquals(Util.loadXML(
            "src/test/resources/GetMineralOccurrencesWithTwoSpecifiedNames.xml").replace("\n", "").replace(" ", ""),
            mineralOccurrenceFilter.getFilterString().replace("\n", "").replace(" ", ""));
    }
    
    @Test
    public void testWithNameAndMinimumOreAmount() throws IOException {
        Collection<String> names = new ArrayList<String>();
        names.add("urn:cgi:feature:GSV:MineralOccurrence:361179");
        
        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter(names, "Any", "1234567", "", "");
        
        Assert.assertEquals(Util.loadXML(
            "src/test/resources/GetMineralOccurrencesWithSpecifiedNameAndMinimumOreAmount.xml").replace("\n", "").replace(" ", ""),
            mineralOccurrenceFilter.getFilterString().replace("\n", "").replace(" ", ""));
    }
    
    @Test
    public void testReserveMinimumOreAmount() throws IOException {
        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter(null, "Reserve", "2000000", "", "");
        
        Assert.assertEquals(Util.loadXML(
            "src/test/resources/GetMineralOccurrencesWithSpecifiedReserveMinimumOreAmount.xml").replace("\n", "").replace(" ", ""),
            mineralOccurrenceFilter.getFilterString().replace("\n", "").replace(" ", ""));
    }

    @Test
    public void testAnyMinimumOreAmount() throws IOException {
        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter(null, "Any", "1000000", "", "");
        
        Assert.assertEquals(Util.loadXML(
            "src/test/resources/GetMineralOccurrencesWithSpecifiedMinimumOreAmount.xml").replace("\n", "").replace(" ", ""),
            mineralOccurrenceFilter.getFilterString().replace("\n", "").replace(" ", ""));
    }

    @Test
    public void testResourceMinimumCommodityAmount() throws IOException {
        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter(null, "Resource", "", "6000000", "");
        
        Assert.assertEquals(Util.loadXML(
            "src/test/resources/GetMineralOccurrencesWithSpecifiedResourceMinimumCommodityAmount.xml").replace("\n", "").replace(" ", ""),
            mineralOccurrenceFilter.getFilterString().replace("\n", "").replace(" ", ""));
    }

    @Test
    public void testAnyMinimumCommodityAmount() throws IOException {
        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter(null, "ANY-Stuff", "", "7000000", "");
        
        Assert.assertEquals(Util.loadXML(
            "src/test/resources/GetMineralOccurrencesWithSpecifiedMinimumCommodityAmount.xml").replace("\n", "").replace(" ", ""),
            mineralOccurrenceFilter.getFilterString().replace("\n", "").replace(" ", ""));
    }

    @Test
    public void testEndowmentCutOffGrade() throws IOException {
        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter(null, "Endowment", "", "", "1500");
        
        Assert.assertEquals(Util.loadXML(
            "src/test/resources/GetMineralOccurrencesWithSpecifiedEndowmentCutOffGrade.xml").replace("\n", "").replace(" ", ""),
            mineralOccurrenceFilter.getFilterString().replace("\n", "").replace(" ", ""));
    }

    @Test
    public void testAnyCutOffGrade() throws IOException {
        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter(null, "Any", "", "", "1000");
        
        Assert.assertEquals(Util.loadXML(
            "src/test/resources/GetMineralOccurrencesWithSpecifiedCutOffGrade.xml").replace("\n", "").replace(" ", ""),
            mineralOccurrenceFilter.getFilterString().replace("\n", "").replace(" ", ""));
    }
}

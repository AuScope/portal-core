package org.auscope.portal.server.web.mineraloccurrence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;

import org.auscope.portal.Util;
import org.auscope.portal.server.web.mineraloccurrence.MineralOccurrenceFilter.MeasureType;
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
        MineralOccurrenceFilter mineralOccurrenceFilter = new MineralOccurrenceFilter(names, null, "", "", "");
        Assert.assertEquals(
                Util.loadXML("src/test/resources/GetMineralOccurrencesWithTwoSpecifiedNames.xml").replace("\n", "").replace(" ", ""),
                mineralOccurrenceFilter.getFilterString().replace("\n", "").replace(" ", ""));
    }
    
    @Test
    public void testReserveMinimumOreAmount() throws IOException {
        MeasureType measureType = MeasureType.RESERVE;
        MineralOccurrenceFilter mineralOccurrenceFilter = new MineralOccurrenceFilter(null, measureType, "2000000", "", "");
        Assert.assertEquals(
                Util.loadXML("src/test/resources/GetMineralOccurrencesWithSpecifiedReserveMinimumOreAmount.xml").replace("\n", "").replace(" ", ""),
                mineralOccurrenceFilter.getFilterString().replace("\n", "").replace(" ", ""));
    }

    @Test
    public void testResourceMinimumCommodityAmount() throws IOException {
        MeasureType measureType = MeasureType.RESOURCE;
        MineralOccurrenceFilter mineralOccurrenceFilter = new MineralOccurrenceFilter(null, measureType, "", "6000000", "");
        Assert.assertEquals(
                Util.loadXML("src/test/resources/GetMineralOccurrencesWithSpecifiedResourceMinimumCommodityAmount.xml").replace("\n", "").replace(" ", ""),
                mineralOccurrenceFilter.getFilterString().replace("\n", "").replace(" ", ""));
    }

    @Test
    public void testCutOffGrade() throws IOException {
        // TODO implementation
    }
}

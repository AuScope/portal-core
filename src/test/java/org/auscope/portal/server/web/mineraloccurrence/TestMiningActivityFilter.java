package org.auscope.portal.server.web.mineraloccurrence;

import org.junit.Test;
import org.auscope.portal.Util;
import junit.framework.Assert;

import java.io.IOException;

/**
 * User: Mathew Wyatt
 * Date: 25/03/2009
 * Time: 9:07:20 AM
 */
public class TestMiningActivityFilter {

    @Test
    public void testAssociatedMine() throws IOException {
        MiningActivityFilter miningActivityFilter = new MiningActivityFilter("urn:cgi:feature:GSV:Mine:361068", "", "", "", "", "", "");
        Assert.assertEquals(Util.loadXML("src/test/resources/GetMiningActivity-AssociatedMine.xml").replace("\n", "").replace(" ", ""), miningActivityFilter.getFilterString().replace("\n", "").replace(" ", ""));
    }

    @Test
    public void testAssociatedMineDateRange() throws IOException {
        MiningActivityFilter miningActivityFilter = new MiningActivityFilter("urn:cgi:feature:GSV:Mine:361068", "01/JAN/1870", "31/DEC/1885", "", "", "", "");
        Assert.assertEquals(Util.loadXML("src/test/resources/GetMiningActivity-AssociatedMineDateRange.xml").replace("\n", "").replace(" ", ""), miningActivityFilter.getFilterString().replace("\n", "").replace(" ", ""));
    }

    @Test
    public void testAssociatedMineDateRangeOre() throws IOException {
        MiningActivityFilter miningActivityFilter =
            new MiningActivityFilter("urn:cgi:feature:GSV:Mine:361068", "01/JAN/1870", "31/DEC/1885", "28", "", "", "");
        
        Assert.assertEquals(
                Util.loadXML("src/test/resources/GetMiningActivity-AssociatedMineDateRangeOre.xml").replace("\n", "").replace(" ", ""),
                miningActivityFilter.getFilterString().replace("\n", "").replace(" ", ""));
    }

    @Test
    public void testAssociatedMineDateRangeProducedMaterial() throws IOException {
        MiningActivityFilter miningActivityFilter =
            new MiningActivityFilter("urn:cgi:feature:GSV:Mine:361068", "01/JAN/1870", "31/DEC/1885", "", "Gold", "", "");
        
        Assert.assertEquals(
                Util.loadXML("src/test/resources/GetMiningActivity-AssociatedMineDateRangeProducedMaterial.xml").replace("\n", "").replace(" ", ""),
                miningActivityFilter.getFilterString().replace("\n", "").replace(" ", ""));
    }

    @Test
    public void testAssociatedMineDateRangeCutOffGrade() throws IOException {
        MiningActivityFilter miningActivityFilter =
            new MiningActivityFilter("urn:cgi:feature:GSV:Mine:361068", "01/JAN/1870", "31/DEC/1885", "", "", "10.14", "");
        
        Assert.assertEquals(
                Util.loadXML("src/test/resources/GetMiningActivity-AssociatedMineDateRangeCutOffGrade.xml").replace("\n", "").replace(" ", ""),
                miningActivityFilter.getFilterString().replace("\n", "").replace(" ", ""));
    }

    @Test
    public void testAssociatedMineDateRangeProduction() throws IOException {
        MiningActivityFilter miningActivityFilter =
            new MiningActivityFilter("urn:cgi:feature:GSV:Mine:361068", "01/JAN/1870", "31/DEC/1885", "", "", "", "1");
        
        Assert.assertEquals(
                Util.loadXML("src/test/resources/GetMiningActivity-AssociatedMineDateRangeProduction.xml").replace("\n", "").replace(" ", ""),
                miningActivityFilter.getFilterString().replace("\n", "").replace(" ", ""));
    }

    @Test
    public void testAll() {

    }

    @Test
    public void testAllWithNoDate() {

    }
}

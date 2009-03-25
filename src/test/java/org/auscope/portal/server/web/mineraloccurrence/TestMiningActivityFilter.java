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
        MiningActivityFilter miningActivityFilter = new MiningActivityFilter("urn:cgi:feature:GSV:Mine:361255", "", "", "", "", "", "");
        Assert.assertEquals(Util.loadXML("src/test/resources/GetMiningActivity-AsscociatedMine.xml").replace("\n", "").replace(" ", ""), miningActivityFilter.getFilterString().replace("\n", "").replace(" ", ""));
    }

    @Test
    public void testAssociatedMineDateRange() {

    }

    @Test
    public void testAssociatedMineDateRangeOre() {

    }

    @Test
    public void testAssociatedMineDateRangeProducedMaterial() {

    }

    @Test
    public void testAssociatedMineDateRangeCutOffGrade() {

    }

    @Test
    public void testAssociatedMineDateRangeProduction() {

    }

    @Test
    public void testAll() {

    }

    @Test
    public void testAllWithNoDate() {

    }
}

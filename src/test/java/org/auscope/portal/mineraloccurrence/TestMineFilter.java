package org.auscope.portal.mineraloccurrence;

import org.junit.Test;
import org.auscope.portal.Util;
import junit.framework.Assert;

import java.io.IOException;

/**
 * User: Mathew Wyatt
 * Date: 25/03/2009
 * Time: 8:23:09 AM
 */
public class TestMineFilter {

    /**
     * Test without mine name. If there is no name specified then all of the mines should be queried.
     */
    /*@Test
    public void testWithNoMineName() throws IOException {
        MineFilter mineFilter = new MineFilter("");
        Assert.assertEquals(Util.loadXML("src/test/resources/GetAllMines.xml").replace("\n", "").replace(" ", ""), mineFilter.getFilterString().replace("\n", "").replace(" ", ""));
    }*/

    /**
     *  Test with a mine name. A filter query should be generated searching for mines with the given name.
     */
    @Test
    public void testWithAMineName() throws IOException {
        MineFilter mineFilter = new MineFilter("Dominion Copper Mine");
        Assert.assertEquals(Util.loadXML("src/test/resources/GetMineWithSpecifiedName.xml").replace("\n", "").replace(" ", ""), mineFilter.getFilterStringAllRecords().replace("\n", "").replace(" ", ""));
    }

}

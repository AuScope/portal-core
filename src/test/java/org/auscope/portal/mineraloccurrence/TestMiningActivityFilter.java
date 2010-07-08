package org.auscope.portal.mineraloccurrence;

import org.junit.Test;
import org.junit.Before;
import org.auscope.portal.Util;
import org.jmock.Mockery;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;
import junit.framework.Assert;

import java.io.IOException;
import java.util.List;
import java.util.Arrays;

/**
 * User: Mathew Wyatt
 * Date: 25/03/2009
 * Time: 9:07:20 AM
 */
public class TestMiningActivityFilter {
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);    
    }};

    private Mine mockMine;
    private List<Mine> mockMineList;

    @Before
    public void setup() {
        this.mockMine = context.mock(Mine.class);
        this.mockMineList = Arrays.asList(mockMine);
    }

    @Test
    public void testAssociatedMine() throws IOException {
        context.checking(new Expectations() {{
            oneOf (mockMine).getMineNameURI();will(returnValue("urn:cgi:feature:GSV:Mine:361068"));
        }});

        MiningActivityFilter miningActivityFilter = new MiningActivityFilter(this.mockMineList, "", "", "", "", "", "");
        Assert.assertEquals(Util.loadXML("src/test/resources/GetMiningActivity-AssociatedMine.xml").replace("\n", "").replace(" ", ""), miningActivityFilter.getFilterStringAllRecords().replace("\n", "").replace(" ", ""));
    }

    @Test
    public void testAssociatedMineDateRange() throws IOException {
        context.checking(new Expectations() {{
            oneOf (mockMine).getMineNameURI();will(returnValue("urn:cgi:feature:GSV:Mine:361068"));
        }});

        MiningActivityFilter miningActivityFilter = new MiningActivityFilter(this.mockMineList, "01/JAN/1870", "31/DEC/1885", "", "", "", "");
        Assert.assertEquals(Util.loadXML("src/test/resources/GetMiningActivity-AssociatedMineDateRange.xml").replace("\n", "").replace(" ", ""), miningActivityFilter.getFilterStringAllRecords().replace("\n", "").replace(" ", ""));
    }

    @Test
    public void testAssociatedMineDateRangeOre() throws IOException {
        context.checking(new Expectations() {{
            oneOf (mockMine).getMineNameURI();will(returnValue("urn:cgi:feature:GSV:Mine:361068"));
        }});

        MiningActivityFilter miningActivityFilter =
            new MiningActivityFilter(this.mockMineList, "01/JAN/1870", "31/DEC/1885", "28", "", "", "");
        
        Assert.assertEquals(
                Util.loadXML("src/test/resources/GetMiningActivity-AssociatedMineDateRangeOre.xml").replace("\n", "").replace(" ", ""),
                miningActivityFilter.getFilterStringAllRecords().replace("\n", "").replace(" ", ""));
    }

    @Test
    public void testAssociatedMineDateRangeProducedMaterial() throws IOException {
       context.checking(new Expectations() {{
            oneOf (mockMine).getMineNameURI();will(returnValue("urn:cgi:feature:GSV:Mine:361068"));
        }});

        MiningActivityFilter miningActivityFilter =
            new MiningActivityFilter(this.mockMineList, "01/JAN/1870", "31/DEC/1885", "", "Gold", "", "");
        
        Assert.assertEquals(
                Util.loadXML("src/test/resources/GetMiningActivity-AssociatedMineDateRangeProducedMaterial.xml").replace("\n", "").replace(" ", ""),
                miningActivityFilter.getFilterStringAllRecords().replace("\n", "").replace(" ", ""));
    }

    @Test
    public void testAssociatedMineDateRangeCutOffGrade() throws IOException {
        context.checking(new Expectations() {{
            oneOf (mockMine).getMineNameURI();will(returnValue("urn:cgi:feature:GSV:Mine:361068"));
        }});

        MiningActivityFilter miningActivityFilter =
            new MiningActivityFilter(this.mockMineList, "01/JAN/1870", "31/DEC/1885", "", "", "10.14", "");
        
        Assert.assertEquals(
                Util.loadXML("src/test/resources/GetMiningActivity-AssociatedMineDateRangeCutOffGrade.xml").replace("\n", "").replace(" ", ""),
                miningActivityFilter.getFilterStringAllRecords().replace("\n", "").replace(" ", ""));
    }

    @Test
    public void testAssociatedMineDateRangeProduction() throws IOException {
        context.checking(new Expectations() {{
            oneOf (mockMine).getMineNameURI();will(returnValue("urn:cgi:feature:GSV:Mine:361068"));
        }});

        MiningActivityFilter miningActivityFilter =
            new MiningActivityFilter(this.mockMineList, "01/JAN/1870", "31/DEC/1885", "", "", "", "1");
        
        Assert.assertEquals(
                Util.loadXML("src/test/resources/GetMiningActivity-AssociatedMineDateRangeProduction.xml").replace("\n", "").replace(" ", ""),
                miningActivityFilter.getFilterStringAllRecords().replace("\n", "").replace(" ", ""));
    }

}

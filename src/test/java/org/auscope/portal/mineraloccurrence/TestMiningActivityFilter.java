package org.auscope.portal.mineraloccurrence;

import java.util.Arrays;
import java.util.List;

import org.auscope.portal.server.domain.ogc.FilterTestUtilities;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

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
    public void testAssociatedMine() throws Exception {
        final List<String> activities = Arrays.asList("activity1", "activity2");
        context.checking(new Expectations() {{
            oneOf (mockMine).getMineNameURI();will(returnValue("urn:cgi:feature:GSV:Mine:361068"));
            allowing (mockMine).getRelatedActivities();will(returnValue(activities));
        }});

        MiningActivityFilter miningActivityFilter = new MiningActivityFilter(this.mockMineList, "", "", "", "", "", "");
        String filter = miningActivityFilter.getFilterStringAllRecords();
        Document doc = FilterTestUtilities.parsefilterStringXML(filter);

        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/ogc:PropertyName", new String[] {"gml:name"}, 2);
        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/ogc:Literal", new String[] {"activity1", "activity2"}, 2);
    }

    @Test
    public void testAssociatedMineDateRange() throws Exception {
        final List<String> activities = Arrays.asList("activity1");
        context.checking(new Expectations() {{
            oneOf (mockMine).getMineNameURI();will(returnValue("urn:cgi:feature:GSV:Mine:361068"));
            allowing (mockMine).getRelatedActivities();will(returnValue(activities));
        }});

        MiningActivityFilter miningActivityFilter = new MiningActivityFilter(this.mockMineList, "01/JAN/1870", "31/DEC/1885", "", "", "", "");
        String filter = miningActivityFilter.getFilterStringAllRecords();
        Document doc = FilterTestUtilities.parsefilterStringXML(filter);

        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/ogc:PropertyName", new String[] {"gml:name"}, 1);
        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/ogc:Literal", new String[] {"activity1"}, 1);

        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsGreaterThanOrEqualTo/ogc:Literal", new String[] {"01/JAN/1870"}, 1);
        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLessThanOrEqualTo/ogc:Literal", new String[] {"31/DEC/1885"}, 1);
    }

    @Test
    public void testAssociatedMineDateRangeOre() throws Exception {
        final List<String> activities = Arrays.asList("activity1");
        context.checking(new Expectations() {{
            oneOf (mockMine).getMineNameURI();will(returnValue("urn:cgi:feature:GSV:Mine:361068"));
            allowing (mockMine).getRelatedActivities();will(returnValue(activities));
        }});

        MiningActivityFilter miningActivityFilter =
            new MiningActivityFilter(this.mockMineList, "01/JAN/1870", "31/DEC/1885", "28", "", "", "");

        String filter = miningActivityFilter.getFilterStringAllRecords();
        Document doc = FilterTestUtilities.parsefilterStringXML(filter);

        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/ogc:PropertyName", new String[] {"gml:name"}, 1);
        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/ogc:Literal", new String[] {"activity1"}, 1);

        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsGreaterThanOrEqualTo/ogc:Literal", new String[] {"01/JAN/1870"}, 1);
        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLessThanOrEqualTo/ogc:Literal", new String[] {"31/DEC/1885"}, 1);

        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsGreaterThan/ogc:Literal", new String[] {"28"}, 1);
    }

    @Test
    public void testAssociatedMineDateRangeProducedMaterial() throws Exception {
        final List<String> activities = Arrays.asList("activity1");
        context.checking(new Expectations() {{
            oneOf (mockMine).getMineNameURI();will(returnValue("urn:cgi:feature:GSV:Mine:361068"));
            allowing (mockMine).getRelatedActivities();will(returnValue(activities));
        }});

        MiningActivityFilter miningActivityFilter =
            new MiningActivityFilter(this.mockMineList, "01/JAN/1870", "31/DEC/1885", "", "Gold", "", "");

        String filter = miningActivityFilter.getFilterStringAllRecords();
        Document doc = FilterTestUtilities.parsefilterStringXML(filter);

        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/ogc:PropertyName", new String[] {"gml:name" , "er:producedMaterial/er:Product/er:productName/gsml:CGI_TermValue/gsml:value"}, 2);
        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/ogc:Literal", new String[] {"activity1", "Gold"}, 2);

        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsGreaterThanOrEqualTo/ogc:Literal", new String[] {"01/JAN/1870"}, 1);
        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLessThanOrEqualTo/ogc:Literal", new String[] {"31/DEC/1885"}, 1);
    }

    @Test
    public void testAssociatedMineDateRangeCutOffGrade() throws Exception {
        final List<String> activities = Arrays.asList("activity1");
        context.checking(new Expectations() {{
            oneOf (mockMine).getMineNameURI();will(returnValue("urn:cgi:feature:GSV:Mine:361068"));
            allowing (mockMine).getRelatedActivities();will(returnValue(activities));
        }});

        MiningActivityFilter miningActivityFilter =
            new MiningActivityFilter(this.mockMineList, "01/JAN/1870", "31/DEC/1885", "", "", "10.14", "");

        String filter = miningActivityFilter.getFilterStringAllRecords();
        Document doc = FilterTestUtilities.parsefilterStringXML(filter);

        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/ogc:PropertyName", new String[] {"gml:name"}, 1);
        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/ogc:Literal", new String[] {"activity1"}, 1);

        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsGreaterThanOrEqualTo/ogc:Literal", new String[] {"01/JAN/1870"}, 1);
        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLessThanOrEqualTo/ogc:Literal", new String[] {"31/DEC/1885"}, 1);

        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsGreaterThan/ogc:Literal", new String[] {"10.14"}, 1);
    }

    @Test
    public void testAssociatedMineDateRangeProduction() throws Exception {
        final List<String> activities = Arrays.asList("activity1");
        context.checking(new Expectations() {{
            oneOf (mockMine).getMineNameURI();will(returnValue("urn:cgi:feature:GSV:Mine:361068"));
            allowing (mockMine).getRelatedActivities();will(returnValue(activities));
        }});

        MiningActivityFilter miningActivityFilter =
            new MiningActivityFilter(this.mockMineList, "01/JAN/1870", "31/DEC/1885", "", "", "", "1");

        String filter = miningActivityFilter.getFilterStringAllRecords();
        Document doc = FilterTestUtilities.parsefilterStringXML(filter);

        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/ogc:PropertyName", new String[] {"gml:name"}, 1);
        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/ogc:Literal", new String[] {"activity1"}, 1);

        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsGreaterThanOrEqualTo/ogc:Literal", new String[] {"01/JAN/1870"}, 1);
        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLessThanOrEqualTo/ogc:Literal", new String[] {"31/DEC/1885"}, 1);

        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsGreaterThan/ogc:Literal", new String[] {"1"}, 1);
    }

}

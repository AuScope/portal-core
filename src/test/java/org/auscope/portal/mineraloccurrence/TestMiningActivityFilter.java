package org.auscope.portal.mineraloccurrence;

import org.auscope.portal.PortalTestClass;
import org.auscope.portal.server.domain.ogc.AbstractFilterTestUtilities;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * User: Mathew Wyatt
 * Date: 25/03/2009
 * Time: 9:07:20 AM
 */
public class TestMiningActivityFilter extends PortalTestClass {

    @Test
    public void testAssociatedMine() throws Exception {
        MiningActivityFilter miningActivityFilter = new MiningActivityFilter("urn:cgi:feature:GSV:Mine:361068", "", "", "", "", "", "");
        String filter = miningActivityFilter.getFilterStringAllRecords();
        Document doc = AbstractFilterTestUtilities.parsefilterStringXML(filter);

        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLike/ogc:PropertyName",
                new String[] {"er:specification/er:Mine/er:relatedActivity/er:MiningActivity/gml:name", "er:specification/er:Mine/er:mineName/er:MineName/er:mineName"}, 2);
        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLike/ogc:Literal",
                new String[] {"*", "urn:cgi:feature:GSV:Mine:361068"}, 2);
    }

    @Test
    public void testAssociatedMineDateRange() throws Exception {

        MiningActivityFilter miningActivityFilter = new MiningActivityFilter("urn:cgi:feature:GSV:Mine:361068", "01/JAN/1870", "31/DEC/1885", "", "", "", "");
        String filter = miningActivityFilter.getFilterStringAllRecords();
        Document doc = AbstractFilterTestUtilities.parsefilterStringXML(filter);

        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLike/ogc:PropertyName",
                new String[] {"er:specification/er:Mine/er:relatedActivity/er:MiningActivity/gml:name", "er:specification/er:Mine/er:mineName/er:MineName/er:mineName"}, 2);
        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLike/ogc:Literal",
                new String[] {"*", "urn:cgi:feature:GSV:Mine:361068"}, 2);

        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsGreaterThanOrEqualTo/ogc:Literal", new String[] {"01/JAN/1870"}, 1);
        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLessThanOrEqualTo/ogc:Literal", new String[] {"31/DEC/1885"}, 1);
    }

    @Test
    public void testAssociatedMineDateRangeOre() throws Exception {

        MiningActivityFilter miningActivityFilter =
            new MiningActivityFilter("urn:cgi:feature:GSV:Mine:361068", "01/JAN/1870", "31/DEC/1885", "28", "", "", "");

        String filter = miningActivityFilter.getFilterStringAllRecords();
        Document doc = AbstractFilterTestUtilities.parsefilterStringXML(filter);

        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLike/ogc:PropertyName",
                new String[] {"er:specification/er:Mine/er:relatedActivity/er:MiningActivity/gml:name", "er:specification/er:Mine/er:mineName/er:MineName/er:mineName"}, 2);
        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLike/ogc:Literal",
                new String[] {"*", "urn:cgi:feature:GSV:Mine:361068"}, 2);

        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsGreaterThanOrEqualTo/ogc:Literal", new String[] {"01/JAN/1870"}, 1);
        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLessThanOrEqualTo/ogc:Literal", new String[] {"31/DEC/1885"}, 1);

        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsGreaterThan/ogc:Literal", new String[] {"28"}, 1);
    }

    @Test
    public void testAssociatedMineDateRangeProducedMaterial() throws Exception {

        MiningActivityFilter miningActivityFilter =
            new MiningActivityFilter("urn:cgi:feature:GSV:Mine:361068", "01/JAN/1870", "31/DEC/1885", "", "Gold", "", "");

        String filter = miningActivityFilter.getFilterStringAllRecords();
        Document doc = AbstractFilterTestUtilities.parsefilterStringXML(filter);

        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLike/ogc:PropertyName",
                new String[] {"er:specification/er:Mine/er:relatedActivity/er:MiningActivity/gml:name", "er:specification/er:Mine/er:mineName/er:MineName/er:mineName"}, 2);
        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLike/ogc:Literal",
                new String[] {"*", "urn:cgi:feature:GSV:Mine:361068"}, 2);

        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/ogc:PropertyName",
                new String[] {"er:specification/er:Mine/er:relatedActivity/er:MiningActivity/er:producedMaterial/er:Product/er:productName/gsml:CGI_TermValue/gsml:value"}, 1);
        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/ogc:Literal", new String[] {"Gold"}, 1);

        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsGreaterThanOrEqualTo/ogc:Literal", new String[] {"01/JAN/1870"}, 1);
        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLessThanOrEqualTo/ogc:Literal", new String[] {"31/DEC/1885"}, 1);
    }

    @Test
    public void testAssociatedMineDateRangeCutOffGrade() throws Exception {

        MiningActivityFilter miningActivityFilter =
            new MiningActivityFilter("urn:cgi:feature:GSV:Mine:361068", "01/JAN/1870", "31/DEC/1885", "", "", "10.14", "");

        String filter = miningActivityFilter.getFilterStringAllRecords();
        Document doc = AbstractFilterTestUtilities.parsefilterStringXML(filter);

        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLike/ogc:PropertyName",
                new String[] {"er:specification/er:Mine/er:relatedActivity/er:MiningActivity/gml:name", "er:specification/er:Mine/er:mineName/er:MineName/er:mineName"}, 2);
        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLike/ogc:Literal",
                new String[] {"*", "urn:cgi:feature:GSV:Mine:361068"}, 2);

        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsGreaterThanOrEqualTo/ogc:Literal", new String[] {"01/JAN/1870"}, 1);
        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLessThanOrEqualTo/ogc:Literal", new String[] {"31/DEC/1885"}, 1);

        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsGreaterThan/ogc:Literal", new String[] {"10.14"}, 1);
    }

    @Test
    public void testAssociatedMineDateRangeProduction() throws Exception {

        MiningActivityFilter miningActivityFilter =
            new MiningActivityFilter("urn:cgi:feature:GSV:Mine:361068", "01/JAN/1870", "31/DEC/1885", "", "", "", "1");

        String filter = miningActivityFilter.getFilterStringAllRecords();
        Document doc = AbstractFilterTestUtilities.parsefilterStringXML(filter);

        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLike/ogc:PropertyName",
                new String[] {"er:specification/er:Mine/er:relatedActivity/er:MiningActivity/gml:name", "er:specification/er:Mine/er:mineName/er:MineName/er:mineName"}, 2);
        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLike/ogc:Literal",
                new String[] {"*", "urn:cgi:feature:GSV:Mine:361068"}, 2);

        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsGreaterThanOrEqualTo/ogc:Literal", new String[] {"01/JAN/1870"}, 1);
        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLessThanOrEqualTo/ogc:Literal", new String[] {"31/DEC/1885"}, 1);

        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsGreaterThan/ogc:Literal", new String[] {"1"}, 1);
    }

}

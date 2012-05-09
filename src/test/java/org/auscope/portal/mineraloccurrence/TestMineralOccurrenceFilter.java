package org.auscope.portal.mineraloccurrence;


import org.auscope.portal.PortalTestClass;
import org.auscope.portal.server.domain.ogc.AbstractFilterTestUtilities;
import org.junit.Test;
import org.w3c.dom.Document;


/**
 * User: Michael Stegherr
 * Date: 30/03/2009
 * Time: 3:27:26 PM
 */
public class TestMineralOccurrenceFilter extends PortalTestClass {

    @Test
    public void testWithNameAndMinimumOreAmount() throws Exception {
        final String commodityName = "urn:cgi:feature:GSV:MineralOccurrence:361179";

        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter(commodityName, "Any", "1234567", "urn:ogc:def:uom:UCUM:t", "", "");

        String filter = mineralOccurrenceFilter.getFilterStringAllRecords();

        //Do some simple checks to ensure it is a) valid XML and b) referencing the values we put in
        Document doc = AbstractFilterTestUtilities.parsefilterStringXML(filter);

        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsGreaterThanOrEqualTo/ogc:Literal", new String[] {"1234567"}, 2);
        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/ogc:Literal", new String[] {commodityName, "urn:ogc:def:uom:UCUM:t"}, 4);
    }

    @Test
    public void testReserveMinimumOreAmount() throws Exception {
        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter("", "Reserve", "2000000", "urn:ogc:def:uom:UCUM:t", "", "");

        String filter = mineralOccurrenceFilter.getFilterStringAllRecords();
        Document doc = AbstractFilterTestUtilities.parsefilterStringXML(filter);
        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsGreaterThanOrEqualTo/ogc:Literal", new String[] {"2000000"}, 1);
        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/ogc:Literal", new String[] {"urn:ogc:def:uom:UCUM:t"}, 1);
    }

    @Test
    public void testAnyMinimumOreAmount() throws Exception {
        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter("", "Any", "1000000", "urn:ogc:def:uom:UCUM:t", "", "");

        String filter = mineralOccurrenceFilter.getFilterStringAllRecords();
        Document doc = AbstractFilterTestUtilities.parsefilterStringXML(filter);

        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsGreaterThanOrEqualTo/ogc:Literal", new String[] {"1000000"}, 2);
        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/ogc:Literal", new String[] {"urn:ogc:def:uom:UCUM:t"}, 2);
    }

    @Test
    public void testResourceMinimumCommodityAmount() throws Exception {
        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter("", "Resource", "", "", "6000000", "urn:ogc:def:uom:UCUM:t");

        String filter = mineralOccurrenceFilter.getFilterStringAllRecords();
        Document doc = AbstractFilterTestUtilities.parsefilterStringXML(filter);

        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/ogc:Literal", new String[] {"urn:ogc:def:uom:UCUM:t", "6000000"}, 2);
    }


    @Test
    public void testAnyMinimumCommodityAmount() throws Exception {
        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter("", "Any", "", "", "7000000", "urn:ogc:def:uom:UCUM:t");

        String filter = mineralOccurrenceFilter.getFilterStringAllRecords();
        Document doc = AbstractFilterTestUtilities.parsefilterStringXML(filter);

        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/ogc:Literal", new String[] {"urn:ogc:def:uom:UCUM:t", "7000000"}, 4);
    }

    /**
     * Ensures that matchCase is always set to false
     * @throws Exception
     */
    @Test
    public void testMatchCaseDefault() throws Exception {
        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter("commodityName", "None", "", "", "", "");

        String filter = mineralOccurrenceFilter.getFilterStringAllRecords();
        Document doc = AbstractFilterTestUtilities.parsefilterStringXML(filter);

        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/@matchCase", new String[] {"false"}, 1);
    }

}

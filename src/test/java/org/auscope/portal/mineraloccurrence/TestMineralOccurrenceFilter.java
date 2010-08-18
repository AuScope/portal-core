package org.auscope.portal.mineraloccurrence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Arrays;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import junit.framework.Assert;

import org.auscope.portal.Util;
import org.auscope.portal.server.domain.ogc.FilterTestUtilities;
import org.auscope.portal.server.domain.ogc.OGCNamespaceContext;
import org.auscope.portal.server.domain.wcs.WCSNamespaceContext;
import org.junit.Test;
import org.jmock.Mockery;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


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
    public void testWithNameAndMinimumOreAmount() throws Exception {
        final String commodityName = "urn:cgi:feature:GSV:MineralOccurrence:361179";
        
        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter(commodityName, "Any", "1234567", "urn:ogc:def:uom:UCUM:t", "", "");

        String filter = mineralOccurrenceFilter.getFilterStringAllRecords();
                
        //Do some simple checks to ensure it is a) valid XML and b) referencing the values we put in
        Document doc = FilterTestUtilities.parsefilterStringXML(filter);
        
        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsGreaterThanOrEqualTo/ogc:Literal", new String[] {"1234567"}, 2);
        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/ogc:Literal", new String[] {commodityName, "urn:ogc:def:uom:UCUM:t"}, 4);
    }
    
    @Test
    public void testReserveMinimumOreAmount() throws Exception {
        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter("", "Reserve", "2000000", "urn:ogc:def:uom:UCUM:t", "", "");
        
        String filter = mineralOccurrenceFilter.getFilterStringAllRecords();
        Document doc = FilterTestUtilities.parsefilterStringXML(filter);
        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsGreaterThanOrEqualTo/ogc:Literal", new String[] {"2000000"}, 1);
        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/ogc:Literal", new String[] {"urn:ogc:def:uom:UCUM:t"}, 1);
    }

    @Test
    public void testAnyMinimumOreAmount() throws Exception {
        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter("", "Any", "1000000", "urn:ogc:def:uom:UCUM:t", "", "");
        
        String filter = mineralOccurrenceFilter.getFilterStringAllRecords();
        Document doc = FilterTestUtilities.parsefilterStringXML(filter);
        
        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsGreaterThanOrEqualTo/ogc:Literal", new String[] {"1000000"}, 2);
        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/ogc:Literal", new String[] {"urn:ogc:def:uom:UCUM:t"}, 2);
    }

    @Test
    public void testResourceMinimumCommodityAmount() throws Exception {
        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter("", "Resource", "", "", "6000000", "urn:ogc:def:uom:UCUM:t");
        
        String filter = mineralOccurrenceFilter.getFilterStringAllRecords();
        Document doc = FilterTestUtilities.parsefilterStringXML(filter);
        
        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/ogc:Literal", new String[] {"urn:ogc:def:uom:UCUM:t", "6000000"}, 2);
    }


    @Test
    public void testAnyMinimumCommodityAmount() throws Exception {
        MineralOccurrenceFilter mineralOccurrenceFilter =
            new MineralOccurrenceFilter("", "Any", "", "", "7000000", "urn:ogc:def:uom:UCUM:t");
        
        String filter = mineralOccurrenceFilter.getFilterStringAllRecords();
        Document doc = FilterTestUtilities.parsefilterStringXML(filter);
        
        FilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsEqualTo/ogc:Literal", new String[] {"urn:ogc:def:uom:UCUM:t", "7000000"}, 4);
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

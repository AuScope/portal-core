package org.auscope.portal.server.web.controllers;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * User: Mathew Wyatt
 * Date: 23/03/2009
 * Time: 12:50:56 PM
 */
public class TestMineralOccurrencesFilterController {

    @Before
    public void setup() {

    }

    @Test
    public void testGetAllForMine() throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {
        MineralOccurrencesFilterController minOccController = new MineralOccurrencesFilterController();
        minOccController.doMineralOccurrenceFilter("http://www.gsv-tb.dpi.vic.gov.au/AuScope-MineralOccurrence/services?", "Dominion Copper Mine");
    }

}

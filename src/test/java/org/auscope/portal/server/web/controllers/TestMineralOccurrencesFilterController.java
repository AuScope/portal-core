package org.auscope.portal.server.web.controllers;

import org.junit.Before;
import org.junit.Test;

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
    public void testGetAllForMine() throws IOException {
        MineralOccurrencesFilterController minOccController = new MineralOccurrencesFilterController();
        minOccController.doMineralOccurrenceFilter("http://www.gsv-tb.dpi.vic.gov.au/AuScope-MineralOccurrence/services?", "Dominion Copper Mine");
    }

}

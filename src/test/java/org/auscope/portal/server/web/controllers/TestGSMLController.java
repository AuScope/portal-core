package org.auscope.portal.server.web.controllers;

import org.junit.Before;
import org.junit.Test;
import org.jmock.Mockery;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;
import org.auscope.portal.server.web.service.CSWService;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.util.GmlToKml;

/**
 * User: Mathew Wyatt
 * Date: 27/08/2009
 * Time: 4:59:56 PM
 */
public class TestGSMLController {

    /**
     * JMock context
     */
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    /**
     * Mock httpService caller
     */
    private HttpServiceCaller httpServiceCaller = context.mock(HttpServiceCaller.class);

    /**
     * Mock gml to kml converter
     */
    private GmlToKml gmlToKml = context.mock(GmlToKml.class);

    /**
     * The controller to test
     */
    private GSMLController gsmlController;

    @Before
    public void setup() {
        gsmlController = new GSMLController(httpServiceCaller, gmlToKml);
    }

    /**
     * Test that all classes are invoked correctly and return valid JSON
     */
    /*@Test
    public void testGetAllFeatures() throws Exception {
        context.checking(new Expectations() {{
            oneOf(httpServiceCaller).getHttpClient();
        }});

        gsmlController.requestAllFeatures("fake", "fake", null);
    }*/
}

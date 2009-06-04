package org.auscope.portal.server.web.mineraloccurrence;

import org.auscope.portal.server.web.HttpServiceCaller;
import org.junit.Before;
import org.junit.Test;
import org.jmock.Mockery;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;
import org.apache.commons.httpclient.methods.GetMethod;

import java.util.Collection;

import junit.framework.Assert;

/**
 * Created by IntelliJ IDEA.
 * User: Mathew Wyatt
 * Date: Jun 4, 2009
 * Time: 11:41:09 AM
 */
public class TestMineralOccurrenceServiceClient {
    private MineralOccurrenceServiceClient mineralOccurrenceServiceClient;
    private HttpServiceCaller httpServiceCaller;
    private MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler;
    private Mockery context = new Mockery(){{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};


    @Before
    public void setup() {
        this.mineralOccurrencesResponseHandler = context.mock(MineralOccurrencesResponseHandler.class);
        this.httpServiceCaller = context.mock(HttpServiceCaller.class);
        this.mineralOccurrenceServiceClient = new MineralOccurrenceServiceClient(this.httpServiceCaller, this.mineralOccurrencesResponseHandler);
    }

    /**
     * The service client ties various different classes togather, so we are not testing its data integrity,
     * rather we are testing that it utilises the other classes properly and send us back the right return object
     *
     * @throws Exception
     */
    @Test
    public void testGetAllMines() throws Exception {
        final String serviceURL = "http://localhost?";

        final GetMethod mockMethod = context.mock(GetMethod.class);
        final String mockMineResponse = new String();
        final Collection<Mine> mockMines = (Collection<Mine>)context.mock(Collection.class);

        context.checking(new Expectations() {{
            oneOf (httpServiceCaller).constructWFSGetFeatureMethod(serviceURL, "mo:Mine", ""); will(returnValue(mockMethod));
            oneOf (httpServiceCaller).callGetMethod(mockMethod); will(returnValue(mockMineResponse));
            oneOf (mineralOccurrencesResponseHandler).getMines(mockMineResponse); will(returnValue(mockMines));
        }});

        Collection<Mine> mines = this.mineralOccurrenceServiceClient.getAllMines(serviceURL);
        Assert.assertEquals(mockMines, mines);
    }

    /**
     * The service client ties various different classes togather, so we are not testing its data integrity,
     * rather we are testing that it utilises the other classes properly and send us back the right return object
     *
     * @throws Exception
     */
    @Test
    public void getMineWithSpecifiedName() throws Exception {
        final String serviceURL = "http://localhost?";
        final String mineName = "SomeName";

        final MineFilter mineFilter = new MineFilter(mineName);
        final GetMethod mockMethod = context.mock(GetMethod.class);
        final String mockMineResponse = new String();
        final Collection<Mine> mockMines = (Collection<Mine>)context.mock(Collection.class);

        context.checking(new Expectations() {{
            oneOf (httpServiceCaller).constructWFSGetFeatureMethod(serviceURL, "mo:Mine", mineFilter.getFilterString()); will(returnValue(mockMethod));
            oneOf (httpServiceCaller).callGetMethod(mockMethod); will(returnValue(mockMineResponse));
            oneOf (mineralOccurrencesResponseHandler).getMines(mockMineResponse); will(returnValue(mockMines));
        }});

        Collection<Mine> mines = this.mineralOccurrenceServiceClient.getMineWithSpecifiedName(serviceURL, mineName);
        Assert.assertEquals(mockMines, mines);
    }
}

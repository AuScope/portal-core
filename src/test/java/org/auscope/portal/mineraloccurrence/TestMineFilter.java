package org.auscope.portal.mineraloccurrence;

import java.io.IOException;

import junit.framework.Assert;

import org.auscope.portal.PortalTestClass;
import org.auscope.portal.server.domain.ogc.AbstractFilterTestUtilities;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * User: Mathew Wyatt
 * Date: 25/03/2009
 * Time: 8:23:09 AM
 */
public class TestMineFilter extends PortalTestClass {

    /**
     * Test without mine name. If there is no name specified then all of the mines should be queried.
     */
    @Test
    public void testWithNoMineName() throws IOException {
        MineFilter mineFilter = new MineFilter("");

        String filter = mineFilter.getFilterStringAllRecords();
        Assert.assertEquals("<ogc:Filter><ogc:PropertyIsLike escapeChar=\"!\" wildCard=\"*\" matchCase=\"false\" singleChar=\"#\" ><ogc:PropertyName>er:specification/er:Mine/gml:name</ogc:PropertyName><ogc:Literal>*</ogc:Literal></ogc:PropertyIsLike></ogc:Filter>", filter);
    }

    /**
     *  Test with a mine name. A filter query should be generated searching for mines with the given name.
     */
    @Test
    public void testWithAMineName() throws Exception {
        MineFilter mineFilter = new MineFilter("Dominion Copper Mine");

        String filter = mineFilter.getFilterStringAllRecords();
        Document doc = AbstractFilterTestUtilities.parsefilterStringXML(filter);

        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLike/ogc:PropertyName",
                new String[] {"er:specification/er:Mine/er:mineName/er:MineName/er:mineName"}, 1);
        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLike/ogc:Literal",
                new String[] {"*", "Dominion Copper Mine"}, 1);

    }

    /**
     * Tests to ensure match case is set to false
     */
    @Test
    public void testMatchCaseDefault() throws Exception {
        MineFilter mineFilter = new MineFilter("mineNameToTest");

        String filter = mineFilter.getFilterStringAllRecords();
        Document doc = AbstractFilterTestUtilities.parsefilterStringXML(filter);
        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLike/@matchCase", new String[] {"false"}, 1);
    }

}

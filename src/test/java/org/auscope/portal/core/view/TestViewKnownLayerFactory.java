package org.auscope.portal.core.view;

import java.awt.Dimension;
import java.awt.Point;

import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.view.knownlayer.KnownLayer;
import org.auscope.portal.core.view.knownlayer.KnownLayerSelector;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.ui.ModelMap;

public class TestViewKnownLayerFactory extends PortalTestClass {
    
    private KnownLayerSelector mockSelector = context.mock(KnownLayerSelector.class);

    /**
     * Tests the optional params include/exclude values as appropriate
     */
    @Test
    public void testToViewOptionalParams() {
        double x = 1.0;
        double y = 5.0;
        double width = 32.0;
        double height = 16.0;

        Point p1 = new Point();
        p1.setLocation(x, y);
        Dimension d1 = new Dimension();
        d1.setSize(width, height);

        final ViewKnownLayerFactory factory = new ViewKnownLayerFactory();
        final KnownLayer knownLayer = new KnownLayer("id", mockSelector);

        //Test with no optional params
        ModelMap model = factory.toView(knownLayer);
        Assert.assertFalse(model.containsKey("iconUrl"));
        Assert.assertFalse(model.containsKey("iconAnchor"));
        Assert.assertFalse(model.containsKey("iconSize"));

        //add our optional params
        knownLayer.setIconUrl("http://icon.url.test");
        knownLayer.setIconSize(d1);
        knownLayer.setIconAnchor(p1);
        knownLayer.setNagiosHostGroup("nagios-host-group");

        //Test with all optional params
        model = factory.toView(knownLayer);
        Assert.assertTrue(model.containsKey("iconUrl"));
        Assert.assertTrue(model.containsKey("iconAnchor"));
        Assert.assertTrue(model.containsKey("iconSize"));

        Assert.assertNotNull(model.get("iconAnchor"));
        Assert.assertTrue(model.get("iconAnchor") instanceof ModelMap);
        ModelMap iconAnchor = (ModelMap) model.get("iconAnchor");
        Assert.assertEquals(x, (Double) iconAnchor.get("x"), 0.01);
        Assert.assertEquals(y, (Double) iconAnchor.get("y"), 0.01);

        Assert.assertNotNull(model.get("iconSize"));
        Assert.assertTrue(model.get("iconSize") instanceof ModelMap);
        ModelMap iconSize = (ModelMap) model.get("iconSize");
        Assert.assertEquals(width, (Double) iconSize.get("width"), 0.01);
        Assert.assertEquals(height, (Double) iconSize.get("height"), 0.01);

        Assert.assertEquals("nagios-host-group", model.get("nagiosHostGroup"));
    }
}

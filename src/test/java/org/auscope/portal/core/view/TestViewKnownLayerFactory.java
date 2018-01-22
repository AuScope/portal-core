package org.auscope.portal.core.view;

import java.awt.Dimension;
import java.awt.Point;

import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.view.knownlayer.KnownLayer;
import org.auscope.portal.core.view.knownlayer.KnownLayerSelector;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.ui.ModelMap;

public class TestViewKnownLayerFactory extends PortalTestClass {

    private Point mockP1 = context.mock(Point.class, "p1");
    private Dimension mockD1 = context.mock(Dimension.class, "d1");
    private KnownLayerSelector mockSelector = context.mock(KnownLayerSelector.class);

    /**
     * Tests the optional params include/exclude values as appropriate
     */
    @Test
    public void testToViewOptionalParams() {
        final ViewKnownLayerFactory factory = new ViewKnownLayerFactory();
        final KnownLayer knownLayer = new KnownLayer("id", mockSelector);
        final double x = 1.0;
        final double y = 5.0;
        final double width = 32.0;
        final double height = 16.0;

        context.checking(new Expectations() {
            {
                oneOf(mockP1).getX();
                will(returnValue(x));
                oneOf(mockP1).getY();
                will(returnValue(y));
                oneOf(mockD1).getWidth();
                will(returnValue(width));
                oneOf(mockD1).getHeight();
                will(returnValue(height));
            }
        });

        //Test with no optional params
        ModelMap model = factory.toView(knownLayer);
        Assert.assertFalse(model.containsKey("iconUrl"));
        Assert.assertFalse(model.containsKey("iconAnchor"));
        Assert.assertFalse(model.containsKey("iconSize"));

        //add our optional params
        knownLayer.setIconUrl("http://icon.url.test");
        knownLayer.setIconSize(mockD1);
        knownLayer.setIconAnchor(mockP1);
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

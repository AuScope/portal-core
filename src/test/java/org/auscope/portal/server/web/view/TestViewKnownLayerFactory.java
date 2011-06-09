package org.auscope.portal.server.web.view;

import java.awt.Dimension;
import java.awt.Point;

import org.auscope.portal.server.web.KnownLayerKeywords;
import org.auscope.portal.server.web.KnownLayerWFS;
import org.auscope.portal.server.web.KnownLayerWMS;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.springframework.ui.ModelMap;

public class TestViewKnownLayerFactory {
	private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    private KnownLayerWFS mockWFS = context.mock(KnownLayerWFS.class);
    private KnownLayerWMS mockWMS = context.mock(KnownLayerWMS.class);
    private KnownLayerKeywords mockKeywords = context.mock(KnownLayerKeywords.class);
    private Point mockP1 = context.mock(Point.class, "p1");
    private Point mockP2 = context.mock(Point.class, "p2");
    private Dimension mockD1 = context.mock(Dimension.class, "d1");

    /**
     * Tests a full conversion
     * @throws Exception
     */
    @Test
    public void testToViewWFS() throws Exception {
    	ViewKnownLayerFactory factory = new ViewKnownLayerFactory();

    	final String featureTypeName = "ftn";
    	final String title = "co";
    	final String description = "asb";
    	final String proxyUrl = "http://bob.xom";
    	final String iconUrl = "http://bob.xom.foo.bar";
    	final String[] serviceEndpoints = new String[]{"http://bob.xox"};
    	final String id = "eyedee";
    	final boolean disableBboxFiltering = false;
    	final boolean hidden = false;
    	final boolean includeEndpoints = true;
    	final String group = "mygroup";

    	final double anchorX = 0.1;
    	final double anchorY = 0.2;

    	final double infoWindowAnchorX = 0.3;
    	final double infoWindowAnchorY = 0.4;

    	final double iconSizeWidth = 45;
    	final double iconSizeHeight = 46;

    	final ModelMap expectation = new ModelMap();
    	final ModelMap anchorExpectation = new ModelMap();
    	final ModelMap infoExpectation = new ModelMap();
    	final ModelMap sizeExpectation = new ModelMap();
    	final String[] relatedFeatureTypeNames = new String[] {"rft1", "rft2"};

    	expectation.put("type", "KnownLayerWFS");
    	expectation.put("featureTypeName", featureTypeName);
    	expectation.put("hidden", hidden);
    	expectation.put("title", title);
    	expectation.put("description", description);
    	expectation.put("proxyUrl", proxyUrl);
    	expectation.put("iconUrl", iconUrl);
    	expectation.put("iconAnchor", anchorExpectation);
    	expectation.put("infoWindowAnchor", infoExpectation);
    	expectation.put("iconSize", sizeExpectation);
    	expectation.put("id", id);
    	expectation.put("disableBboxFiltering", disableBboxFiltering);
    	expectation.put("relatedNames", relatedFeatureTypeNames);
    	expectation.put("serviceEndpoints", serviceEndpoints);
    	expectation.put("includeEndpoints", includeEndpoints);
    	expectation.put("group", group);

    	anchorExpectation.put("x", anchorX);
    	anchorExpectation.put("y", anchorY);

    	infoExpectation.put("x", infoWindowAnchorX);
    	infoExpectation.put("y", infoWindowAnchorY);

    	sizeExpectation.put("width", iconSizeWidth);
    	sizeExpectation.put("height", iconSizeHeight);


    	context.checking(new Expectations() {{

    		allowing(mockWFS).getFeatureTypeName();will(returnValue(featureTypeName));
    		allowing(mockWFS).getId();will(returnValue(id));
    		allowing(mockWFS).getTitle();will(returnValue(title));
    		allowing(mockWFS).getDescription();will(returnValue(description));
    		allowing(mockWFS).getProxyUrl();will(returnValue(proxyUrl));
    		allowing(mockWFS).getIconUrl();will(returnValue(iconUrl));
    		allowing(mockWFS).getIconAnchor();will(returnValue(mockP1));
    		allowing(mockWFS).getInfoWindowAnchor();will(returnValue(mockP2));
    		allowing(mockWFS).getIconSize();will(returnValue(mockD1));
    		allowing(mockWFS).getDisableBboxFiltering();will(returnValue(disableBboxFiltering));
    		allowing(mockWFS).isHidden();will(returnValue(hidden));
    		allowing(mockWFS).getRelatedFeatureTypeNames();will(returnValue(relatedFeatureTypeNames));
            allowing(mockWFS).getServiceEndpoints();will(returnValue(serviceEndpoints));
            allowing(mockWFS).includeEndpoints();will(returnValue(includeEndpoints));
            allowing(mockWFS).getGroup();will(returnValue(group));
            
    		allowing(mockP1).getX();will(returnValue(anchorX));
    		allowing(mockP1).getY();will(returnValue(anchorY));

    		allowing(mockP2).getX();will(returnValue(infoWindowAnchorX));
    		allowing(mockP2).getY();will(returnValue(infoWindowAnchorY));

    		allowing(mockD1).getWidth();will(returnValue(iconSizeWidth));
    		allowing(mockD1).getHeight();will(returnValue(iconSizeHeight));
        }});

    	ModelMap result = factory.toView(mockWFS);

    	AssertViewUtility.assertModelMapsEqual(expectation,result);
    }

    /**
     * Tests with all optional components removed
     * @throws Exception
     */
    @Test
    public void testToViewWFSOptional() throws Exception {
    	ViewKnownLayerFactory factory = new ViewKnownLayerFactory();

    	final String featureTypeName = "ftn";
    	final String title = "co";
    	final String description = "asb";
    	final String proxyUrl = "http://bob.xom";
    	final String iconUrl = "http://bob.xom.foo.bar";
    	final String[] serviceEndpoints = null;
    	final String id = "eyedee";
    	final boolean disableBboxFiltering = true;
    	final boolean hidden = true;
    	final String[] relatedFeatureTypeNames = null; 
    	final String group = "mygroup";
    	final boolean includeEndpoints = false;

    	final ModelMap expectation = new ModelMap();

    	expectation.put("type", "KnownLayerWFS");
    	expectation.put("hidden", hidden);
    	expectation.put("featureTypeName", featureTypeName);
    	expectation.put("title", title);
    	expectation.put("description", description);
    	expectation.put("proxyUrl", proxyUrl);
    	expectation.put("iconUrl", iconUrl);
    	expectation.put("id", id);
    	expectation.put("disableBboxFiltering", disableBboxFiltering);
    	expectation.put("relatedFeatureTypeNames", relatedFeatureTypeNames);
    	expectation.put("serviceEndpoints", serviceEndpoints);
    	expectation.put("includeEndpoints", includeEndpoints);
    	expectation.put("group", group);
    	
    	context.checking(new Expectations() {{

    		allowing(mockWFS).getFeatureTypeName();will(returnValue(featureTypeName));
    		allowing(mockWFS).getId();will(returnValue(id));
    		allowing(mockWFS).getTitle();will(returnValue(title));
    		allowing(mockWFS).getDescription();will(returnValue(description));
    		allowing(mockWFS).getProxyUrl();will(returnValue(proxyUrl));
    		allowing(mockWFS).getIconUrl();will(returnValue(iconUrl));
    		allowing(mockWFS).getIconAnchor();will(returnValue(null));
    		allowing(mockWFS).getInfoWindowAnchor();will(returnValue(null));
    		allowing(mockWFS).getIconSize();will(returnValue(null));
    		allowing(mockWFS).getDisableBboxFiltering();will(returnValue(disableBboxFiltering));
    		allowing(mockWFS).isHidden();will(returnValue(hidden));
    		allowing(mockWFS).getRelatedFeatureTypeNames();will(returnValue(null));
    		allowing(mockWFS).getServiceEndpoints();will(returnValue(null));
    		allowing(mockWFS).includeEndpoints();will(returnValue(includeEndpoints));
    		allowing(mockWFS).getGroup();will(returnValue(group));
        }});

    	ModelMap result = factory.toView(mockWFS);

    	AssertViewUtility.assertModelMapsEqual(expectation,result);
    }

    @Test
    public void testToViewWMS() throws Exception {
        ViewKnownLayerFactory factory = new ViewKnownLayerFactory();

        final String layerName = "ftn";
        final String title = "aasdsad";
        final String description = "asb";
        final String styleName = "styleThatispretty";
        final String id = "eyedee";
        final boolean hidden = false;
        final String group = "mygroup";
        final String[] relatedLayers = new String[] {"a", "b"};

        final ModelMap expectation = new ModelMap();

        expectation.put("type", "KnownLayerWMS");
        expectation.put("hidden", hidden);
        expectation.put("layerName", layerName);
        expectation.put("title", title);
        expectation.put("description", description);
        expectation.put("styleName", styleName);
        expectation.put("id", id);
        expectation.put("group", group);
        expectation.put("relatedNames", relatedLayers);

        context.checking(new Expectations() {{

            allowing(mockWMS).getId();will(returnValue(id));
            allowing(mockWMS).getLayerName();will(returnValue(layerName));
            allowing(mockWMS).getTitle();will(returnValue(title));
            allowing(mockWMS).getDescription();will(returnValue(description));
            allowing(mockWMS).getStyleName();will(returnValue(styleName));
            allowing(mockWMS).isHidden();will(returnValue(hidden));
            allowing(mockWMS).getGroup();will(returnValue(group));
            allowing(mockWMS).getRelatedLayerNames();will(returnValue(relatedLayers));
        }});

        ModelMap result = factory.toView(mockWMS);

        AssertViewUtility.assertModelMapsEqual(expectation,result);
    }

    @Test
    public void testToViewKeywords() throws Exception {
        ViewKnownLayerFactory factory = new ViewKnownLayerFactory();

        final String title = "aasdsad";
        final String description = "asb";
        final String descriptiveKeyword = "myKeyword";
        final String id = "eyedee";
        final String iconUrl = "http://maps.google.com/mapfiles/kml/paddle/blu-blank.png";
        final boolean hidden = true;
        final String group = "mygroup";

    	final double anchorX = 1.0;
    	final double anchorY = 1.0;

    	final double iconSizeWidth = 16;
    	final double iconSizeHeight = 16;

    	final ModelMap anchorExpectation = new ModelMap();
    	final ModelMap sizeExpectation = new ModelMap();

        final ModelMap expectation = new ModelMap();

        expectation.put("type", "KnownLayerKeywords");
        expectation.put("hidden", hidden);
        expectation.put("descriptiveKeyword", descriptiveKeyword);
        expectation.put("title", title);
        expectation.put("description", description);
        expectation.put("id", id);
        expectation.put("iconUrl", iconUrl);
    	expectation.put("iconAnchor", anchorExpectation);
    	expectation.put("iconSize", sizeExpectation);
    	expectation.put("group", group);

    	anchorExpectation.put("x", anchorX);
    	anchorExpectation.put("y", anchorY);
    	sizeExpectation.put("width", iconSizeWidth);
    	sizeExpectation.put("height", iconSizeHeight);

        context.checking(new Expectations() {{

            allowing(mockKeywords).getId();will(returnValue(id));
            allowing(mockKeywords).getTitle();will(returnValue(title));
            allowing(mockKeywords).getDescription();will(returnValue(description));
            allowing(mockKeywords).getDescriptiveKeyword();will(returnValue(descriptiveKeyword));
            allowing(mockKeywords).getIconUrl();will(returnValue(iconUrl));
            allowing(mockKeywords).getIconAnchor();will(returnValue(mockP1));
            allowing(mockKeywords).getIconSize();will(returnValue(mockD1));
            allowing(mockKeywords).isHidden();will(returnValue(hidden));
            allowing(mockKeywords).getGroup();will(returnValue(group));

    		allowing(mockP1).getX();will(returnValue(anchorX));
    		allowing(mockP1).getY();will(returnValue(anchorY));

    		allowing(mockD1).getWidth();will(returnValue(iconSizeWidth));
    		allowing(mockD1).getHeight();will(returnValue(iconSizeHeight));
        }});

        ModelMap result = factory.toView(mockKeywords);

        AssertViewUtility.assertModelMapsEqual(expectation,result);
    }
}

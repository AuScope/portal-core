package org.auscope.portal.server.web.view;

import java.awt.Dimension;
import java.awt.Point;

import org.auscope.portal.server.web.KnownLayer;
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
    	final String id = "eyedee";
    	final boolean disableBboxFiltering = false;
    	
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
    	
    	expectation.put("type", "KnownLayerWFS");
    	expectation.put("featureTypeName", featureTypeName);
    	expectation.put("title", title);
    	expectation.put("description", description);
    	expectation.put("proxyUrl", proxyUrl);
    	expectation.put("iconUrl", iconUrl);
    	expectation.put("iconAnchor", anchorExpectation);
    	expectation.put("infoWindowAnchor", infoExpectation);
    	expectation.put("iconSize", sizeExpectation);
    	expectation.put("id", id);
    	expectation.put("disableBboxFiltering", disableBboxFiltering);
    	
    	anchorExpectation.put("x", anchorX);
    	anchorExpectation.put("y", anchorY);
    	
    	infoExpectation.put("x", infoWindowAnchorX);
    	infoExpectation.put("y", infoWindowAnchorY);
    	
    	sizeExpectation.put("width", iconSizeWidth);
    	sizeExpectation.put("height", iconSizeHeight);
    	
    	
    	context.checking(new Expectations() {{
    		
    		oneOf(mockWFS).getFeatureTypeName();will(returnValue(featureTypeName));
    		oneOf(mockWFS).getId();will(returnValue(id));
    		oneOf(mockWFS).getTitle();will(returnValue(title));
    		oneOf(mockWFS).getDescription();will(returnValue(description));
    		oneOf(mockWFS).getProxyUrl();will(returnValue(proxyUrl));
    		oneOf(mockWFS).getIconUrl();will(returnValue(iconUrl));
    		oneOf(mockWFS).getIconAnchor();will(returnValue(mockP1));
    		oneOf(mockWFS).getInfoWindowAnchor();will(returnValue(mockP2));
    		oneOf(mockWFS).getIconSize();will(returnValue(mockD1));
    		oneOf(mockWFS).getDisableBboxFiltering();will(returnValue(disableBboxFiltering));
    		
    		
    		oneOf(mockP1).getX();will(returnValue(anchorX));
    		oneOf(mockP1).getY();will(returnValue(anchorY));
    		
    		oneOf(mockP2).getX();will(returnValue(infoWindowAnchorX));
    		oneOf(mockP2).getY();will(returnValue(infoWindowAnchorY));
    		
    		oneOf(mockD1).getWidth();will(returnValue(iconSizeWidth));
    		oneOf(mockD1).getHeight();will(returnValue(iconSizeHeight));
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
    	final String id = "eyedee";
    	final boolean disableBboxFiltering = true;
    	
    	final ModelMap expectation = new ModelMap();
    	
    	expectation.put("type", "KnownLayerWFS");
    	expectation.put("featureTypeName", featureTypeName);
    	expectation.put("title", title);
    	expectation.put("description", description);
    	expectation.put("proxyUrl", proxyUrl);
    	expectation.put("iconUrl", iconUrl);
    	expectation.put("id", id);
    	expectation.put("disableBboxFiltering", disableBboxFiltering);
    	
    	context.checking(new Expectations() {{
    		
    		oneOf(mockWFS).getFeatureTypeName();will(returnValue(featureTypeName));
    		oneOf(mockWFS).getId();will(returnValue(id));
    		oneOf(mockWFS).getTitle();will(returnValue(title));
    		oneOf(mockWFS).getDescription();will(returnValue(description));
    		oneOf(mockWFS).getProxyUrl();will(returnValue(proxyUrl));
    		oneOf(mockWFS).getIconUrl();will(returnValue(iconUrl));
    		oneOf(mockWFS).getIconAnchor();will(returnValue(null));
    		oneOf(mockWFS).getInfoWindowAnchor();will(returnValue(null));
    		oneOf(mockWFS).getIconSize();will(returnValue(null));
    		oneOf(mockWFS).getDisableBboxFiltering();will(returnValue(disableBboxFiltering));
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
        
        final ModelMap expectation = new ModelMap();
        
        expectation.put("type", "KnownLayerWMS");
        expectation.put("layerName", layerName);
        expectation.put("title", title);
        expectation.put("description", description);
        expectation.put("styleName", styleName);
        expectation.put("id", id);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWMS).getId();will(returnValue(id));
            oneOf(mockWMS).getLayerName();will(returnValue(layerName));
            oneOf(mockWMS).getTitle();will(returnValue(title));
            oneOf(mockWMS).getDescription();will(returnValue(description));
            oneOf(mockWMS).getStyleName();will(returnValue(styleName));
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
        
    	final double anchorX = 1.0;
    	final double anchorY = 1.0;
    	
    	final double iconSizeWidth = 16;
    	final double iconSizeHeight = 16;
    	
    	final ModelMap anchorExpectation = new ModelMap();
    	final ModelMap sizeExpectation = new ModelMap();
        
        final ModelMap expectation = new ModelMap();
        
        expectation.put("type", "KnownLayerKeywords");
        expectation.put("descriptiveKeyword", descriptiveKeyword);
        expectation.put("title", title);
        expectation.put("description", description);
        expectation.put("id", id);
        expectation.put("iconUrl", iconUrl);
    	expectation.put("iconAnchor", anchorExpectation);
    	expectation.put("iconSize", sizeExpectation);
        
    	anchorExpectation.put("x", anchorX);
    	anchorExpectation.put("y", anchorY);    	
    	sizeExpectation.put("width", iconSizeWidth);
    	sizeExpectation.put("height", iconSizeHeight);
        
        context.checking(new Expectations() {{
            
            oneOf(mockKeywords).getId();will(returnValue(id));
            oneOf(mockKeywords).getTitle();will(returnValue(title));
            oneOf(mockKeywords).getDescription();will(returnValue(description));
            oneOf(mockKeywords).getDescriptiveKeyword();will(returnValue(descriptiveKeyword));
            oneOf(mockKeywords).getIconUrl();will(returnValue(iconUrl));
            oneOf(mockKeywords).getIconAnchor();will(returnValue(mockP1));
            oneOf(mockKeywords).getIconSize();will(returnValue(mockD1));            
            
    		oneOf(mockP1).getX();will(returnValue(anchorX));
    		oneOf(mockP1).getY();will(returnValue(anchorY));
    				
    		oneOf(mockD1).getWidth();will(returnValue(iconSizeWidth));
    		oneOf(mockD1).getHeight();will(returnValue(iconSizeHeight));
        }});
        
        ModelMap result = factory.toView(mockKeywords);
        
        AssertViewUtility.assertModelMapsEqual(expectation,result);
    }
}

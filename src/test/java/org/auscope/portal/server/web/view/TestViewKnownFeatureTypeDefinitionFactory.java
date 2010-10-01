package org.auscope.portal.server.web.view;

import java.awt.Dimension;
import java.awt.Point;

import org.auscope.portal.server.web.KnownFeatureTypeDefinition;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.springframework.ui.ModelMap;

public class TestViewKnownFeatureTypeDefinitionFactory {
	private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private KnownFeatureTypeDefinition mockDefn = context.mock(KnownFeatureTypeDefinition.class);
    private Point mockP1 = context.mock(Point.class, "p1");
    private Point mockP2 = context.mock(Point.class, "p2");
    private Dimension mockD1 = context.mock(Dimension.class, "d1");
    
    /**
     * Tests a full conversion
     * @throws Exception
     */
    @Test
    public void testToView() throws Exception {
    	ViewKnownFeatureTypeDefinitionFactory factory = new ViewKnownFeatureTypeDefinitionFactory();
    	
    	final String featureTypeName = "ftn";
    	final String displayName = "co";
    	final String description = "asb";
    	final String proxyUrl = "http://bob.xom";
    	final String iconUrl = "http://bob.xom.foo.bar";
    	
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
    	
    	expectation.put("featureTypeName", featureTypeName);
    	expectation.put("displayName", displayName);
    	expectation.put("description", description);
    	expectation.put("proxyUrl", proxyUrl);
    	expectation.put("iconUrl", iconUrl);
    	expectation.put("iconAnchor", anchorExpectation);
    	expectation.put("infoWindowAnchor", infoExpectation);
    	expectation.put("iconSize", sizeExpectation);
    	
    	anchorExpectation.put("x", anchorX);
    	anchorExpectation.put("y", anchorY);
    	
    	infoExpectation.put("x", infoWindowAnchorX);
    	infoExpectation.put("y", infoWindowAnchorY);
    	
    	sizeExpectation.put("width", iconSizeWidth);
    	sizeExpectation.put("height", iconSizeHeight);
    	
    	
    	context.checking(new Expectations() {{
    		
    		oneOf(mockDefn).getFeatureTypeName();will(returnValue(featureTypeName));
    		oneOf(mockDefn).getDisplayName();will(returnValue(displayName));
    		oneOf(mockDefn).getDescription();will(returnValue(description));
    		oneOf(mockDefn).getProxyUrl();will(returnValue(proxyUrl));
    		oneOf(mockDefn).getIconUrl();will(returnValue(iconUrl));
    		oneOf(mockDefn).getIconAnchor();will(returnValue(mockP1));
    		oneOf(mockDefn).getInfoWindowAnchor();will(returnValue(mockP2));
    		oneOf(mockDefn).getIconSize();will(returnValue(mockD1));
    		
    		oneOf(mockP1).getX();will(returnValue(anchorX));
    		oneOf(mockP1).getY();will(returnValue(anchorY));
    		
    		oneOf(mockP2).getX();will(returnValue(infoWindowAnchorX));
    		oneOf(mockP2).getY();will(returnValue(infoWindowAnchorY));
    		
    		oneOf(mockD1).getWidth();will(returnValue(iconSizeWidth));
    		oneOf(mockD1).getHeight();will(returnValue(iconSizeHeight));
        }});
    	
    	ModelMap result = factory.toView(mockDefn);
    	
    	TestViewUtility.assertModelMapsEqual(expectation,result);
    }
    
    /**
     * Tests with all optional components removed
     * @throws Exception
     */
    @Test
    public void testToViewOptional() throws Exception {
    	ViewKnownFeatureTypeDefinitionFactory factory = new ViewKnownFeatureTypeDefinitionFactory();
    	
    	final String featureTypeName = "ftn";
    	final String displayName = "co";
    	final String description = "asb";
    	final String proxyUrl = "http://bob.xom";
    	final String iconUrl = "http://bob.xom.foo.bar";
    	
    	final ModelMap expectation = new ModelMap();
    	
    	expectation.put("featureTypeName", featureTypeName);
    	expectation.put("displayName", displayName);
    	expectation.put("description", description);
    	expectation.put("proxyUrl", proxyUrl);
    	expectation.put("iconUrl", iconUrl);
    	
    	
    	context.checking(new Expectations() {{
    		
    		oneOf(mockDefn).getFeatureTypeName();will(returnValue(featureTypeName));
    		oneOf(mockDefn).getDisplayName();will(returnValue(displayName));
    		oneOf(mockDefn).getDescription();will(returnValue(description));
    		oneOf(mockDefn).getProxyUrl();will(returnValue(proxyUrl));
    		oneOf(mockDefn).getIconUrl();will(returnValue(iconUrl));
    		oneOf(mockDefn).getIconAnchor();will(returnValue(null));
    		oneOf(mockDefn).getInfoWindowAnchor();will(returnValue(null));
    		oneOf(mockDefn).getIconSize();will(returnValue(null));
        }});
    	
    	ModelMap result = factory.toView(mockDefn);
    	
    	TestViewUtility.assertModelMapsEqual(expectation,result);
    }
}

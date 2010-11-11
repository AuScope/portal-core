package org.auscope.portal.gsml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import junit.framework.Assert;

import org.junit.Test;

public class TestGSMLResponseHandler {
	GSMLResponseHandler gsmlResponseHandler = new GSMLResponseHandler();
	
	
	@Test
	public void testGetNumberOfFeaturesZero() throws Exception {
		File getFeatureResponse = new File("src/test/resources/YilgarnGeochemistryNoFeatureResponse.xml");
		BufferedReader reader = new BufferedReader( new FileReader(getFeatureResponse) );
        StringBuffer getFeatureResponseXML = new StringBuffer();

        String str;
        while ((str = reader.readLine()) != null) {
            getFeatureResponseXML.append(str);
        }
        reader.close();

        int numberOfFeatures = gsmlResponseHandler.getNumberOfFeatures(getFeatureResponseXML.toString());
        Assert.assertEquals("There are 0 features", 0, numberOfFeatures);
	}
	
	@Test
	public void testGetNumberOfFeaturesTwo() throws Exception {
		File geochemistryGetFeatureResponse = new File("src/test/resources/YilgarnGeochemGetFeatureResponse.xml");
        BufferedReader reader = new BufferedReader( new FileReader(geochemistryGetFeatureResponse) );
        StringBuffer geochemGetFeatureResponseXML = new StringBuffer();

        String str;
        while ((str = reader.readLine()) != null) {
            geochemGetFeatureResponseXML.append(str);
        }
        reader.close();

        int numberOfFeatures =
            gsmlResponseHandler.getNumberOfFeatures(geochemGetFeatureResponseXML.toString());

        Assert.assertEquals("There are 2 features", 2, numberOfFeatures);		
	}

}

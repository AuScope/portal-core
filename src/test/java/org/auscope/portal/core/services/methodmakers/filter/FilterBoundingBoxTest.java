/**
 * 
 */
package org.auscope.portal.core.services.methodmakers.filter;

import org.auscope.portal.core.server.GeoServerType;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

/**
 * Tests now that I've added GeoServerType to FilterBoundingBox
 * 
 * @author Brooke Smith
 *
 */
public class FilterBoundingBoxTest {

    private StringBuffer json = new StringBuffer();
    private double north = 20.0;
    private double south = -20.0;
    private double east = 40.0;
    private double west = 2.0;
    
    @Before
    public void init() {
        json.append("{");
        json.append("'crs':'EPSG:4326',");
        json.append("'northBoundLatitude':'"+north+"',");
        json.append("'southBoundLatitude':'"+south+"',");
        json.append("'eastBoundLongitude':'"+east+"',");
        json.append("'westBoundLongitude':'"+west+"'");
        json.append("}");
    }
    
    @Test
    public void testAttemptParseFromJSONArcGis() {
        FilterBoundingBox fbb =  FilterBoundingBox.attemptParseFromJSON(json.toString(), GeoServerType.ArcGis);
        Assert.assertEquals(fbb.getLowerCornerPoints()[0], south);
        Assert.assertEquals(fbb.getLowerCornerPoints()[1], west);
        Assert.assertEquals(fbb.getUpperCornerPoints()[0],north);
        Assert.assertEquals(fbb.getUpperCornerPoints()[1],east);
    }
    @Test
    public void testAttemptParseFromJSONGeoserver() {
        FilterBoundingBox fbb =  FilterBoundingBox.attemptParseFromJSON(json.toString(), GeoServerType.GeoServer);
        Assert.assertEquals(fbb.getLowerCornerPoints()[0], west);
        Assert.assertEquals(fbb.getLowerCornerPoints()[1], south);
        Assert.assertEquals(fbb.getUpperCornerPoints()[0], east);
        Assert.assertEquals(fbb.getUpperCornerPoints()[1], north);
    }

}

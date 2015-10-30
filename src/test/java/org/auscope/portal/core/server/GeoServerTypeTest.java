/**
 * 
 */
package org.auscope.portal.core.server;

import org.junit.Assert;
import org.junit.Test;


/**
 * @author u86990
 *
 */
public class GeoServerTypeTest {

    @Test
    public void testArcGis1() {
        Assert.assertEquals(GeoServerType.ArcGis, GeoServerType.parseUrl("http://server:9939/blah/bing/bam/WFSServer"));
    }

    @Test
    public void testArcGis2() {
        Assert.assertEquals(GeoServerType.ArcGis, GeoServerType.parseUrl("http://server:9939/blah/bing/bam/WFSServer?"));
    }
    @Test
    public void testArcGis3() {
        Assert.assertEquals(GeoServerType.ArcGis, GeoServerType.parseUrl("http://server:9939/blah/bing/bam/WFSServer?arg=value&arg=value"));
    }
    @Test
    public void testGeoserver1() {
        Assert.assertEquals(GeoServerType.GeoServer, GeoServerType.parseUrl("http://server:9939/blah/bing/bam/wfs"));
    }

    @Test
    public void testGeoserver2() {
        Assert.assertEquals(GeoServerType.GeoServer, GeoServerType.parseUrl("http://server:9939/blah/bing/bam/wfs?"));
    }
    @Test
    public void testGeoserver3() {
        Assert.assertEquals(GeoServerType.GeoServer, GeoServerType.parseUrl("http://server:9939/blah/bing/bam/wfs?arg=value&arg=value"));
    }

}

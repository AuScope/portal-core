package org.auscope.portal.server.domain.ows;

import org.auscope.portal.server.util.Util;
import org.junit.Test;
import org.w3c.dom.Document;

public class TestOWSExceptionParser {
    
    private void check(String path) throws Exception {
        final String xmlString = org.auscope.portal.Util.loadXML(path);
        
        Util util = new Util();
        
        Document doc = util.buildDomFromString(xmlString);
        
        OWSExceptionParser.checkForExceptionResponse(doc);
    }
    
    @Test(expected=OWSException.class)
    public void testThrowException1() throws Exception {
        check("src/test/resources/OWSExceptionSample1.xml");
    }
    
    @Test(expected=OWSException.class)
    public void testThrowException2() throws Exception {
        check("src/test/resources/GetMineError.xml");
    }
    
    @Test
    public void testDontThrowException() throws Exception {
        check("src/test/resources/DescribeCoverageResponse1.xml");
        check("src/test/resources/DescribeCoverageResponse2.xml");
        check("src/test/resources/GetMineralOccurrencesWithSpecifiedEndowmentCutOffGrade.xml");
        check("src/test/resources/GetMineralOccurrencesWithSpecifiedReserveMinimumOreAmount.xml");
        check("src/test/resources/GetMiningActivity-AssociatedMineDateRangeProducedMaterial.xml");
    }
}

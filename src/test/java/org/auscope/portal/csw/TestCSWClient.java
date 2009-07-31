package org.auscope.portal.csw;

import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

/**
 * User: Mathew Wyatt
 * Date: 23/02/2009
 * Time: 1:11:22 PM
 */
public class TestCSWClient {

    /*@Test
    public void testManyRecords() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        //String filter = "<?xml+version=\"1.0\"+encoding=\"UTF-8\"?><Filter+xmlns=\"http://www.opengis.net/ogc\"+xmlns:gml=\"http://www.opengis.net/gml\"><And><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>WFS</Literal></PropertyIsEqualTo><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>GPS</Literal></PropertyIsEqualTo></And></Filter>&constraintLanguage=FILTER&constraint_language_version=1.1.0";
        //String filter = "<?xml+version=\"1.0\"+encoding=\"UTF-8\"?><Filter+xmlns=\"http://www.opengis.net/ogc\"+xmlns:gml=\"http://www.opengis.net/gml\"><And><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>WFS</Literal></PropertyIsEqualTo><PropertyIsEqualTo><PropertyName>keyword</PropertyName><Literal>mo%3AMiningFeatureOccurrence</Literal></PropertyIsEqualTo></And></Filter>&constraintLanguage=FILTER&constraint_language_version=1.1.0";

        String filter =
            "<?xml+version=\"1.0\"+encoding=\"UTF-8\"?>" +
                "<Filter+xmlns=\"http://www.opengis.net/ogc\"+xmlns:gml=\"http://www.opengis.net/gml\">" +
                "<PropertyIsEqualTo>" +
                    "<PropertyName>keyword</PropertyName>" +
                    "<Literal>WFS</Literal>" +
                "</PropertyIsEqualTo>" +
            "</Filter>" +
            "&constraintLanguage=FILTER&constraint_language_version=1.1.0";
        
        CSWRecord[] cswRecords =
            new CSWClient("http://auscope-portal.arrc.csiro.au/geonetwork/srv/en/csw", filter)
                    .getRecordResponse().getCSWRecords();
        
        for(CSWRecord record : cswRecords)
            System.out.println(record.getServiceName() + " " + record.getServiceUrl());

    }*/
}

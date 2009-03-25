package org.auscope.portal.server.web.controllers;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.parsers.ParserConfigurationException;


/**
 * User: Mathew Wyatt
 * Date: 23/03/2009
 * Time: 12:50:56 PM
 */
public class TestMineralOccurrencesFilterController {

    @Before
    public void setup() {

    }

    @Test
    public void testGetAllForMine() throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {
        MineralOccurrencesFilterController minOccController = new MineralOccurrencesFilterController();
        minOccController.doMineralOccurrenceFilter("http://www.gsv-tb.dpi.vic.gov.au/AuScope-MineralOccurrence/services?", "Dominion Copper Mine", "", "", "", "", "", "", null);
    }
    @Test 
    public void testConvertToKML() {
       MineralOccurrencesFilterController minOccController = new MineralOccurrencesFilterController();
       
       //C:\Projects\AuScope-Portal
       String basePath = new File("").getAbsolutePath();
       //C:\Projects\AuScope-Portal\src\main\webapp\WEB-INF\xsl\ma.xml    mine.xml
       //String testRunner = "file:///" + basePath + "/src/test/js/testRunner.html";
       //String testSuite  = new File(new URI("file:///" + basePath.replace("\\", "/") + "/src/main/webapp/WEB-INF/xsl/ma.xml")).getAbsolutePath();
       //String testSuite1  = new File(new URI("file:///" + basePath.replace("\\", "/") + "/src/main/webapp/WEB-INF/xsl/mine.xml")).getAbsolutePath();
       String mineFilePath = basePath + "/src/main/webapp/WEB-INF/xsl/mine.xml";
       String maFilePath = basePath + "/src/main/webapp/WEB-INF/xsl/ma.xml";
       String s = new String();
       
       try {
          BufferedReader inMine = new BufferedReader (new FileReader(mineFilePath));
          BufferedReader inMA   = new BufferedReader (new FileReader(maFilePath));
          try {
             while ((s = inMine.readLine()) != null )
                System.out.println(s);

                System.out.println("---------------------");
                
             while ((s = inMA.readLine()) != null )
                System.out.println(s);
             
                System.out.println("========---------------------");                
          } catch(IOException iox) {
             System.out.println("File read error...");
             iox.printStackTrace();
          }
       
          System.out.println("....Calling ...minOccController.convertToKML");
          minOccController.convertToKML(new FileInputStream(mineFilePath), 
                                        new FileInputStream(maFilePath),
                                        null);
          
       } catch (FileNotFoundException e) {
          System.out.println("File not found...");
       }
       
    }
}

package org.auscope.portal.server.util;

import org.apache.log4j.Logger;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;

/**
 * <p> This class converts geoSciMl into KML format </p>
 * @author jsanders
 */
public class GmlToKml {
   private static final Logger log = Logger.getLogger(GmlToKml.class);
   
   /**
    * Utility method to merge xml files.
    * @param geoXML file to be converted in geoSciMl format
    * @param inXSLT XSLT stylesheet
    * @return 
    */   
   public static String convert(String geoXML, InputStream inXSLT) {
      log.debug("....Convert");
      //log.debug(geoXML);
      
      StringWriter sw = new StringWriter();
      try {
         TransformerFactory tFactory = TransformerFactory.newInstance();
         Transformer transformer = tFactory.newTransformer (new StreamSource(inXSLT));
         transformer.transform (new StreamSource (new StringReader(geoXML)),
                                new StreamResult (sw));
      } catch (TransformerException e) {
         e.printStackTrace();
      }     

      return sw.toString();
      
   }
}

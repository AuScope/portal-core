package org.auscope.portal.server.util;

import java.io.*;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

/**
 * <p> This class converts geoSciMl into KML format </p>
 * @author jsanders
 */
@Repository
public class GmlToKml {
   private static final Log log = LogFactory.getLog(GmlToKml.class);
   
   /**
    * Utility method to merge xml files.
    * @param geoXML file to be converted in geoSciMl format
    * @param inXSLT XSLT stylesheet
    * @return 
    */   
   public String convert(String geoXML, InputStream inXSLT) {
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

    /**
     * Utility method specific to Auscope Portal
      * @param geoXML
     * @param httpRequest
     */
   public String convert(String geoXML, HttpServletRequest httpRequest) {
        InputStream inXSLT = httpRequest.getSession().getServletContext().getResourceAsStream("/WEB-INF/xsl/kml.xsl");
        return this.convert(geoXML, inXSLT);
   }
}

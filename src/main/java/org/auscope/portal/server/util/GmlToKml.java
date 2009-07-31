package org.auscope.portal.server.util;

import java.io.*;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;

/**
 * <p> This class converts geoSciML into KML format </p>
 * @author jsanders
 */
@Component
public class GmlToKml {
   private static final Log log = LogFactory.getLog(GmlToKml.class);

   @Autowired
   @Qualifier(value = "propertyConfigurer")
   private PortalPropertyPlaceholderConfigurer hostConfigurer;
   
   
   /**
    * Utility method to transform xml file. It is kml.xml specific as it sets
    * uriResolverURL parameter for the stylesheet.
    * @param geoXML file to be converted in geoSciMl format
    * @param inXSLT XSLT stylesheet
    * @return Xml output string
    */   
   public String convert(String geoXML, InputStream inXSLT) {
      log.debug("GML input: " + geoXML);      
      StringWriter sw = new StringWriter();
      try {
         // Use the static TransformerFactory.newInstance() method:
         // TransformerFactory tFactory = TransformerFactory.newInstance();
         // to instantiate a TransformerFactory. 
         // The javax.xml.transform.TransformerFactory system property setting 
         // determines the actual class to instantiate:
         // org.apache.xalan.transformer.TransformerImpl.
         // However, we prefer Saxon...
         TransformerFactory tFactory = new net.sf.saxon.TransformerFactoryImpl();
         log.debug ("XSLT implementation in use: " + tFactory.getClass()); 
         
         // Use the TransformerFactory to instantiate a transformer that will 
         // work with the style sheet we specify. This method call also processes 
         // the style sheet into a compiled Templates object.        
         Transformer transformer 
            = tFactory.newTransformer (new StreamSource(inXSLT));
         
         // Set stylesheet parameter 
         transformer.setParameter 
            ("uriResolverURL"
            , hostConfigurer.resolvePlaceholder("HOST.uriResolver.url"));
         
         // Write the output to a stream
         transformer.transform (new StreamSource (new StringReader(geoXML)),
                                new StreamResult (sw));

      } catch (TransformerConfigurationException tce) {
         log.error(tce);
      } catch (TransformerException e) {
         log.error("Failed to transform kml file: " + e);
      }     
      log.debug("KML output: " + sw.toString());
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

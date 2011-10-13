package org.auscope.portal.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.support.ServletContextResource;

/**
 * URIResolver that feeds the correct locations of documents referenced
 * in xsl:inlude, xsl:import tags in the source xslt documents.
 *
 * Example usage:
 * TransformerFactory factory = TransformerFactory.newInstance();
 * factory.setURIResolver(new XmlResolver(servletContext));
 * Transformer transformer = factory.newTransformer(new StreamSource(oXSLTStream));
 *
 * @version $Id$
 */
public class PortalURIResolver implements URIResolver {

   protected final Log log = LogFactory.getLog(getClass());

   //Servlet Context to pull the file from
   private ServletContext servletContext;

   //The Default path to look for the file
   private String defaultPath = "/WEB-INF/xsl/";

   //Simple Cache to improve speed
   private Map<String,Source> cache = new HashMap<String,Source>();

   /**
    * Create a URIResolver
    * @param servletContext the javax.servlet.ServletContext
    */
   public PortalURIResolver(ServletContext servletContext){
      this.servletContext = servletContext;
   }

   /**
    * Called by the processor when it encounters an xsl:include, xsl:import,
    * or document() function.
    * @param href The relative or absolute URI. May be an empty string. May
    * contain a fragment identifier starting with "#", which must be the value
    * of an ID attribute in the referenced XML document.
    * @param base The base URI that should be used. May be null if uri is absolute.
    */
   public Source resolve(String href, String base) throws TransformerException {

      log.debug("Invoking XmlResolver");
      Source source = null;

      if (cache.containsKey(href)) {
         source = (Source)cache.get(href);
      } else {
         ServletContextResource servletContextResource = null;
         InputStream oXSLTStream = null;
         try {
            servletContextResource = new ServletContextResource(servletContext, defaultPath + href);
            oXSLTStream = servletContextResource.getInputStream();
            source = new StreamSource(oXSLTStream);

             //cache the source
             cache.put(href, source);

         } catch (IOException e) {
            log.error("Unable to Access Xml stylesheet from PortalUriResolver", e);
         }
      }
      return source;
   }

   public String getDefaultPath() {
      return defaultPath;
   }

   public void setDefaultPath(String defaultPath) {
      this.defaultPath = defaultPath;
   }
}

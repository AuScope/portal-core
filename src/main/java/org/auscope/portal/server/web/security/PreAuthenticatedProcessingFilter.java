package org.auscope.portal.server.web.security;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import org.springframework.security.ui.FilterChainOrder;
import org.springframework.security.ui.preauth.AbstractPreAuthenticatedProcessingFilter;

/**
 * This AbstractPreAuthenticatedProcessingFilter implementation 
 * obtains the username from request header pre-populated by an 
 * external Shibboleth authentication system.
 * 
 * 
 * @author san218
 * @version $Id$
 */
public class PreAuthenticatedProcessingFilter 
   extends AbstractPreAuthenticatedProcessingFilter {

   protected final Logger logger = Logger.getLogger(getClass());
   
   protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
      java.util.Enumeration eHeaders = request.getHeaderNames();
      while(eHeaders.hasMoreElements()) {
         String name = (String) eHeaders.nextElement();
         if ( ( name.matches(".*Shib.*") || name.matches(".*shib.*") ) && 
              !name.equals("HTTP_SHIB_ATTRIBUTES") && 
              !name.equals("Shib-Attributes") ) 
         {
               Object object = request.getHeader(name);
               String value = object.toString();
               logger.debug(name + " : " + value);
         }
      }
      //logger.debug("Shib-Person-commonName: " + request.getHeader("Shib-Person-commonName"));
      logger.info("Shib-Person-mail: " + request.getHeader("Shib-Person-mail"));
      //logger.debug("Shib-Shared-Token: " + request.getHeader("Shib-Shared-Token"));
      request.getSession().setAttribute("Shib-Shared-Token", request.getHeader("Shib-Shared-Token"));
      request.getSession().setAttribute("Shib-Person-commonName", request.getHeader("Shib-Person-commonName"));
      return request.getHeader("Shib-Person-mail");
   }
   
   protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
      // no password - user is already authenticated
      return "NONE";
   }
   
   public int getOrder() {
      return FilterChainOrder.PRE_AUTH_FILTER;
   }   
}

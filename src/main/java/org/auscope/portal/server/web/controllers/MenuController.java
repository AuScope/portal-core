package org.auscope.portal.server.web.controllers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller that handles all {@link Menu}-related requests,
 *
 * @author Jarek Sanders
 */

@Controller
public class MenuController {
   
   protected final Log logger = LogFactory.getLog(getClass());
   
   /* For the time being we are redirecting Home link to AuScope site
   @RequestMapping("/home.html")
   public ModelAndView menu() {
      logger.info("menu controller started!");
      return new ModelAndView("home");
   }
   */
   
   @RequestMapping("/gmap.html")
   public ModelAndView gmap() {
      return new ModelAndView("gmap");
   }
   
   @RequestMapping("/login.html")
   public ModelAndView login() {
      return new ModelAndView("login");
   }
}

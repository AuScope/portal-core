package org.auscope.portal.server.web.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class GMapController extends AbstractController {

   protected final Log logger = LogFactory.getLog(getClass());	

   @Override
   protected ModelAndView handleRequestInternal(HttpServletRequest request,
         HttpServletResponse response) throws Exception {
      // TODO Auto-generated method stub
      logger.info("started!");

      ModelAndView mav = new ModelAndView("gmap");
				
      mav.addObject("centerLat",Float.valueOf("-25.56545"));
      mav.addObject("centerLon",Float.valueOf("133.12301"));
		
      logger.info("completed");
      return mav;
   }
}

package org.auscope.portal.server.web.controllers;

import java.awt.Menu;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

   @Autowired
   @Qualifier(value = "propertyConfigurer")
   private PortalPropertyPlaceholderConfigurer hostConfigurer;

   @RequestMapping("/gmap.html")
   public ModelAndView gmap() {
      String googleKey
         = hostConfigurer.resolvePlaceholder("HOST.googlemap.key");
      String vocabServiceUrl
         = hostConfigurer.resolvePlaceholder("HOST.vocabService.url");
      String maxFeatureValue
           = hostConfigurer.resolvePlaceholder("HOST.maxFeatures.value");

      logger.debug("googleKey: " + googleKey);
      logger.debug("vocabServiceUrl: " + vocabServiceUrl);
      logger.debug("maxFeatureValue: " + maxFeatureValue);

      ModelAndView mav = new ModelAndView("gmap");
      mav.addObject("googleKey", googleKey);
      mav.addObject("vocabServiceUrl", vocabServiceUrl);
      mav.addObject("maxFeatureValue", maxFeatureValue);
      return mav;
   }

   @RequestMapping("/mosaic_image.html")
   public ModelAndView mosaic_image() {
      String googleKey
         = hostConfigurer.resolvePlaceholder("HOST.googlemap.key");
      logger.debug(googleKey);

      ModelAndView mav = new ModelAndView("mosaic_image");
      mav.addObject("googleKey",googleKey);
      return mav;
   }

   @RequestMapping("/plotted_images.html")
   public ModelAndView plotted_images() {
      String googleKey
         = hostConfigurer.resolvePlaceholder("HOST.googlemap.key");
      logger.debug(googleKey);

      ModelAndView mav = new ModelAndView("plotted_images");
      mav.addObject("googleKey",googleKey);
      return mav;
   }

   @RequestMapping("/links.html")
   public ModelAndView links() {
      return new ModelAndView("links");
   }

   @RequestMapping("/about.html")
   public ModelAndView about(HttpServletRequest request) {

      String appServerHome = request.getSession().getServletContext().getRealPath("/");
      File manifestFile = new File(appServerHome,"META-INF/MANIFEST.MF");
      Manifest mf = new Manifest();
      ModelAndView mav = new ModelAndView("about");
      try {
         mf.read(new FileInputStream(manifestFile));
         Attributes atts = mf.getMainAttributes();
         if (mf != null) {
            mav.addObject("specificationTitle", atts.getValue("Specification-Title"));
            mav.addObject("implementationVersion", atts.getValue("Implementation-Version"));
            mav.addObject("implementationBuild", atts.getValue("Implementation-Build"));
            mav.addObject("buildDate", atts.getValue("buildDate"));
            mav.addObject("buildJdk", atts.getValue("Build-Jdk"));
            mav.addObject("javaVendor", atts.getValue("javaVendor"));
            mav.addObject("builtBy", atts.getValue("Built-By"));
            mav.addObject("osName", atts.getValue("osName"));
            mav.addObject("osVersion", atts.getValue("osVersion"));

            mav.addObject("serverName", request.getServerName());
            mav.addObject("serverInfo", request.getSession().getServletContext().getServerInfo());
            mav.addObject("serverJavaVersion", System.getProperty("java.version"));
            mav.addObject("serverJavaVendor", System.getProperty("java.vendor"));
            mav.addObject("javaHome", System.getProperty("java.home"));
            mav.addObject("serverOsArch", System.getProperty("os.arch"));
            mav.addObject("serverOsName", System.getProperty("os.name"));
            mav.addObject("serverOsVersion", System.getProperty("os.version"));
         }
      } catch (IOException e) {
         /* ignore, since we'll just leave an empty form */
          logger.debug(e.getMessage());
      }
      return mav;
   }
}

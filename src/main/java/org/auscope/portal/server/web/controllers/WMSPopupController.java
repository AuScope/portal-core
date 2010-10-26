package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Controller that handles GetFeatureInfo WMS requests
 * 
 * @author Jarek Sanders
 * @version $Id$ - %I%, %G%
 */

@Controller
public class WMSPopupController {
   protected final Log log = LogFactory.getLog(getClass());
   
   @RequestMapping("/wmsMarkerPopup.do")
   public void wmsUnitPopup(HttpServletRequest request,
                            HttpServletResponse response,
                            @RequestParam("WMS_URL") String wms_url,
                            @RequestParam("lat") String latitude,
                            @RequestParam("lng") String longitude,
                            @RequestParam("QUERY_LAYERS") String query_layers,
                            @RequestParam("x") String x,
                            @RequestParam("y") String y,
                            @RequestParam("BBOX") String bbox, 
                            @RequestParam("WIDTH") String width,
                            @RequestParam("HEIGHT") String height,
                            @RequestParam("INFO_FORMAT") String infoFormat) throws IOException 
   {   
      String AMP = "&";
      String url = wms_url;
      // "&" character cannot be passed within url string
      // We need this check for tricky urls such as Geoscience Australia.
      if ( !url.endsWith("?") || !url.endsWith("&") || !url.endsWith("=")) {
         if (url.contains("?"))
            url += AMP;
         else
            url += "?";
      }
      url += "REQUEST=GetFeatureInfo&EXCEPTIONS=application/vnd.ogc.se_xml";
      url += "&VERSION=1.1.0";
      url += "&BBOX=" + bbox;
      url += "&X=" + x + "&Y=" + y;
      url += "&INFO_FORMAT=" + infoFormat;
      url += "&QUERY_LAYERS=" + query_layers;
      url += "&FEATURE_COUNT=50";
      url += "&SRS=EPSG:4326";
      url += "&LAYERS=" + query_layers;
      url += "&STYLES=";      // Ask server for default style
      url += "&WIDTH=" + width + "&HEIGHT=" + height;
      url += "&FORMAT=image/png";
      log.debug(url);

      HttpServiceCaller serviceCaller = new HttpServiceCaller();
      String responseFromCall = serviceCaller.callHttpUrlGET(new URL(url));
      
      log.debug(responseFromCall);
      
      // Send response back to client
      response.getWriter().write(responseFromCall);
   }
}

package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.util.FileIOUtil;
import org.auscope.portal.server.web.WMSMethodMaker;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.springframework.beans.factory.annotation.Autowired;
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
public class WMSPopupController extends BasePortalController {
   private final Log log = LogFactory.getLog(getClass());

   private HttpServiceCaller serviceCaller;

   /**
    * Creates a new controller
    * @param serviceCaller will be used for making external requests
    */
   @Autowired
   public WMSPopupController(HttpServiceCaller serviceCaller) {
       this.serviceCaller = serviceCaller;
   }

   /**
    *
    * @param request
    * @param response
    * @param wmsUrl
    * @param latitude
    * @param longitude
    * @param queryLayers
    * @param x
    * @param y
    * @param bbox A CSV string formatted in the form - longitude,latitude,longitude,latitude
    * @param width
    * @param height
    * @param infoFormat
    * @throws Exception
    */
   @RequestMapping("/wmsMarkerPopup.do")
   public void wmsUnitPopup(HttpServletRequest request,
                            HttpServletResponse response,
                            @RequestParam("WMS_URL") String wmsUrl,
                            @RequestParam("lat") String latitude,
                            @RequestParam("lng") String longitude,
                            @RequestParam("QUERY_LAYERS") String queryLayers,
                            @RequestParam("x") String x,
                            @RequestParam("y") String y,
                            @RequestParam("BBOX") String bbox,
                            @RequestParam("WIDTH") String width,
                            @RequestParam("HEIGHT") String height,
                            @RequestParam("INFO_FORMAT") String infoFormat) throws Exception {

      String[] bboxParts = bbox.split(",");
      double lng1 = Double.parseDouble(bboxParts[0]);
      double lng2 = Double.parseDouble(bboxParts[2]);
      double lat1 = Double.parseDouble(bboxParts[1]);
      double lat2 = Double.parseDouble(bboxParts[3]);


      WMSMethodMaker methodMaker = new WMSMethodMaker(wmsUrl);
      HttpMethodBase method = methodMaker.getFeatureInfo(infoFormat,
              queryLayers,
              "EPSG:4326",
              Math.min(lng1, lng2), //west
              Math.min(lat1, lat2), //south
              Math.max(lng1, lng2), //east
              Math.max(lat1, lat2), //north
              Integer.parseInt(width),
              Integer.parseInt(height),
              Double.parseDouble(longitude),
              Double.parseDouble(latitude),
              Integer.parseInt(x),
              Integer.parseInt(y),
              "");


      InputStream responseStream = null;

      try {
          responseStream = serviceCaller.getMethodResponseAsStream(method, serviceCaller.getHttpClient());
          this.writeInputToOutputStream(responseStream, response.getOutputStream(), 1024*1024);
      } finally {
          if (responseStream != null) {
              responseStream.close();
          }
      }
   }
}

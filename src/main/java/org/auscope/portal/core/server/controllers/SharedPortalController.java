package org.auscope.portal.core.server.controllers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.WMSService;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.core.view.ViewCSWRecordFactory;
import org.auscope.portal.core.view.ViewKnownLayerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SharedPortalController {
    private WMSService wmsService;
    private final Log log = LogFactory.getLog(getClass());
    protected static int BUFFERSIZE = 1024 * 1024;
    HttpServiceCaller serviceCaller;

    // ----------------------------------------------------------- Constructors

    @Autowired
    public SharedPortalController(WMSService wmsService, ViewCSWRecordFactory viewCSWRecordFactory,
            ViewKnownLayerFactory knownLayerFact, HttpServiceCaller serviceCaller) {
        //super(viewCSWRecordFactory, knownLayerFact);
        this.wmsService = wmsService;
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
    * @param bbox
    *            A CSV string formatted in the form - longitude,latitude,longitude,latitude
    * @param width
    * @param height
    * @param infoFormat
    * @param sld_body
    *            - sld_body
    * @param postMethod
    *            Use getfeatureinfo POST method rather then GET
    * @param version
    *            - the wms version to use
    * @throws Exception
    */
   //@RequestMapping(value = "/wmsMarkerPopup.do", method = {RequestMethod.GET, RequestMethod.POST})
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
           @RequestParam("INFO_FORMAT") String infoFormat,
           @RequestParam(value = "SLD_BODY", defaultValue = "") String sldBody,
           @RequestParam(value = "postMethod", defaultValue = "false") Boolean postMethod,
           @RequestParam("version") String version,
           @RequestParam(value = "feature_count", defaultValue = "0") String feature_count) throws Exception {

       String[] bboxParts = bbox.split(",");
       double lng1 = Double.parseDouble(bboxParts[0]);
       double lng2 = Double.parseDouble(bboxParts[2]);
       double lat1 = Double.parseDouble(bboxParts[1]);
       double lat2 = Double.parseDouble(bboxParts[3]);

       String responseString = wmsService.getFeatureInfo(wmsUrl, infoFormat, queryLayers, "EPSG:3857",
               Math.min(lng1, lng2), Math.min(lat1, lat2), Math.max(lng1, lng2), Math.max(lat1, lat2),
               Integer.parseInt(width), Integer.parseInt(height), Double.parseDouble(longitude),
               Double.parseDouble(latitude),
               (int) (Double.parseDouble(x)), (int) (Double.parseDouble(y)), "", sldBody, postMethod, version,
               feature_count, true);
       //VT: Ugly hack for the GA wms layer in registered tab as its font is way too small at 80.
       //VT : GA style sheet also mess up the portal styling of tables as well.
       if (responseString.contains("table, th, td {")) {
           responseString = responseString.replaceFirst("table, th, td \\{",
                   ".ausga table, .ausga th, .ausga td {");
           responseString = responseString.replaceFirst("th, td \\{", ".ausga th, .ausga td {");
           responseString = responseString.replaceFirst("th \\{", ".ausga th {");
           responseString = responseString.replace("<table", "<table class='ausga'");
       }

       InputStream responseStream = new ByteArrayInputStream(responseString.getBytes());
       FileIOUtil.writeInputToOutputStream(responseStream, response.getOutputStream(), BUFFERSIZE, true);
   }    

}

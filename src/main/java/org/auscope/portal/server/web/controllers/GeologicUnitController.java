package org.auscope.portal.server.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.auscope.portal.server.web.HttpServiceCaller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: mat
 * Date: 28/04/2009
 * Time: 8:56:08 PM
 * To change this template use File | Settings | File Templates.
 */

@Controller
public class GeologicUnitController {

    @RequestMapping("/geologicUnitPopup.do")
    public void geologicUnitPopup(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @RequestParam("lat") String latitude,
                                  @RequestParam("lng") String longitude) throws IOException {

        //String url = "http://www.gsv-tb.dpi.vic.gov.au/AuScope-GeoSciML/services?service=WFS&version=1.1.0&request=GetFeature&typeName=gsml:GeologicUnit&outputFormat=text/xml;subtype=geoscimlhtml&filter=<ogc:Filter xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gsml=\"urn:cgi:xmlns:CGI:GeoSciML:2.0\"><ogc:BBOX><ogc:PropertyName>gsml:occurrence/gsml:MappedFeature/gsml:shape</ogc:PropertyName><gml:Envelope srsName=\"EPSG:4326\"><gml:lowerCorner>"+longitude+" "+latitude+"</gml:lowerCorner><gml:upperCorner>"+longitude+" "+latitude+"</gml:upperCorner></gml:Envelope></ogc:BBOX></ogc:Filter>";
        String url = "http://www.gsv-tb.dpi.vic.gov.au/AuScope-GeoSciML/services?service=WFS&version=1.1.0&request=GetFeature&outputFormat=text/xml;%20subtype=geoscimlhtml&featureid=gsml.geologicunit.16777549126932018";

        HttpServiceCaller serviceCaller = new HttpServiceCaller();
        BufferedInputStream responseFromCall = serviceCaller.callHttpUrlGet(url);
        response.getWriter().write(serviceCaller.responseToString(responseFromCall));
    }
}

package org.auscope.portal.server.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.auscope.portal.server.web.HttpServiceCaller;
import org.auscope.portal.server.util.PortalURIResolver;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: Mathew Wyatt
 * Date: 28/04/2009
 * Time: 8:56:08 PM
 */

@Controller
public class GeologicUnitController {
    private Logger logger = Logger.getLogger(getClass());

    @Autowired
    @Qualifier(value = "propertyConfigurer")
    private PortalPropertyPlaceholderConfigurer hostConfigurer;
    
    @RequestMapping("/geologicUnitPopup.do")
    public void geologicUnitPopup(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @RequestParam("lat") String latitude,
                                  @RequestParam("lng") String longitude) throws IOException {

        //deegree does not like fully encoded URLS, it only likes spaces to be encoded with %20
        //String url = "http://www.gsv-tb.dpi.vic.gov.au/AuScope-GeoSciML/services?service=WFS&version=1.1.0&request=GetFeature&typeName=gsml:GeologicUnit&filter=<ogc:Filter xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gsml=\"urn:cgi:xmlns:CGI:GeoSciML:2.0\"><ogc:BBOX><ogc:PropertyName>gsml:occurrence/gsml:MappedFeature/gsml:shape</ogc:PropertyName><gml:Envelope srsName=\"EPSG:4326\"><gml:lowerCorner>"+longitude+" "+latitude+"</gml:lowerCorner><gml:upperCorner>"+longitude+" "+latitude+"</gml:upperCorner></gml:Envelope></ogc:BBOX></ogc:Filter>";
        String url = hostConfigurer.resolvePlaceholder("gsv.gu.url") 
           + "WFS&version=1.1.0&request=GetFeature&typeName=gsml:GeologicUnit&filter=<ogc:Filter xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gsml=\"urn:cgi:xmlns:CGI:GeoSciML:2.0\"><ogc:BBOX><ogc:PropertyName>gsml:occurrence/gsml:MappedFeature/gsml:shape</ogc:PropertyName><gml:Envelope srsName=\"EPSG:4326\"><gml:lowerCorner>"+longitude+" "+latitude+"</gml:lowerCorner><gml:upperCorner>"+longitude+" "+latitude+"</gml:upperCorner></gml:Envelope></ogc:BBOX></ogc:Filter>";
        url = url.replace(" ", "%20");
        //String url = "http://www.gsv-tb.dpi.vic.gov.au/AuScope-GeoSciML/services?service=WFS&version=1.1.0&request=GetFeature&outputFormat=text/xml;%20subtype=geoscimlhtml&featureid=gsml.geologicunit.16777549126932018";

        HttpServiceCaller serviceCaller = new HttpServiceCaller();
        String responseFromCall = serviceCaller.callHttpUrlGET(new URL(url));
        //response.getWriter().write(serviceCaller.responseToString(responseFromCall));

        try {
            //String result = serviceCaller.responseToString(responseFromCall);
            StringWriter downThePipe = new StringWriter();
            InputStream in = request.getSession().getServletContext().getResourceAsStream("/WEB-INF/xsl/GeoSciMLtoHTML.xsl");

            // Use the static TransformerFactory.newInstance() method to instantiate
            // a TransformerFactory. The javax.xml.transform.TransformerFactory
            // system property setting determines the actual class to instantiate --
            // org.apache.xalan.transformer.TransformerImpl.
            TransformerFactory tFactory = TransformerFactory.newInstance();

            // Set an object that is used by default during the transformation 
            // to resolve URIs used in document(), xsl:import, or xsl:include.
            tFactory.setURIResolver(new PortalURIResolver(request.getSession().getServletContext()));

            StreamSource xslStream = new StreamSource(in);

            // Use the TransformerFactory to instantiate a transformer that will work with
            // the style sheet we specify. This method call also processes the style sheet
            // into a compiled Templates object.
            //Transformer transformer = tFactory.newTransformer(new javax.xml.transform.stream.StreamSource("kml.xsl"));
            Transformer transformer = tFactory.newTransformer (xslStream);

            // Setup a resolver handler to process each 'document()', 'xsl:include'
            // and 'xsl:import' instruction to pre-append a root dir.
            transformer.setURIResolver(tFactory.getURIResolver());
            
            
            // Use the transformer to apply the associated template object to an XML document
            // and write the output to a stream
            transformer.transform (new StreamSource (new StringReader(responseFromCall)),
                                   new StreamResult(downThePipe));

            // Send response back to client
            response.getWriter().println(downThePipe.toString());
        } catch (IOException ex) {
            logger.error("geologicUnitPopup: ", ex);
        } catch (TransformerException e) {
            logger.error("geologicUnitPopup: ", e);
        }
    }
}

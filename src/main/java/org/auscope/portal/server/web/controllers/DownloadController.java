package org.auscope.portal.server.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.Header;

import org.auscope.portal.server.web.service.HttpServiceCaller;

import javax.servlet.http.HttpServletResponse;

import java.text.SimpleDateFormat;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.util.Date;

import net.sf.json.JSONObject;

/**
 * User: Mathew Wyatt
 * Date: 02/09/2009
 * Time: 12:33:48 PM
 */

@Controller
public class DownloadController {
    protected final Log logger = LogFactory.getLog(getClass());
    private HttpServiceCaller serviceCaller;

    @Autowired
    public DownloadController(HttpServiceCaller serviceCaller) {
        this.serviceCaller = serviceCaller;        
    }

    /**
     * Given a list of URls, this function will collate the responses 
     * into a zip file and send the response back to the browser.
     *
     * @param serviceUrls
     * @param response
     * @throws Exception
     */
    @RequestMapping("/downloadGMLAsZip.do")
    public void downloadGMLAsZip( @RequestParam("serviceUrls") final String[] serviceUrls,
                                  HttpServletResponse response) throws Exception {

        //set the content type for zip files
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition","inline; filename=GMLDownload.zip;");

        //create the output stream
        ZipOutputStream zout = new ZipOutputStream(response.getOutputStream());

        logger.info("No. of serviceUrls: " + serviceUrls.length);

        for(int i=0; i<serviceUrls.length; i++) {

            GetMethod method = new GetMethod(serviceUrls[i]);
            HttpClient client = serviceCaller.getHttpClient();

            logger.info("Calling service: " + serviceUrls[i]);

            String responseString 
               = serviceCaller.getMethodResponseAsString(method, client);

            logger.info("Response: " + responseString);

            JSONObject jsonObject = JSONObject.fromObject( responseString );

            byte[] gmlBytes = JSONObject.fromObject(jsonObject.get("data")).get("gml").toString().getBytes();

            logger.info(gmlBytes.length);

            if(jsonObject.get("success").toString().equals("false"))
               zout.putNextEntry(new ZipEntry(new SimpleDateFormat((i+1) + "_yyyyMMdd_HHmmss").format(new Date()) + "-operation-failed.xml"));
            else {
                //create a new entry in the zip file with a timestamped name 
               zout.putNextEntry(new ZipEntry(new SimpleDateFormat((i+1) + "_yyyyMMdd_HHmmss").format(new Date()) + ".xml"));               
            }

            zout.write(gmlBytes);
            zout.closeEntry();
        }

        zout.finish();
        zout.flush();
        zout.close();
    }

    /**
     * Given a list of WMS URL's, this function will collate the responses 
     * into a zip file and send the response back to the browser.
     * 
     * @param serviceUrls
     * @param response
     * @throws Exception
     */
    @RequestMapping("/downloadWMSAsZip.do")
    public void downloadWMSAsZip( @RequestParam("serviceUrls") final String[] serviceUrls,
                                  HttpServletResponse response) throws Exception {

        //set the content type for zip files
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition","inline; filename=PNGDownload.zip;");

        //create the output stream
        ZipOutputStream zout = new ZipOutputStream(response.getOutputStream());

        for(int i=0; i<serviceUrls.length; i++) {

            GetMethod method = new GetMethod(serviceUrls[i]);
            HttpClient client = serviceCaller.getHttpClient();

            byte[] responseBytes = serviceCaller.getMethodResponseInBytes(method, client);

            Header contentType = serviceCaller.getResponseHeader(method, "Content-Type");

            //create a new entry in the zip file with a timestamped name
            if(contentType.getValue().contains("xml"))
                zout.putNextEntry(new ZipEntry(new SimpleDateFormat((i+1) + "_yyyyMMdd_HHmmss").format(new Date()) + ".xml"));
            else
                zout.putNextEntry(new ZipEntry(new SimpleDateFormat((i+1) + "_yyyyMMdd_HHmmss").format(new Date()) + ".png"));

            zout.write(responseBytes);
            zout.closeEntry();
        }

        zout.finish();
        zout.flush();
        zout.close();
    }

}

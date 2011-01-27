package org.auscope.portal.server.web.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

        logger.trace("No. of serviceUrls: " + serviceUrls.length);

        for(int i=0; i<serviceUrls.length; i++) {

            GetMethod method = new GetMethod(serviceUrls[i]);
            HttpClient client = serviceCaller.getHttpClient();

            logger.trace("Calling service: " + serviceUrls[i]);

            //Our request may fail (due to timeout or otherwise)
            String responseString = null;
            JSONObject jsonObject = null;
            try {
                responseString = serviceCaller.getMethodResponseAsString(method, client);
                
                logger.trace("Response: " + responseString);

                jsonObject = JSONObject.fromObject( responseString );
            } catch (Exception ex) {
                //Replace a failure exception with a JSONObject representing that exception
                logger.error(ex, ex);
                jsonObject = new JSONObject();
                jsonObject.put("msg", ex.getMessage());
                jsonObject.put("success", false);
                responseString = ex.toString();
            }

            //Extract our data (if it exists)
            byte[] gmlBytes = new byte[] {}; //The error response is an empty array
            Object dataObject = jsonObject.get("data");
            Object messageObject = jsonObject.get("msg"); //This will be used as an error string
            if (messageObject == null) {
            	messageObject = ""; 
            }
            if (dataObject != null) {
            	Object gmlResponseObject = JSONObject.fromObject(dataObject).get("gml");
            	
            	if (gmlResponseObject != null) {
            		gmlBytes = gmlResponseObject.toString().getBytes();
            	}
            }

            logger.trace(gmlBytes.length);

            if(jsonObject.get("success").toString().equals("false")) {
            	//The server may have returned an error message, if so, lets include it in the filename
            	String messageString = messageObject.toString();
            	if (messageString.length() == 0)
            		messageString = "operation-failed";
            	
            	//"Tidy" up the message
            	messageString = messageString.replace(' ', '_').replace(".", "");
            	
            	zout.putNextEntry(new ZipEntry(new SimpleDateFormat((i+1) + "_yyyyMMdd_HHmmss").format(new Date()) + "-" + messageString + ".xml"));
            } else {
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
     * @param filename
     * @param response
     * @throws Exception
     */
    @RequestMapping("/downloadDataAsZip.do")
    public void downloadDataAsZip( @RequestParam("serviceUrls") final String[] serviceUrls,
    				              @RequestParam("filename") final String filename,
                                  HttpServletResponse response) throws Exception {

    	String filenameStr = filename == null || filename.length() < 0 ? "DataDownload" : filename;
    	
        //set the content type for zip files
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition","inline; filename=" + filenameStr + ".zip;");

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

package org.auscope.portal.server.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.log4j.Logger;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.Header;
import org.auscope.portal.server.web.service.HttpServiceCaller;

import javax.servlet.http.HttpServletResponse;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.util.Date;

/**
 * User: Mathew Wyatt
 * Date: 02/09/2009
 * Time: 12:33:48 PM
 */

@Controller
public class DownloadController {
    private Logger logger = Logger.getLogger(getClass());
    private HttpServiceCaller serviceCaller;

    @Autowired
    public DownloadController(HttpServiceCaller serviceCaller) {
        this.serviceCaller = serviceCaller;        
    }

    /**
     * Given a list of URls, this function will collate the responses into a zip file and send the response back to the browser.
     *
     * @param serviceUrls
     * @param response
     * @throws Exception
     */
    @RequestMapping("/downloadAsZip.do")
    public void downloadAsZip(  @RequestParam("serviceUrls") final String[] serviceUrls,
                                HttpServletResponse response) throws Exception {

        //set the content type for zip files
        //response.setContentType("application/zip");
        //response.setHeader("Content-Disposition","inline; filename=output.zip;");

        //create the output stream
        //ZipOutputStream zout = new ZipOutputStream(response.getOutputStream());

        for(int i=0; i<serviceUrls.length; i++) {
            System.out.println(serviceUrls[i]);

            GetMethod method = new GetMethod(serviceUrls[i]);
            HttpClient client = serviceCaller.getHttpClient();

            byte[] responseBytes = serviceCaller.getMethodResponseInBytes(method, client);

            System.out.println(method.getResponseBodyAsString());

            Header contentType = method.getResponseHeader("Content-Type");
            System.out.println(contentType.getValue());

            /*//create a new entry in the zip file with a timestamped name
            if(contentType.value().equals("application/xml"))
                zout.putNextEntry(new ZipEntry(method.getName() + new Date().toString() +".xml"));
            else
                zout.putNextEntry(new ZipEntry(method.getName() + new Date().toString() +".xml"));

            zout.write(responseBytes);
            zout.closeEntry();*/
        }

        /*zout.finish();
        zout.flush();
        zout.close();*/
    }
}

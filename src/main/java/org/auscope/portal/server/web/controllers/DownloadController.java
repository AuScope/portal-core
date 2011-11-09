package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.manager.download.DownloadResponse;
import org.auscope.portal.manager.download.ServiceDownloadManager;
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
    private final Log logger = LogFactory.getLog(getClass());
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
    public void downloadGMLAsZip(
            @RequestParam("serviceUrls") final String[] serviceUrls,
            HttpServletResponse response) throws Exception {
        ExecutorService pool = Executors.newCachedThreadPool();
        downloadGMLAsZip(serviceUrls,response,pool);
    }


    public void downloadGMLAsZip(
            @RequestParam("serviceUrls") final String[] serviceUrls,
            HttpServletResponse response,ExecutorService threadpool) throws Exception {

        // set the content type for zip files
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition",
                "inline; filename=GMLDownload.zip;");

        // create the output stream
        ZipOutputStream zout = new ZipOutputStream(response.getOutputStream());

        logger.trace("No. of serviceUrls: " + serviceUrls.length);
        ServiceDownloadManager downloadManager = new ServiceDownloadManager(
                serviceUrls, serviceCaller,threadpool);
        //VT: threadpool is closed within downloadAll();
        ArrayList<DownloadResponse> gmlDownloads=downloadManager.downloadAll();
        writeResponseToZip(gmlDownloads,zout);

        zout.finish();
        zout.flush();
        zout.close();
    }

    private void writeResponseToZip(ArrayList<DownloadResponse> gmlDownloads,ZipOutputStream zout) throws IOException{
        StringBuilder errorMsg=new StringBuilder();

        for(int i=0;i<gmlDownloads.size();i++){
            DownloadResponse download=gmlDownloads.get(i);
            //Check that attempt to request is successful
            if(!download.hasException()){
                JSONObject jsonObject = JSONObject.fromObject(download.getResponseAsString());
                //check that JSON reply is successful
                if (jsonObject.get("success").toString().equals("false")) {
                    errorMsg.append("Unsuccessful JSON reply from: " + download.getRequestURL() + "\n");

                    Object messageObject = jsonObject.get("msg");
                    if(messageObject==null || messageObject.toString().length()==0){
                        errorMsg.append("No error message\n\n");
                    }else{
                        errorMsg.append(messageObject.toString() + "\n\n");
                    }
                }else{
                    byte[] gmlBytes = new byte[] {};
                    Object dataObject = jsonObject.get("data");
                    if (dataObject != null && !JSONNull.getInstance().equals(dataObject)) {
                        Object gmlResponseObject = JSONObject.fromObject(dataObject)
                                .get("gml");

                        if (gmlResponseObject != null) {
                            gmlBytes = gmlResponseObject.toString().getBytes();
                        }
                    }

                    zout.putNextEntry(new ZipEntry(new SimpleDateFormat(
                            (i + 1) + "_yyyyMMdd_HHmmss").format(new Date())
                            + ".xml"));
                    zout.write(gmlBytes);
                    zout.closeEntry();
                }

            }else{
                errorMsg.append("Exception thrown while attempting to download from: " + download.getRequestURL() + "\n");
                errorMsg.append(download.getExceptionAsString()+ "\n\n");
            }
        }
        if(errorMsg.length()!=0){
            zout.putNextEntry(new ZipEntry("error.txt"));
            zout.write(errorMsg.toString().getBytes());
            zout.closeEntry();
        }
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
                zout.putNextEntry(new ZipEntry(new SimpleDateFormat((i + 1) + "_yyyyMMdd_HHmmss").format(new Date()) + ".xml"));
            else
                zout.putNextEntry(new ZipEntry(new SimpleDateFormat((i + 1) + "_yyyyMMdd_HHmmss").format(new Date()) + ".png"));

            zout.write(responseBytes);
            zout.closeEntry();
        }

        zout.finish();
        zout.flush();
        zout.close();
    }

}
